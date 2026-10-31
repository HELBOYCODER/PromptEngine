package com.astr0net.promptcompiler

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.astr0net.promptcompiler.data.AppConfig
import com.astr0net.promptcompiler.data.CompileError
import com.astr0net.promptcompiler.data.CompileOutcome
import com.astr0net.promptcompiler.data.DEFAULT_MODEL
import com.astr0net.promptcompiler.data.GENERATE_MODELS
import com.astr0net.promptcompiler.data.HistoryItem
import com.astr0net.promptcompiler.data.ParsedResult
import com.astr0net.promptcompiler.data.UsageSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A one-shot message for the toast: a string resource, or text that came from the API. */
sealed interface Msg {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : Msg
    data class Raw(val text: String) : Msg
}

enum class Screen { Editor, History, Usage, Settings }

data class UiState(
    val input: String = "",
    val modelId: String = DEFAULT_MODEL,
    val loading: Boolean = false,
    val result: ParsedResult? = null,
    val usage: UsageSnapshot? = null,
    val config: AppConfig = AppConfig(),
    val screen: Screen = Screen.Editor,
    val history: List<HistoryItem> = emptyList(),
)

class MainViewModel(private val app: PromptEngineApp) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _messages = Channel<Msg>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    private var job: Job? = null

    init {
        val config = app.configStore.load()
        _state.update { it.copy(config = config, modelId = usableModel(config.geminiModel), usage = app.usageStore.compute(), history = app.historyStore.load()) }
    }

    private fun usableModel(id: String) = if (GENERATE_MODELS.any { it.id == id }) id else GENERATE_MODELS.first().id

    fun setInput(text: String) = _state.update { it.copy(input = text) }
    fun selectModel(id: String) = _state.update { it.copy(modelId = id) }
    fun open(screen: Screen) {
        if (screen == Screen.Usage) refreshUsage()
        _state.update { it.copy(screen = screen, history = if (screen == Screen.History) app.historyStore.load() else it.history) }
    }
    fun openHistoryItem(item: HistoryItem) = _state.update {
        it.copy(input = item.input, result = item.result, screen = Screen.Editor, modelId = if (GENERATE_MODELS.any { m -> m.id == item.modelId }) item.modelId else it.modelId)
    }

    fun deleteHistory(id: Long) { _state.update { it.copy(history = app.historyStore.delete(id)) } }

    fun clearHistory() {
        app.historyStore.clear()
        _state.update { it.copy(history = emptyList()) }
    }

    fun refreshUsage() = _state.update { it.copy(usage = app.usageStore.compute()) }

    fun resetUsage() {
        app.usageStore.clear()
        refreshUsage()
    }

    fun saveConfig(config: AppConfig) {
        app.configStore.save(config)
        val saved = app.configStore.load()
        _state.update { it.copy(config = saved, modelId = if (it.config.geminiModel != saved.geminiModel) usableModel(saved.geminiModel) else it.modelId) }
    }

    fun compile() {
        val s = _state.value
        if (s.loading) return
        if (s.input.isBlank()) {
            _messages.trySend(Msg.Res(R.string.toast_write_prompt))
            return
        }
        _state.update { it.copy(loading = true) }
        job = viewModelScope.launch {
            val outcome = app.compiler.compile(s.input.trim(), s.modelId)
            when (outcome) {
                is CompileOutcome.Success -> {
                    val history = app.historyStore.add(s.input.trim(), outcome.modelId, outcome.parsed)
                    _state.update { it.copy(result = outcome.parsed, loading = false, usage = app.usageStore.compute(), history = history) }
                }
                is CompileOutcome.Failure -> {
                    _state.update { it.copy(loading = false) }
                    _messages.trySend(
                        when (val e = outcome.error) {
                            CompileError.EmptyPrompt -> Msg.Res(R.string.toast_write_prompt)
                            is CompileError.NotGenerateModel -> Msg.Res(R.string.err_not_generate, listOf(e.name))
                            is CompileError.Network -> Msg.Res(R.string.err_network, listOf(e.errorType))
                            is CompileError.Api -> Msg.Raw(e.message)
                        }
                    )
                }
            }
        }
    }

    fun toast(msg: Msg) { _messages.trySend(msg) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { MainViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PromptEngineApp) }
        }
    }
}
