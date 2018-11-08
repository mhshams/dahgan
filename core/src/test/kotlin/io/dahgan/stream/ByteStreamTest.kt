package io.dahgan.stream

import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.Charset

/**
 * byte stream encoding and data tests.
 */
class ByteStreamTest {
    companion object {
        private const val any: Int = 17

        val defaultUTF32BE = ByteStream("Yaml".toByteArray(Charset.forName("UTF-32BE")))
        val defaultUTF32LE = ByteStream("Yaml".toByteArray(Charset.forName("UTF-32LE")))
        val defaultUTF16BE = ByteStream("Yaml".toByteArray(Charset.forName("UTF-16BE")))
        val defaultUTF16LE = ByteStream("Yaml".toByteArray(Charset.forName("UTF-16LE")))
        val defaultUTF8 = ByteStream("Yaml".toByteArray(Charset.forName("UTF-8")))

        fun of(vararg elements: Int) = elements.map(Int::toByte).toByteArray()
    }

    @Test
    fun encoding() {
        assertEquals(Encoding.UTF32BE, defaultUTF32BE.encoding())
        assertEquals(Encoding.UTF32BE, ByteStream(of(0, 0, 0xFE, 0xFF)).encoding())
        assertEquals(Encoding.UTF32BE, ByteStream(of(0, 0, 0xFE, 0xFF, any)).encoding())
        assertEquals(Encoding.UTF32BE, ByteStream(of(0, 0, 0, any)).encoding())
        assertEquals(Encoding.UTF32BE, ByteStream(of(0, 0, 0, any, any)).encoding())

        assertEquals(Encoding.UTF32LE, defaultUTF32LE.encoding())
        assertEquals(Encoding.UTF16BE, ByteStream(of(0xFE, 0xFF)).encoding())
        assertEquals(Encoding.UTF16BE, ByteStream(of(0xFE, 0xFF, any)).encoding())
        assertEquals(Encoding.UTF16BE, ByteStream(of(0)).encoding())
        assertEquals(Encoding.UTF16BE, ByteStream(of(0, any)).encoding())
        assertEquals(Encoding.UTF16BE, defaultUTF16BE.encoding())

        assertEquals(Encoding.UTF16LE, defaultUTF16LE.encoding())
        assertEquals(Encoding.UTF16LE, ByteStream(of(0xFF, 0xFE)).encoding())
        assertEquals(Encoding.UTF16LE, ByteStream(of(0xFF, 0xFE, any)).encoding())
        assertEquals(Encoding.UTF16LE, ByteStream(of(any, 0)).encoding())
        assertEquals(Encoding.UTF16LE, ByteStream(of(any, 0, any)).encoding())

        assertEquals(Encoding.UTF8, defaultUTF8.encoding())
        assertEquals(Encoding.UTF8, ByteStream(of(0xEF, 0xBB, 0xBF, any)).encoding())
        assertEquals(Encoding.UTF8, ByteStream(of(0xEF, 0xBB, 0xBF, any, any)).encoding())
        assertEquals(Encoding.UTF8, ByteStream(of(any)).encoding())
        assertEquals(Encoding.UTF8, ByteStream(of(any, any)).encoding())
    }

    @Test
    fun headAndTail() {
        assertEquals(UniChar(4, 'Y'.toInt()), defaultUTF32BE.head())
        assertEquals(UniChar(8, 'a'.toInt()), defaultUTF32BE.tail().head())
        assertEquals(UniChar(12, 'm'.toInt()), defaultUTF32BE.tail().tail().head())
        assertEquals(UniChar(16, 'l'.toInt()), defaultUTF32BE.tail().tail().tail().head())

        assertEquals(UniChar(4, 'Y'.toInt()), defaultUTF32LE.head())
        assertEquals(UniChar(8, 'a'.toInt()), defaultUTF32LE.tail().head())
        assertEquals(UniChar(12, 'm'.toInt()), defaultUTF32LE.tail().tail().head())
        assertEquals(UniChar(16, 'l'.toInt()), defaultUTF32LE.tail().tail().tail().head())

        assertEquals(UniChar(2, 'Y'.toInt()), defaultUTF16BE.head())
        assertEquals(UniChar(4, 'a'.toInt()), defaultUTF16BE.tail().head())
        assertEquals(UniChar(6, 'm'.toInt()), defaultUTF16BE.tail().tail().head())
        assertEquals(UniChar(8, 'l'.toInt()), defaultUTF16BE.tail().tail().tail().head())

        assertEquals(UniChar(2, 'Y'.toInt()), defaultUTF16LE.head())
        assertEquals(UniChar(4, 'a'.toInt()), defaultUTF16LE.tail().head())
        assertEquals(UniChar(6, 'm'.toInt()), defaultUTF16LE.tail().tail().head())
        assertEquals(UniChar(8, 'l'.toInt()), defaultUTF16LE.tail().tail().tail().head())

        assertEquals(UniChar(1, 'Y'.toInt()), defaultUTF8.head())
        assertEquals(UniChar(2, 'a'.toInt()), defaultUTF8.tail().head())
        assertEquals(UniChar(3, 'm'.toInt()), defaultUTF8.tail().tail().head())
        assertEquals(UniChar(4, 'l'.toInt()), defaultUTF8.tail().tail().tail().head())
    }

