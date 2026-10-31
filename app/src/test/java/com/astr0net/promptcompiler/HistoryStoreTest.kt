package com.astr0net.promptcompiler

import com.astr0net.promptcompiler.data.HistoryStore
import com.astr0net.promptcompiler.data.PromptEngine
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class HistoryStoreTest {
    @get:Rule val tmp = TemporaryFolder()
    private var now = 1_800_000_000_000L
    private fun store() = HistoryStore(File(tmp.root, "history.json")) { now++ }
    private val result = PromptEngine.parseModelOutput("""{"detected_category":"coding","detected_subcategory":"api","english_prompt":"E","persian_prompt":"ف","preserved_requirements":["a"],"missing_information":["m"]}""")

    @Test fun roundTripsAndOrdersNewestFirst() {
        val s = store()
        s.add("one", "gemini-3.5-flash", result); s.add("two", "gemini-2.5-pro", result)
        val items = store().load()
        assertEquals(listOf("two", "one"), items.map { it.input })
        assertEquals(result, items[0].result)
        assertEquals("gemini-2.5-pro", items[0].modelId)
    }

    @Test fun deleteAndClear() {
        val s = store()
        val a = s.add("a", "m", result).first().id
        s.add("b", "m", result)
        assertEquals(listOf("b"), s.delete(a).map { it.input })
        s.clear()
        assertEquals(0, s.load().size)
    }

    @Test fun capsAtMaxItems() {
        val s = store()
        repeat(HistoryStore.MAX_ITEMS + 5) { s.add("i$it", "m", result) }
        val items = s.load()
        assertEquals(HistoryStore.MAX_ITEMS, items.size)
        assertEquals("i${HistoryStore.MAX_ITEMS + 4}", items.first().input)
    }

    @Test fun corruptFileIsIgnored() {
        File(tmp.root, "history.json").writeText("{nope")
        assertEquals(0, store().load().size)
    }
}
