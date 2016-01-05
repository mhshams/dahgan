package io.dahgan.stream

import org.junit.Test
import kotlin.test.assertEquals

/**
 * UTF-8 decoder tests.
 */
class UTF8DecoderTest {
    companion object {
        val decoder = UTF8Decoder()
        val singleBytes = "Yaml".toByteArray(Charsets.UTF_8)

        // 1-byte;2-bytes;3-bytes;4-bytes
        val variableBytes = "\u0024\u00A2\u20AC\uD83D\uDF01".toByteArray(Charsets.UTF_8)
    }

    @Test
    fun decode() {
        assertEquals(UniChar(1, 'Y'.toInt()), decoder.decode(singleBytes, 0))
        assertEquals(UniChar(2, 'a'.toInt()), decoder.decode(singleBytes, 1))
        assertEquals(UniChar(3, 'm'.toInt()), decoder.decode(singleBytes, 2))
        assertEquals(UniChar(4, 'l'.toInt()), decoder.decode(singleBytes, 3))
    }

    @Test
    fun decodeWithVariableLength() {
        assertEquals(UniChar(1, 0x24), decoder.decode(variableBytes, 0))
        assertEquals(UniChar(3, 0xA2), decoder.decode(variableBytes, 1))
        assertEquals(UniChar(6, 0x20AC), decoder.decode(variableBytes, 3))
        assertEquals(UniChar(10, 0x1F701), decoder.decode(variableBytes, 6))
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWithInvalidFirstByte() {
        decoder.decode(byteArrayOf(0x81.toByte()), 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWithAnotherInvalidFirstByte() {
        decoder.decode(byteArrayOf(0xF9.toByte()), 0)
    }
}