package io.dahgan.stream

/**
 * Decodes a UTF-32 (LE or BE) byte array to unicode characters.
 */
class UTF32Decoder
private constructor(val combine: (Int, Int, Int, Int) -> Int) : Decoder {

    companion object {
        /**
         * Decodes a UTF-32BE byte array to unicode characters.
         *
         * Combine function combines two bytes of a UTF-32BE character and returns the result.
         */
        fun be(): UTF32Decoder = UTF32Decoder { first, second, third, fourth ->
            fourth + 256 * (third + 256 * (second + 256 * first))
        }

        /**
         * Decodes a UTF-32LE byte array to unicode characters.
         *
         * Combine function combines two bytes of a UTF-32LE character and returns the result.
         */
        fun le(): UTF32Decoder = UTF32Decoder { first, second, third, fourth ->
            first + 256 * (second + 256 * (third + 256 * fourth))
        }
    }

    /**
     * @see Decoder#decode
     */
    override fun decode(bytes: ByteArray, offset: Int): UniChar {
        if (hasFewerThan(offset, 4, bytes)) {
            throw IllegalArgumentException("UTF-32 input contains invalid number of bytes")
        }

        val first = bytes[offset].toUnsignedInt()
        val second = bytes[offset + 1].toUnsignedInt()
        val third = bytes[offset + 2].toUnsignedInt()
        val fourth = bytes[offset + 3].toUnsignedInt()

        return UniChar(offset + 4, combine(first, second, third, fourth))
    }
}

/**
 * Decodes a UTF-16 (LE or BE) byte array to unicode characters.
 */
class UTF16Decoder
private constructor(val combine: (Int, Int) -> Int) : Decoder {

    companion object {
        /**
         * Decodes a UTF-16BE byte array to unicode characters.
         *
         * Combine function combines two bytes of a UTF-16BE character and returns the result.
         */
        fun be(): UTF16Decoder = UTF16Decoder { first, second -> second + first * 256 }

        /**
         * Decodes a UTF-16LE byte array to unicode characters.
         *
         * Combine function combines two bytes of a UTF-16LE character and returns the result.
         */
        fun le(): UTF16Decoder = UTF16Decoder { first, second -> first + second * 256 }
    }

    /**
     * Copied from the unicode FAQs.
     */
    private val surrogateOffset = 0x10000 - (0xD800 * 1024) - 0xDC00

    /**
     * @see Decoder#decode
     */
    override fun decode(bytes: ByteArray, offset: Int): UniChar {
        val head = undo(bytes, offset)
        return when {
            head.code in 0xD800..0xDBFF -> combineLead(head, bytes, head.offset)
            head.code in 0xDC00..0xDFFF -> throw IllegalArgumentException("UTF-16 contains trail surrogate without lead surrogate")
            else -> head
        }
    }

    /**
     * Decodes a UTF-16 (LE or BE) byte array to a unicode char.
     */
    private fun undo(bytes: ByteArray, offset: Int): UniChar {
        if (hasFewerThan(offset, 2, bytes)) {
            throw IllegalArgumentException("UTF-16 input contains odd number of bytes")
        }
        val first = bytes[offset].toUnsignedInt()
        val second = bytes[offset + 1].toUnsignedInt()

        return UniChar(offset + 2, combine(first, second))
    }

    /**
     * Combines two UTF-16 surrogates into a single unicode character.
     */
    private fun combineSurrogates(lead: Int, trail: Int): Int = lead * 1024 + trail + surrogateOffset

    /**
     * Combines the lead surrogate with the head of the rest of the input characters,
     * assumed to be a trail surrogate, and continues combining surrogate pairs.
     */
    private fun combineLead(lead: UniChar, bytes: ByteArray, offset: Int): UniChar {
        if (hasFewerThan(offset, 2, bytes)) {
            throw IllegalArgumentException("UTF-16 contains lead surrogate as final character")
        }

        val tail = undo(bytes, offset)
        val tailChar = tail.code

        if (tail.code in 0xDC00..0xDFFF) {
            return UniChar(tail.offset, combineSurrogates(lead.code, tailChar))
        }
        throw IllegalArgumentException("UTF-16 contains lead surrogate without trail surrogate")
    }
}

/**
 *  Decodes a UTF-8 byte array to unicode characters.
 */
class UTF8Decoder : Decoder {

    /**
     * @see Decoder#decode
     */
    override fun decode(bytes: ByteArray, offset: Int): UniChar {
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
     * Combines the first and second bytes of a two-byte UTF-8 char into a single unicode char.
     */
    private fun combineTwoUTF8(first: Int, second: Int) =
        (first - 0xC0) * 64 + (second - 0x80)

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
     * Combines the first, second and third bytes of a three-byte UTF-8 char into a single unicode char.
     */
    private fun combineThreeUTF8(first: Int, second: Int, third: Int) =
        (first - 0xE0) * 4096 + (second - 0x80) * 64 + (third - 0x80)

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
     * Combines the first, second, third and fourth bytes of a four-byte UTF-8 char into a single unicode char.
     */
    private fun combineFourUTF8(first: Int, second: Int, third: Int, fourth: Int) =
        (first - 0xF0) * 262144 + (second - 0x80) * 4096 + (third - 0x80) * 64 + (fourth - 0x80)
}

/**
 *  Checks whether there are fewer than n bytes left to read.
 */
private fun hasFewerThan(offset: Int, n: Int, bytes: ByteArray) = bytes.size - offset < n

/**
 * Copies the byte in an Int and returns the int representation of it.
 */
private fun Byte.toUnsignedInt(): Int = this.toInt() and 0xFF
