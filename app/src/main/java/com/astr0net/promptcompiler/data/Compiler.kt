package com.astr0net.promptcompiler.data

/** Why a compile request failed; the UI maps each case to a localized message. */
sealed interface CompileError {
    data object EmptyPrompt : CompileError
    data class NotGenerateModel(val name: String) : CompileError
    data class Network(val errorType: String) : CompileError
    /** Message coming straight from Google / the client, shown as-is. */
    data class Api(val message: String) : CompileError
}

sealed interface CompileOutcome {
    data class Success(val raw: String, val parsed: ParsedResult, val usage: UsageMetadata?, val modelId: String) : CompileOutcome
    data class Failure(val error: CompileError) : CompileOutcome
}

/** The /translate endpoint of the reference app, as a plain class (mirrors api/routes/translation.py). */
class PromptCompiler(
    private val client: GeminiClient,
    private val usageStore: UsageStore,
    private val configProvider: () -> AppConfig,
    private val defaultSystemPrompt: () -> String,
) {
    suspend fun compile(persianText: String, requestedModel: String): CompileOutcome {
        val config = configProvider()
        if (persianText.isBlank()) return CompileOutcome.Failure(CompileError.EmptyPrompt)

        val modelId = requestedModel.trim().ifEmpty { config.geminiModel.ifEmpty { DEFAULT_MODEL } }
        findModel(modelId)?.let { if (!it.generate) return CompileOutcome.Failure(CompileError.NotGenerateModel(it.name)) }

        val systemPrompt = config.systemPromptOverride.ifEmpty { defaultSystemPrompt() }

        return try {
            val response = client.generate(config.geminiApiKey, systemPrompt, persianText, modelId)
            usageStore.record(modelId, response.usage)
            CompileOutcome.Success(response.text, PromptEngine.parseModelOutput(response.text), response.usage, modelId)
        } catch (e: GeminiNetworkException) {
            CompileOutcome.Failure(CompileError.Network(e.detail))
        } catch (e: GeminiException) {
            CompileOutcome.Failure(CompileError.Api(e.message.orEmpty()))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            CompileOutcome.Failure(CompileError.Api(e.message ?: e.javaClass.simpleName))
        }
    }
}
