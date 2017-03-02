package io.dahgan.stream

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.Charset

/**
 * UTF-32 decoder tests.
 */
class UTF32DecoderTest {
    companion object {
        val decoderBe = UTF32Decoder.be()
        val decoderLe = UTF32Decoder.le()
        val utf32be = "Yaml".toByteArray(Charset.forName("UTF-32BE"))
        val utf32le = "Yaml".toByteArray(Charset.forName("UTF-32LE"))
    }

    @Test
    fun decodeBe() {
        assertEquals(UniChar(4, 'Y'.toInt()), decoderBe.decode(utf32be, 0))
        assertEquals(UniChar(8, 'a'.toInt()), decoderBe.decode(utf32be, 4))
        assertEquals(UniChar(12, 'm'.toInt()), decoderBe.decode(utf32be, 8))
        assertEquals(UniChar(16, 'l'.toInt()), decoderBe.decode(utf32be, 12))
    }

    @Test
    fun decodeLe() {
        assertEquals(UniChar(4, 'Y'.toInt()), decoderLe.decode(utf32le, 0))
        assertEquals(UniChar(8, 'a'.toInt()), decoderLe.decode(utf32le, 4))
        assertEquals(UniChar(12, 'm'.toInt()), decoderLe.decode(utf32le, 8))
        assertEquals(UniChar(16, 'l'.toInt()), decoderLe.decode(utf32le, 12))
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