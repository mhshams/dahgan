package io.dahgan.stream

import org.junit.Test
import kotlin.test.assertEquals

/**
 * UTF-16 decoder tests.
 */
class UTF16DecoderTest {
    companion object {
        val decoderBe = UTF16Decoder.be()
        val decoderLe = UTF16Decoder.le()
        val utf16be = "Yaml".toByteArray(Charsets.UTF_16BE)
        val utf16le = "Yaml".toByteArray(Charsets.UTF_16LE)
    }

    @Test
    fun decodeBe() {
        assertEquals(UniChar(2, 'Y'.toInt()), decoderBe.decode(utf16be, 0))
        assertEquals(UniChar(4, 'a'.toInt()), decoderBe.decode(utf16be, 2))
        assertEquals(UniChar(6, 'm'.toInt()), decoderBe.decode(utf16be, 4))
        assertEquals(UniChar(8, 'l'.toInt()), decoderBe.decode(utf16be, 6))
    }

    @Test
    fun decodeLe() {
        assertEquals(UniChar(2, 'Y'.toInt()), decoderLe.decode(utf16le, 0))
        assertEquals(UniChar(4, 'a'.toInt()), decoderLe.decode(utf16le, 2))
        assertEquals(UniChar(6, 'm'.toInt()), decoderLe.decode(utf16le, 4))
        assertEquals(UniChar(8, 'l'.toInt()), decoderLe.decode(utf16le, 6))
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWithInvalidFirstByte() {
        decoderBe.decode(byteArrayOf(0x81.toByte()), 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWithAnotherInvalidFirstByte() {
        decoderLe.decode(byteArrayOf(0xF9.toByte()), 0)
    }
}