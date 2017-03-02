package io.dahgan.stream

import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.Charset

/**
 * pushed stream encoding and data tests.
 */
class PushedStreamTest {
    companion object {
        val any: Int = 17
        val head = UniChar(any, any)

        val defaultUTF32BE = ByteStream("Yaml".toByteArray(Charset.forName("UTF-32BE")))
        val pushedUTF32BE = PushedStream(head, defaultUTF32BE)

        val defaultUTF32LE = ByteStream("Yaml".toByteArray(Charset.forName("UTF-32LE")))
        val pushedUTF32LE = PushedStream(head, defaultUTF32LE)

        val defaultUTF16BE = ByteStream("Yaml".toByteArray(Charset.forName("UTF-16BE")))
        val pushedUTF16BE = PushedStream(head, defaultUTF16BE)

        val defaultUTF16LE = ByteStream("Yaml".toByteArray(Charset.forName("UTF-16LE")))
        val pushedUTF16LE = PushedStream(head, defaultUTF16LE)

        val defaultUTF8 = ByteStream("Yaml".toByteArray(Charset.forName("UTF-8")))
        val pushedUTF8 = PushedStream(head, defaultUTF8)
    }

    @Test
    fun encoding() {

        assertEquals(defaultUTF32BE.encoding(), pushedUTF32BE.encoding())
        assertEquals(defaultUTF32LE.encoding(), pushedUTF32LE.encoding())
        assertEquals(defaultUTF16BE.encoding(), pushedUTF16BE.encoding())
        assertEquals(defaultUTF16LE.encoding(), pushedUTF16LE.encoding())
        assertEquals(defaultUTF8.encoding(), pushedUTF8.encoding())
    }

    @Test
    fun head() {
        assertEquals(head, pushedUTF32BE.head())
        assertEquals(head, pushedUTF32LE.head())
        assertEquals(head, pushedUTF16BE.head())
        assertEquals(head, pushedUTF16LE.head())
        assertEquals(head, pushedUTF8.head())
    }

    @Test
    fun tail() {
        assertEquals(defaultUTF32BE, pushedUTF32BE.tail())
        assertEquals(defaultUTF32LE, pushedUTF32LE.tail())
        assertEquals(defaultUTF16BE, pushedUTF16BE.tail())
        assertEquals(defaultUTF16LE, pushedUTF16LE.tail())
        assertEquals(defaultUTF8, pushedUTF8.tail())
    }

    @Test
    fun isEmpty() {
        assertFalse(pushedUTF32BE.isEmpty())
        assertFalse(pushedUTF32LE.isEmpty())
        assertFalse(pushedUTF16BE.isEmpty())
        assertFalse(pushedUTF16LE.isEmpty())
        assertFalse(pushedUTF8.isEmpty())
    }

    @Test
    fun isNotEmpty() {
        assertTrue(pushedUTF32BE.isNotEmpty())
        assertTrue(pushedUTF32LE.isNotEmpty())
        assertTrue(pushedUTF16BE.isNotEmpty())
        assertTrue(pushedUTF16LE.isNotEmpty())
        assertTrue(pushedUTF8.isNotEmpty())
    }

    @Test
    fun push() {
        val newHead = UniChar(31, 131)

        assertEquals(newHead, pushedUTF32BE.push(newHead).head())
        assertEquals(pushedUTF32BE, pushedUTF32BE.push(newHead).tail())

        assertEquals(newHead, pushedUTF32LE.push(newHead).head())
        assertEquals(pushedUTF32LE, pushedUTF32LE.push(newHead).tail())

        assertEquals(newHead, pushedUTF16BE.push(newHead).head())
        assertEquals(pushedUTF16BE, pushedUTF16BE.push(newHead).tail())

        assertEquals(newHead, pushedUTF16LE.push(newHead).head())
        assertEquals(pushedUTF16LE, pushedUTF16LE.push(newHead).tail())

        assertEquals(newHead, pushedUTF8.push(newHead).head())
        assertEquals(pushedUTF8, pushedUTF8.push(newHead).tail())
    }

    @Test
    fun codes() {
        val newHead = UniChar(31, 131)
        val expected = "131, 17, 89, 97, 109, 108"

        assertEquals(expected, pushedUTF32BE.push(newHead).codes().joinToString())
        assertEquals(expected, pushedUTF32LE.push(newHead).codes().joinToString())
        assertEquals(expected, pushedUTF16BE.push(newHead).codes().joinToString())
        assertEquals(expected, pushedUTF16LE.push(newHead).codes().joinToString())
        assertEquals(expected, pushedUTF8.push(newHead).codes().joinToString())
    }
}