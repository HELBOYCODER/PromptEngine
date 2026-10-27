package com.astr0net.promptcompiler

import com.astr0net.promptcompiler.data.KNOWN_CATEGORIES
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Port of SystemPromptPolicyTests / FinalPolicyTests: the bundled system prompt must stay intact. */
class SystemPromptPolicyTest {
    private val prompt = File("src/main/assets/system_prompt.txt").readText(Charsets.UTF_8)

    private fun has(vararg phrases: String) = phrases.forEach { assertTrue("missing: $it", it in prompt) }

    @Test fun integrityPolicy() = has("Requirement Integrity Policy")

    @Test fun doesNotEncourageFollowUpFilling() =
        assertFalse("comprehensive enough that an AI agent can execute it without asking follow-up questions" in prompt)

    @Test fun antiHallucinationRules() = has(
        "NEVER invent requirements",
        "NEVER fabricate technical specifications",
        "NEVER add technologies simply because they are considered",
        "NEVER convert optional recommendations into mandatory requirements",
        "NEVER silently resolve important ambiguities by guessing",
    )

    @Test fun badGoodExample() = has(
        "Build a Flask website using PostgreSQL, Redis, Docker",
        "Do not introduce additional frameworks, databases, or infrastructure",
    )

    @Test fun allCategoriesHaveGuidance() {
        (KNOWN_CATEGORIES - "general").forEach { has("### $it") }
        has("- general")
    }

    @Test fun schemaFields() = has("preserved_requirements", "assumptions", "missing_information", "suggestions")

    @Test fun proportionalityAndCanonicalFlow() = has(
        "Proportionality Rule",
        "compile(user_input) -> english_prompt",
        "translate(english_prompt) -> persian_prompt",
        "Consistency Validation (English/Persian)",
        "Interpretation Boundary",
        "از PostgreSQL استفاده نکن.",
    )
}
