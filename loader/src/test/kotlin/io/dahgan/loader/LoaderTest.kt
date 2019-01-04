package io.dahgan.loader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Verifies Loader
 */
class LoaderTest {

    companion object {
        val BASE_PATH = File(LoaderTest::class.java.getResource("/io/dahgan/loader").file)
    }


    @Test
    fun `load empty string`() {
        assertEquals(emptyList<YmlElement>(), loadx("").elements)
    }

    @Test
    fun `load empty document`() {
        assertEquals(listOf(YmlScalar("")), loadx("---").elements)
    }

    @Test
    fun `load empty document with a comment`() {
        assertEquals(
            listOf(
                YmlScalar(""),
                YmlComment("a comment")
            ), loadx("--- #a comment").elements
        )
    }

    @Test
    fun `load empty document with a comment in new line`() {
        assertEquals(
            listOf(
                YmlScalar(""),
                YmlLineBreak,
                YmlComment("a comment")
            ), loadx("---\n#a comment").elements
        )
    }

    @Test
    fun `load a scalar`() {
        val text = "a single scalar"

        assertEquals(listOf(YmlScalar(text)), loadx(text).elements)
    }

    @Test
    fun `load a pair of scalars`() {
        val text = "foo: bar"

        assertEquals(
            listOf(
                YmlMap(
                    listOf(
                        YmlPair(YmlScalar("foo"), YmlScalar("bar"))
                    )
                )
            ),
            loadx(text).elements
        )
    }

    @Test
    fun `load sequence of scalars`() {
        val text = "- a\n- b\n- c"

        assertEquals(
            listOf(
                YmlSequence(
                    listOf(
                        YmlScalar("a"),
                        YmlLineBreak,
                        YmlScalar("b"),
                        YmlLineBreak,
                        YmlScalar("c")
                    )
                )
            ), loadx(text).elements
        )
    }

    @Test
    fun `load sequence of pairs`() {
        val text = "- a: a value\n- b: b value\n- c: c value"

        assertEquals(
            listOf(
                YmlSequence(
                    listOf(
                        YmlMap(
                            listOf(
                                YmlPair(YmlScalar("a"), YmlScalar("a value")),
                                YmlLineBreak
                            )
                        ),
                        YmlMap(
                            listOf(
                                YmlPair(YmlScalar("b"), YmlScalar("b value")),
                                YmlLineBreak
                            )
                        ),
                        YmlMap(
                            listOf(
                                YmlPair(YmlScalar("c"), YmlScalar("c value"))
                            )
                        )
                    )
                )
            ), loadx(text).elements
        )
    }

    @Test
    fun `load mapping of sequences`() {
        val text = """
            a:
                - foo
                - bar
            b:
                - baz
                - bux
        """

        val map = assertMapping(2, load(text))

        assertSequence(listOf("foo", "bar"), map[YmlScalar("a")])
        assertSequence(listOf("baz", "bux"), map[YmlScalar("b")])
    }

    @Test
    fun `load sequence of mappings`() {
        val text = """
            - a:
                - foo
                - bar
            - b:
                - baz
                - bux
        """

        val sequence = assertSequence(2, load(text))

        assertMapping(1, sequence[0])
        assertSequence(listOf("foo", "bar"), (sequence[0] as Map<*, *>)[YmlScalar("a")])

        assertMapping(1, sequence[1])
        assertSequence(listOf("baz", "bux"), (sequence[1] as Map<*, *>)[YmlScalar("b")])
    }

    @Test
    fun `load multi line text`() {
        val text = """
            comments: |
                Late afternoon is best.
                Backup contact is Nancy
                Billsmer @ 338-4338.
        """

        assertPair("comments", "Late afternoon is best.\nBackup contact is Nancy\nBillsmer @ 338-4338.\n", load(text))
    }

    @Test
    fun `load folded text`() {
        val text = """
            comments: >
                Late afternoon is best.
                Backup contact is Nancy
                Billsmer @ 338-4338.
        """

        assertPair("comments", "Late afternoon is best. Backup contact is Nancy Billsmer @ 338-4338.\n", load(text))
    }

    @Test
    fun `load implicit folded text`() {
        val text = """
            comments:
                Late afternoon is best.
                Backup contact is Nancy
                Billsmer @ 338-4338.
        """

        assertPair("comments", "Late afternoon is best. Backup contact is Nancy Billsmer @ 338-4338.", load(text))
    }

    @Test
    fun `load anchor alias`() {
        val text = """
            sender: &123
                name: Nancy Billsmer
            receiver: *123
        """

        val map = assertMapping(2, load(text))
        assertPair("name", "Nancy Billsmer", map[YmlScalar("sender")])
        assertPair("name", "Nancy Billsmer", map[YmlScalar("receiver")])
    }

    @Test
    fun `load all scalars`() {
        val first = "First Scalar"
        val second = "Second Scalar"
        val third = "Third Scalar"

        val documents = assertDocuments(3, loadAll("---\n$first\n...\n---\n$second\n...\n---\n$third\n..."))

        assertScalar(first, documents[0])
        assertScalar(second, documents[1])
        assertScalar(third, documents[2])
    }

    @Test
    fun `load all pairs`() {
        val first = "foo 1: bar 1"
        val second = "foo 2: bar 2"
        val third = "foo 3: bar 3"

        val documents = assertDocuments(3, loadAll("---\n$first\n...\n---\n$second\n...\n---\n$third\n..."))

        assertPair("foo 1", "bar 1", documents[0])
        assertPair("foo 2", "bar 2", documents[1])
        assertPair("foo 3", "bar 3", documents[2])
    }

