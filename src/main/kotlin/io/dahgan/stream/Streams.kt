package io.dahgan.stream

import java.util.ArrayList

/**
 * Wraps the given byte arrays, detects its encoding and finally converts it to characters.
 */
class ByteStream(private val input: ByteArray, private val offset: Int = 0) : Stream {
    companion object {
        private val X00 = 0x00.toByte()
        private val XFE = 0xFE.toByte()
        private val XFF = 0xFF.toByte()
        private val XEF = 0xEF.toByte()
        private val XBB = 0xBB.toByte()
        private val XBF = 0xBF.toByte()
    }

    private val encoding: Encoding by lazy {
        when {
            input.size >= 4 && input[0] == X00 && input[1] == X00 && input[2] == XFE && input[3] == XFF -> Encoding.UTF32BE
            input.size >= 4 && input[0] == X00 && input[1] == X00 && input[2] == X00 -> Encoding.UTF32BE
            input.size >= 4 && input[0] == XFF && input[1] == XFE && input[2] == X00 && input[3] == X00 -> Encoding.UTF32LE
            input.size >= 4 && input[1] == X00 && input[2] == X00 && input[3] == X00 -> Encoding.UTF32LE
            input.size >= 2 && input[0] == XFE && input[1] == XFF -> Encoding.UTF16BE
            input.size >= 1 && input[0] == X00 -> Encoding.UTF16BE
            input.size >= 2 && input[0] == XFF && input[1] == XFE -> Encoding.UTF16LE
            input.size >= 2 && input[1] == X00 -> Encoding.UTF16LE
            input.size >= 4 && input[0] == XEF && input[1] == XBB && input[2] == XBF -> Encoding.UTF8
            else -> Encoding.UTF8
        }
    }

    private val head: UniChar by lazy {
        when (encoding) {
            Encoding.UTF8 -> undoUTF8(input, offset)
            Encoding.UTF16LE -> combinePairs(undoUTF16LE(input, offset), input)
            Encoding.UTF16BE -> combinePairs(undoUTF16BE(input, offset), input)
            Encoding.UTF32LE -> undoUTF32LE(input, offset)
            Encoding.UTF32BE -> undoUTF32BE(input, offset)
        }
    }

    /**
     * @see Stream#encoding()
     */
    override fun encoding(): Encoding = encoding

    /**
     * @see Stream#head()
     */
    override fun head(): UniChar = head

    /**
     * @see Stream#tail()
     */
    override fun tail(): ByteStream = ByteStream(this.input, head.offset)

    /**
     * @see Stream#isEmpty()
     */
    override fun isEmpty(): Boolean = input.size <= offset

    /**
     * @see Stream#isNotEmpty()
     */
    override fun isNotEmpty(): Boolean = !isEmpty()

    /**
     * @see Stream#push(head)
     */
    override fun push(head: UniChar): Stream = PushedStream(head, this)

    /**
     * @see Stream#codes()
     */
    override fun codes(): IntArray {
        val destination = ArrayList<Int>()

        var current = this
        while (current.isNotEmpty()) {
            destination.add(current.head.code)
            current = current.tail()
        }

        return destination.toIntArray()
    }

    /**
     *  Checks whether there are fewer than n bytes left to read.
     */
    private fun hasFewerThan(offset: Int, n: Int, bytes: ByteArray) = bytes.size - offset < n

    /**
     * UTF-32 decoding
     */

    /**
     * Decodes a UTF-32LE bytes stream to Unicode chars.
     */
    private fun undoUTF32LE(bytes: ByteArray, offset: Int): UniChar {
        if (hasFewerThan(offset, 4, bytes)) {
            throw IllegalArgumentException("UTF-32BE input contains invalid number of bytes")
        }

        val first = bytes[offset].toUnsignedInt()
        val second = bytes[offset + 1].toUnsignedInt()
        val third = bytes[offset + 2].toUnsignedInt()
        val fourth = bytes[offset + 3].toUnsignedInt()


        return UniChar(offset + 4, first + 256 * (second + 256 * (third + 256 * fourth)))
    }

