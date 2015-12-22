package io.dahgan.loader

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 */
class LoaderTest {
    @Test
    fun loadSingleScalar() {
        val text = "a single scalar"

        assertScalar(text, load(text))
    }

    @Test
    fun loadSinglePair() {
        val text = "foo: bar"

        assertPair("foo", "bar", load(text))
    }

    @Test
    fun loadSequenceOfScalars() {
        val text = """
            - a
            - b
            - c
        """

        assertSequence(listOf("a", "b", "c"), load(text))
    }

    @Test
    fun loadSequenceOfPairs() {
        val text = """
            - a: a value
            - b: b value
            - c: c value
        """

        val list = assertSequence(3, load(text))

        assertPair("a", "a value", list[0])
        assertPair("b", "b value", list[1])
        assertPair("c", "c value", list[2])
    }

    @Test
    fun loadMappingOfSequences() {
        val text = """
            a:
                - foo
                - bar
            b:
                - baz
                - bux
        """

        val map = assertMapping(2, load(text))

        assertSequence(listOf("foo", "bar"), map["a"])
        assertSequence(listOf("baz", "bux"), map["b"])
    }

    @Test
    fun loadSequenceOfMappings() {
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
        assertSequence(listOf("foo", "bar"), (sequence[0] as Map<*, *>)["a"])

        assertMapping(1, sequence[1])
        assertSequence(listOf("baz", "bux"), (sequence[1] as Map<*, *>)["b"])
    }

    @Test
    fun loadAllScalars() {
        val first = "First Scalar"
        val second = "Second Scalar"
        val third = "Third Scalar"

        val documents = assertDocuments(3, loadAll("---\n$first\n...\n---\n$second\n...\n---\n$third\n..."))

        assertScalar(first, documents[0])
        assertScalar(second, documents[1])
        assertScalar(third, documents[2])
    }

    @Test
    fun loadAllPairs() {
        val first = "foo 1: bar 1"
        val second = "foo 2: bar 2"
        val third = "foo 3: bar 3"

        val documents = assertDocuments(3, loadAll("---\n$first\n...\n---\n$second\n...\n---\n$third\n..."))

        assertPair("foo 1", "bar 1", documents[0])
        assertPair("foo 2", "bar 2", documents[1])
        assertPair("foo 3", "bar 3", documents[2])
    }

    @Test
    fun loadAllSequenceOfScalars() {
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
    fun loadAllSequencesOfMappings() {
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
        assertSequence(listOf("foo", "bar"), (firstSequence[0] as Map<*, *>)["a"])

        val secondSequence = assertSequence(1, documents[1])

        assertMapping(1, secondSequence[0])
        assertSequence(listOf("baz", "bux"), (secondSequence[0] as Map<*, *>)["b"])
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

    private fun assertSequence(expected: List<*>, container: Any?): List<*> {
        val result = assertSequence(expected.size, container)

        for (i in (0..expected.size - 1)) {
            assertEquals(expected[i], result[i])
        }

        return result
    }

    private fun assertPair(key: Any, value: Any, container: Any?) {
        assertTrue(container is Map<*, *>)
        val map = container as Map<*, *>

        assertEquals(value, map[key])
    }

    private fun assertScalar(text: String, container: Any?): String {
        assertTrue(container is String)
        assertEquals(text, container)
        return container as String
    }
}