    @Test
    fun `load all sequence of scalars`() {
        val first = """
            - a
            - b
        """
        val second = """
            - c
            - d
        """

        val documents = assertDocuments(2, loadAll("---\n$first\n...\n---\n$second\n..."))

        assertSequence(listOf("a", "b"), documents[0])
        assertSequence(listOf("c", "d"), documents[1])
    }

    @Test
    fun `load all sequences of mappings`() {
        val first = """---
            - a:
                - foo
                - bar
        """
        val second = """---
            - b:
                - baz
                - bux
        """

        val documents = assertDocuments(2, loadAll("$first\n$second"))

        val firstSequence = assertSequence(1, documents[0])

        assertMapping(1, firstSequence[0])
        assertSequence(listOf("foo", "bar"), (firstSequence[0] as Map<*, *>)[YmlScalar("a")])

        val secondSequence = assertSequence(1, documents[1])

        assertMapping(1, secondSequence[0])
        assertSequence(listOf("baz", "bux"), (secondSequence[0] as Map<*, *>)[YmlScalar("b")])
    }

    @Test
    fun `load comments file`() {
        val comments = assertMapping(2, load(File(BASE_PATH, "comments.yaml")))

        assertSequence(listOf("Mark McGwire", "Sammy Sosa"), comments[YmlScalar("hr")])
        assertSequence(listOf("Sammy Sosa", "Ken Griffey"), comments[YmlScalar("rbi")])
    }

    @Test
    fun `load all log file`() {
        val logs = assertDocuments(3, loadAll(File(BASE_PATH, "log.yaml")))

        assertPair("Time", "2001-11-23 15:01:42 -5", logs[0])
        assertPair("Time", "2001-11-23 15:02:31 -5", logs[1])
        assertPair("Date", "2001-11-23 15:03:17 -5", logs[2])

        assertPair("User", "ed", logs[0])
        assertPair("User", "ed", logs[1])
        assertPair("User", "ed", logs[2])

        assertPair("Warning", "This is an error message for the log file", logs[0])
        assertPair("Warning", "A slightly different error message.", logs[1])

        assertPair("Fatal", """Unknown variable "bar"""", logs[2])
        val files = assertSequence(2, (logs[2] as Map<*, *>)[YmlScalar("Stack")])

        assertPair("file", "TopClass.py", files[0])
        assertPair("file", "MoreClass.py", files[1])

        assertPair("line", "23", files[0])
        assertPair("line", "58", files[1])

        assertPair("code", "x = MoreObject(\"345\\x5cn\")\n", files[0])
        assertPair("code", "foo = bar", files[1])
    }

    @Test
    fun `load all game file`() {
        val games = assertDocuments(2, loadAll(File(BASE_PATH, "game.yaml")))

        assertPair("time", "20:03:20", games[0])
        assertPair("time", "20:03:47", games[1])

        assertPair("player", "Sammy Sosa", games[0])
        assertPair("player", "Sammy Sosa", games[1])

        assertPair("action", "strike (miss)", games[0])
        assertPair("action", "grand slam", games[1])
    }

    @Test
    fun `load invoice file`() {
        val invoice = load(File(BASE_PATH, "invoice.yaml"))

        assertPair("invoice", "34843", invoice)
        assertPair("date", "2001-01-23", invoice)
        assertPair("tax", "251.42", invoice)
        assertPair("total", "4443.52", invoice)
        assertPair("comments", "Late afternoon is best. Backup contact is Nancy Billsmer @ 338-4338.", invoice)

        val assertContact = { contact: Map<*, *> ->
            assertPair("given", "Chris", contact)
            assertPair("family", "Dumars", contact)
            val address = assertMapping(4, contact[YmlScalar("address")])
            assertPair("lines", "458 Walkman Dr.\nSuite #292\n", address)
            assertPair("city", "Royal Oak", address)
            assertPair("state", "MI", address)
            assertPair("postal", "48046", address)
        }

        assertContact(assertMapping(3, (invoice as Map<*, *>)[YmlScalar("bill-to")]))
        assertContact(assertMapping(3, invoice[YmlScalar("ship-to")]))

        val product = assertSequence(2, invoice[YmlScalar("product")])
        assertPair("sku", "BL394D", product[0])
        assertPair("quantity", "4", product[0])
        assertPair("description", "Basketball", product[0])
        assertPair("price", "450.00", product[0])

        assertPair("sku", "BL4438H", product[1])
        assertPair("quantity", "1", product[1])
        assertPair("description", "Super Hoop", product[1])
        assertPair("price", "2392.00", product[1])
    }

    @Test(expected = IllegalStateException::class)
    fun `load invalid document`() {
        val text = "foo: bar \nfoo"

        assertPair("foo", "bar", load(text))
    }

    private fun assertDocuments(size: Int, documents: List<Any>): List<*> {
        assertEquals(size, documents.size)
        return documents
    }

    private fun assertMapping(size: Int, container: Any?): Map<*, *> {
        assertTrue(container is Map<*, *>)
        assertEquals(size, (container as Map<*, *>).size)
        return container
    }

    private fun assertSequence(size: Int, container: Any?): List<*> {
        assertTrue(container is List<*>)
        assertEquals(size, (container as List<*>).size)
        return container
    }

    private fun assertSequence(expected: List<String>, container: Any?): List<*> {
        val result = assertSequence(expected.size, container)

        for (i in (0 until expected.size)) {
            assertEquals(YmlScalar(expected[i]), result[i])
        }

        return result
    }

    private fun assertPair(key: Any, value: Any, container: Any?) {
        assertTrue(container is Map<*, *>)
        val map = container as Map<*, *>

        assertEquals(YmlScalar(value as String), map[YmlScalar(key as String)])
    }

    private fun assertScalar(text: String, container: Any?) {
        assertTrue(container is YmlScalar)
        assertEquals(text, (container as YmlScalar).value)
    }
}