    @Test
    fun isEmpty() {
        assertFalse(defaultUTF32BE.isEmpty())
        assertFalse(defaultUTF32BE.tail().isEmpty())
        assertFalse(defaultUTF32BE.tail().tail().isEmpty())
        assertFalse(defaultUTF32BE.tail().tail().tail().isEmpty())
        assertTrue(defaultUTF32BE.tail().tail().tail().tail().isEmpty())

        assertFalse(defaultUTF32LE.isEmpty())
        assertFalse(defaultUTF32LE.tail().isEmpty())
        assertFalse(defaultUTF32LE.tail().tail().isEmpty())
        assertFalse(defaultUTF32LE.tail().tail().tail().isEmpty())
        assertTrue(defaultUTF32LE.tail().tail().tail().tail().isEmpty())

        assertFalse(defaultUTF16BE.isEmpty())
        assertFalse(defaultUTF16BE.tail().isEmpty())
        assertFalse(defaultUTF16BE.tail().tail().isEmpty())
        assertFalse(defaultUTF16BE.tail().tail().tail().isEmpty())
        assertTrue(defaultUTF16BE.tail().tail().tail().tail().isEmpty())

        assertFalse(defaultUTF16LE.isEmpty())
        assertFalse(defaultUTF16LE.tail().isEmpty())
        assertFalse(defaultUTF16LE.tail().tail().isEmpty())
        assertFalse(defaultUTF16LE.tail().tail().tail().isEmpty())
        assertTrue(defaultUTF16LE.tail().tail().tail().tail().isEmpty())

        assertFalse(defaultUTF8.isEmpty())
        assertFalse(defaultUTF8.tail().isEmpty())
        assertFalse(defaultUTF8.tail().tail().isEmpty())
        assertFalse(defaultUTF8.tail().tail().tail().isEmpty())
        assertTrue(defaultUTF8.tail().tail().tail().tail().isEmpty())
    }

    @Test
    fun isNotEmpty() {
        assertTrue(defaultUTF32BE.isNotEmpty())
        assertTrue(defaultUTF32BE.tail().isNotEmpty())
        assertTrue(defaultUTF32BE.tail().tail().isNotEmpty())
        assertTrue(defaultUTF32BE.tail().tail().tail().isNotEmpty())
        assertFalse(defaultUTF32BE.tail().tail().tail().tail().isNotEmpty())

        assertTrue(defaultUTF32LE.isNotEmpty())
        assertTrue(defaultUTF32LE.tail().isNotEmpty())
        assertTrue(defaultUTF32LE.tail().tail().isNotEmpty())
        assertTrue(defaultUTF32LE.tail().tail().tail().isNotEmpty())
        assertFalse(defaultUTF32LE.tail().tail().tail().tail().isNotEmpty())

        assertTrue(defaultUTF16BE.isNotEmpty())
        assertTrue(defaultUTF16BE.tail().isNotEmpty())
        assertTrue(defaultUTF16BE.tail().tail().isNotEmpty())
        assertTrue(defaultUTF16BE.tail().tail().tail().isNotEmpty())
        assertFalse(defaultUTF16BE.tail().tail().tail().tail().isNotEmpty())

        assertTrue(defaultUTF16LE.isNotEmpty())
        assertTrue(defaultUTF16LE.tail().isNotEmpty())
        assertTrue(defaultUTF16LE.tail().tail().isNotEmpty())
        assertTrue(defaultUTF16LE.tail().tail().tail().isNotEmpty())
        assertFalse(defaultUTF16LE.tail().tail().tail().tail().isNotEmpty())

        assertTrue(defaultUTF8.isNotEmpty())
        assertTrue(defaultUTF8.tail().isNotEmpty())
        assertTrue(defaultUTF8.tail().tail().isNotEmpty())
        assertTrue(defaultUTF8.tail().tail().tail().isNotEmpty())
        assertFalse(defaultUTF8.tail().tail().tail().tail().isNotEmpty())
    }


    @Test
    fun push() {
        val newHead = UniChar(31, 131)

        val pushedUTF32BE = defaultUTF32BE.push(newHead)
        assertEquals(newHead, pushedUTF32BE.push(newHead).head())
        assertEquals(pushedUTF32BE, pushedUTF32BE.push(newHead).tail())

        val pushedUTF32LE = defaultUTF32LE.push(newHead)
        assertEquals(newHead, pushedUTF32LE.push(newHead).head())
        assertEquals(pushedUTF32LE, pushedUTF32LE.push(newHead).tail())

        val pushedUTF16BE = defaultUTF16BE.push(newHead)
        assertEquals(newHead, pushedUTF16BE.push(newHead).head())
        assertEquals(pushedUTF16BE, pushedUTF16BE.push(newHead).tail())

        val pushedUTF16LE = defaultUTF16LE.push(newHead)
        assertEquals(newHead, pushedUTF16LE.push(newHead).head())
        assertEquals(pushedUTF16LE, pushedUTF16LE.push(newHead).tail())

        val pushedUTF8 = defaultUTF8.push(newHead)
        assertEquals(newHead, pushedUTF8.push(newHead).head())
        assertEquals(pushedUTF8, pushedUTF8.push(newHead).tail())
    }

    @Test
    fun codes() {
        val expected = "89, 97, 109, 108" // Yaml

        assertEquals(expected, defaultUTF32BE.codes().joinToString())
        assertEquals(expected, defaultUTF32LE.codes().joinToString())
        assertEquals(expected, defaultUTF16BE.codes().joinToString())
        assertEquals(expected, defaultUTF16LE.codes().joinToString())
        assertEquals(expected, defaultUTF8.codes().joinToString())
    }
}