    /**
     * Decodes a UTF-32BE bytes stream to Unicode chars.
     */
    private fun undoUTF32BE(bytes: ByteArray, offset: Int): UniChar {
        if (hasFewerThan(offset, 4, bytes)) {
            throw IllegalArgumentException("UTF-32BE input contains invalid number of bytes")
        }

        val first = bytes[offset].toUnsignedInt()
        val second = bytes[offset + 1].toUnsignedInt()
        val third = bytes[offset + 2].toUnsignedInt()
        val fourth = bytes[offset + 3].toUnsignedInt()

        return UniChar(offset + 4, fourth + 256 * (third + 256 * (second + 256 * first)))
    }

    /**
     * UTF-16 decoding
     */

    /**
     * Copied from the Unicode FAQs.
     */
    private val surrogateOffset = 0x10000 - (0xD800 * 1024) - 0xDC00

    /**
     * Combines two UTF-16 surrogates into a single Unicode character.
     */
    private fun combineSurrogates(lead: Int, trail: Int): Int = (lead * 1024 + trail + surrogateOffset)

    /**
     * Converts each pair of UTF-16 surrogate characters to a single Unicode character.
     */
    private fun combinePairs(head: UniChar, bytes: ByteArray): UniChar {
        return when {
            0xD800 <= head.code && head.code <= 0xDBFF -> combineLead(head, bytes, head.offset)
            0xDC00 <= head.code && head.code <= 0xDFFF -> throw IllegalArgumentException("UTF-16 contains trail surrogate without lead surrogate")
            else -> head
        }
    }

    /**
     * Combines the lead surrogate with the head of the rest of the input chars,
     * assumed to be a trail surrogate, and continues combining surrogate pairs.
     */
    private fun combineLead(lead: UniChar, bytes: ByteArray, offset: Int): UniChar {
        if (bytes.size <= offset) {
            throw IllegalArgumentException("UTF-16 contains lead surrogate as final character")
        }

        val tail = undoUTF32BE(bytes, offset)
        val tailChar = tail.code

        if (0xDC00 <= tail.code && tail.code <= 0xDFFF) {
            return UniChar(tail.offset, combineSurrogates(lead.code, tailChar))
        }
        throw IllegalArgumentException("UTF-16 contains lead surrogate without trail surrogate")
    }

    /**
     * Decodes a UTF-16LE bytes stream to Unicode chars.
     */
    private fun undoUTF16LE(bytes: ByteArray, offset: Int): UniChar {
        if (hasFewerThan(offset, 2, bytes)) {
            throw IllegalArgumentException("UTF-16LE input contains odd number of bytes")
        }
        val high = bytes[offset].toUnsignedInt()
        val low = bytes[offset + 1].toUnsignedInt()

        return UniChar(offset + 2, high + low * 256)
    }

    /**
     * Decodes a UTF-16BE bytes stream to Unicode chars.
     */
    private fun undoUTF16BE(bytes: ByteArray, offset: Int): UniChar {
        if (hasFewerThan(offset, 2, bytes)) {
            throw IllegalArgumentException("UTF-16BE input contains odd number of bytes")
        }
        val high = bytes[offset].toUnsignedInt()
        val low = bytes[offset + 1].toUnsignedInt()

        return UniChar(offset + 2, low + high * 256)
    }

    /**
     * UTF-8 decoding
     */

    /**
     *  Decodes a UTF-8 bytes stream to Unicode chars.
     */
    private fun undoUTF8(bytes: ByteArray, offset: Int): UniChar {
        if (hasFewerThan(offset, 1, bytes)) {
            throw IllegalArgumentException("UTF-8 input contains invalid number of bytes")
        }

        val first = bytes[offset].toUnsignedInt()

        return when {
            first < 0x80 -> UniChar(offset + 1, first)
            first < 0xC0 -> throw IllegalArgumentException("UTF-8 input contains invalid first byte")
            first < 0xE0 -> decodeTwoUTF8(first, offset + 1, bytes)
            first < 0xF0 -> decodeThreeUTF8(first, offset + 1, bytes)
            first < 0xF8 -> decodeFourUTF8(first, offset + 1, bytes)
            else -> throw IllegalArgumentException("UTF-8 input contains invalid first byte")
        }
    }

    /**
     * Decodes a two-byte UTF-8 character,
     * where the first byte is already available and the second is the head of
     * the bytes, and then continues to undo the UTF-8 encoding.
     */
    private fun decodeTwoUTF8(first: Int, offset: Int, bytes: ByteArray): UniChar {
        if (hasFewerThan(offset, 1, bytes)) {
            throw IllegalArgumentException("UTF-8 double byte char is missing second byte at eof")
        }

        val second = bytes[offset].toUnsignedInt()

        return when {
            second < 0x80 || 0xBF < second -> throw IllegalArgumentException("UTF-8 triple byte char has invalid second byte")
            else -> UniChar(offset + 1, combineTwoUTF8(first, second))
        }
    }

