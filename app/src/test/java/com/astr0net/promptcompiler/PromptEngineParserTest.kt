package com.astr0net.promptcompiler

import com.astr0net.promptcompiler.data.KNOWN_CATEGORIES
import com.astr0net.promptcompiler.data.PromptEngine.parseModelOutput
import org.junit.Assert.assertEquals
import org.junit.Test

/** Port of ParseModelOutputTests. */
class PromptEngineParserTest {
    @Test fun validRawJson() {
        val raw = """{"detected_category": "coding", "detected_subcategory": "api",
            "detected_purpose": "Build a Flask API with login.",
            "persian_prompt": "پرامپت فارسی", "english_prompt": "Build a Flask API.",
            "preserved_requirements": ["Flask"], "assumptions": ["None"],
            "missing_information": ["Auth method"], "suggestions": ["Add tests"]}"""
        val out = parseModelOutput(raw)
        assertEquals("coding", out.detectedCategory)
        assertEquals("api", out.detectedSubcategory)
        assertEquals("Build a Flask API.", out.englishPrompt)
        assertEquals("پرامپت فارسی", out.persianPrompt)
        assertEquals(listOf("Flask"), out.preservedRequirements)
        assertEquals(listOf("Auth method"), out.missingInformation)
        assertEquals(listOf("Add tests"), out.suggestions)
    }

    @Test fun markdownWrappedJson() {
        val out = parseModelOutput("```json\n{\"detected_category\": \"research\", \"english_prompt\": \"Research X.\"}\n```")
        assertEquals("research", out.detectedCategory)
        assertEquals("Research X.", out.englishPrompt)
    }

    @Test fun uppercaseFenceIsStripped() {
        val out = parseModelOutput("```JSON\n{\"english_prompt\": \"Y.\"}\n```")
        assertEquals("Y.", out.englishPrompt)
    }

    @Test fun invalidJsonFallsBackGracefully() {
        val out = parseModelOutput("this is not json at all")
        assertEquals("general", out.detectedCategory)
        assertEquals("this is not json at all", out.englishPrompt)
        assertEquals(emptyList<String>(), out.preservedRequirements)
        assertEquals(emptyList<String>(), out.missingInformation)
    }

    @Test fun jsonArrayFallsBack() {
        assertEquals("[1, 2]", parseModelOutput("[1, 2]").englishPrompt)
    }

    @Test fun unknownCategoryBecomesGeneral() =
        assertEquals("general", parseModelOutput("""{"detected_category": "something_unknown", "english_prompt": "X."}""").detectedCategory)

    @Test fun emptyEnglishPromptGetsPlaceholder() =
        assertEquals("No prompt was generated.", parseModelOutput("""{"detected_category": "general", "english_prompt": ""}""").englishPrompt)

    @Test fun nullOrBlankInputGetsPlaceholder() {
        assertEquals("No prompt was generated.", parseModelOutput(null).englishPrompt)
        assertEquals("No prompt was generated.", parseModelOutput("   ").englishPrompt)
    }

    @Test fun stringFieldsAreNormalized() {
        val out = parseModelOutput("""{"detected_category": "coding", "suggestions": "single string",
            "missing_information": "", "preserved_requirements": ["a", "  ", "b"]}""")
        assertEquals(listOf("single string"), out.suggestions)
        assertEquals(emptyList<String>(), out.missingInformation)
        assertEquals(listOf("a", "b"), out.preservedRequirements)
    }

    @Test fun camelCaseFallbackKeys() {
        val out = parseModelOutput("""{"englishPrompt": "E.", "persianPrompt": "ف"}""")
        assertEquals("E.", out.englishPrompt)
        assertEquals("ف", out.persianPrompt)
    }

    @Test fun everyKnownCategoryIsAccepted() = KNOWN_CATEGORIES.forEach {
        assertEquals(it, parseModelOutput("""{"detected_category": "$it", "english_prompt": "P."}""").detectedCategory)
    }
}