    /**
     * Combines the first and second bytes of a two-byte UTF-8 char into a single Unicode char.
     */
    private fun combineTwoUTF8(first: Int, second: Int) =
            (first - 0xC0) * 64 +
                    (second - 0x80)

    /**
     * Decodes a three-byte UTF-8 character,
     * where the first byte is already available and the second and third are the
     * head of the bytes, and then continues to undo the UTF-8 encoding.
     */
    private fun decodeThreeUTF8(first: Int, offset: Int, bytes: ByteArray): UniChar {
        if (hasFewerThan(offset, 2, bytes)) {
            throw IllegalArgumentException("UTF-8 triple byte char is missing bytes at eof")
        }

        val second = bytes[offset].toUnsignedInt()
        val third = bytes[offset + 1].toUnsignedInt()

        return when {
            second < 0x80 || 0xBF < second -> throw IllegalArgumentException("UTF-8 triple byte char has invalid second byte")
            third < 0x80 || 0xBF < third -> throw IllegalArgumentException("UTF-8 triple byte char has invalid third byte")
            else -> UniChar(offset + 2, combineThreeUTF8(first, second, third))
        }
    }

    /**
     * Combines the first, second and third bytes of a three-byte UTF-8 char into a single Unicode char.
     */
    private fun combineThreeUTF8(first: Int, second: Int, third: Int) =
            (first - 0xE0) * 4096 +
                    (second - 0x80) * 64 +
                    (third - 0x80)

    /**
     * Decodes a four-byte UTF-8 character, where the first byte is already available and the second, third and fourth
     * are the head of the bytes, and then continues to undo the UTF-8 encoding.
     */
    private fun decodeFourUTF8(first: Int, offset: Int, bytes: ByteArray): UniChar {
        if (hasFewerThan(offset, 3, bytes)) {
            throw IllegalArgumentException("UTF-8 quad byte char is missing bytes at eof")
        }

        val second = bytes[offset].toUnsignedInt()
        val third = bytes[offset + 1].toUnsignedInt()
        val fourth = bytes[offset + 2].toUnsignedInt()

        return when {
            second < 0x80 || 0xBF < second -> throw IllegalArgumentException("UTF-8 quad byte char has invalid second byte")
            third < 0x80 || 0xBF < third -> throw IllegalArgumentException("UTF-8 quad byte char has invalid third byte")
            fourth < 0x80 || 0xBF < fourth -> throw IllegalArgumentException("UTF-8 quad byte char has invalid fourth byte")
            else -> UniChar(offset + 3, combineFourUTF8(first, second, third, fourth))
        }
    }

    /**
     * Combines the first, second, third and fourth bytes of a four-byte UTF-8 char into a single Unicode char.
     */
    private fun combineFourUTF8(first: Int, second: Int, third: Int, fourth: Int) =
            (first - 0xF0) * 262144 +
                    (second - 0x80) * 4096 +
                    (third - 0x80) * 64 +
                    (fourth - 0x80)
}

/**
 * A PushedStream adds the given decoded character as head to the given Stream.
 */
class PushedStream(private val head: UniChar, private val tail: Stream) : Stream {
    /**
     * @see Stream#encoding()
     */
    override fun encoding(): Encoding = tail.encoding()

    /**
     * @see Stream#head()
     */
    override fun head(): UniChar = head

    /**
     * @see Stream#tail()
     */
    override fun tail(): Stream = tail

    /**
     * @see Stream#isEmpty()
     */
    override fun isEmpty(): Boolean = false

    /**
     * @see Stream#isNotEmpty()
     */
    override fun isNotEmpty(): Boolean = true

    /**
     * @see Stream#push(head)
     */
    override fun push(head: UniChar): Stream = PushedStream(head, this)

    /**
     * @see Stream#codes()
     */
    override fun codes(): IntArray = intArrayOf(head.code, *tail.codes())
}

/**
 * Copies the byte in an Int and returns the int representation of it.
 */
private fun Byte.toUnsignedInt(): Int = this.toInt() and 0xFF
