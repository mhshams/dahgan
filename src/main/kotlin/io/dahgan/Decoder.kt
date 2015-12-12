package io.dahgan

/**
 * UTF decoding
 *
 * TODO: needs to be lazy (sequence maybe)
 */


/**
 * Recognized Unicode encodings. As of YAML 1.2 UTF-32 is also required.
 */
enum class Encoding(val text: String) {
    UTF8("UTF-8"),
    UTF16LE("UTF-16LE"),
    UTF16BE("UTF-16BE"),
    UTF32LE("UTF-32LE"),
    UTF32BE("UTF-32BE");

    override fun toString() = text
}

/**
 * Represents a unicode character and its ending offset in the input.
 */
data class UniChar(val offset: Int, val code: Int)

/**
 * Automatically detects the 'Encoding' used and converts the
 * bytes to Unicode characters, with byte offsets. Note the offset is for
 * past end of the character, not its beginning.
 */
fun decode(input: ByteArray): List<UniChar> = undoEncoding(detectEncoding(input), input)

private val X00 = 0x00.toByte()
private val XFE = 0xFE.toByte()
private val XFF = 0xFF.toByte()
private val XEF = 0xEF.toByte()
private val XBB = 0xBB.toByte()
private val XBF = 0xBF.toByte()

/**
 * Examines the first few chars (bytes) of the text
 * to deduce the Unicode encoding used according to the YAML spec.
 */
fun detectEncoding(input: ByteArray): Encoding = when {
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

/**
 * Converts a bytes stream to Unicode characters according to the encoding.
 */
private fun undoEncoding(encoding: Encoding, bytes: ByteArray) = when (encoding) {
    Encoding.UTF8 -> undoUTF8(bytes, 0)
    Encoding.UTF16LE -> combinePairs(undoUTF16LE(bytes, 0))
    Encoding.UTF16BE -> combinePairs(undoUTF16BE(bytes, 0))
    Encoding.UTF32LE -> undoUTF32LE(bytes, 0)
    Encoding.UTF32BE -> undoUTF32BE(bytes, 0)
}

/**
 * UTF-32 decoding
 */

/**
 *  Checks whether there are fewer than n bytes left to read.
 */
private fun hasFewerThan(n: Int, bytes: ByteArray) = bytes.size < n

/**
 * Decodes a UTF-32LE bytes stream to Unicode chars.
 */
private fun undoUTF32LE(bytes: ByteArray, offset: Int): List<UniChar> {
    if (bytes.isEmpty()) {
        return emptyList()
    }
    if (hasFewerThan(4, bytes)) {
        throw IllegalArgumentException("UTF-32LE input contains invalid number of bytes")
    }
    val first = bytes[0].toUnsignedInt()
    val second = bytes[1].toUnsignedInt()
    val third = bytes[2].toUnsignedInt()
    val fourth = bytes[3].toUnsignedInt()
    val rest = bytes.copyOfRange(4, bytes.lastIndex + 1)

    val chr = (first + 256 * (second + 256 * (third + 256 * (fourth))))

    return listOf(UniChar(offset + 4, chr)) + undoUTF32LE(rest, offset + 4)
}

/**
 * Decodes a UTF-32BE bytes stream to Unicode chars.
 */
private fun undoUTF32BE(bytes: ByteArray, offset: Int): List<UniChar> {
    if (bytes.isEmpty()) {
        return emptyList()
    }
    if (hasFewerThan(4, bytes)) {
        throw IllegalArgumentException("UTF-32BE input contains invalid number of bytes")
    }
    val first = bytes[0].toUnsignedInt()
    val second = bytes[1].toUnsignedInt()
    val third = bytes[2].toUnsignedInt()
    val fourth = bytes[3].toUnsignedInt()
    val rest = bytes.copyOfRange(4, bytes.lastIndex + 1)

    val chr = (fourth + 256 * (third + 256 * (second + 256 * (first))))

    return listOf(UniChar(offset + 4, chr)) + undoUTF32BE(rest, offset + 4)
}

/**
 * UTF-16 decoding
 */

/**
 * Converts each pair of UTF-16 surrogate characters to a single Unicode character.
 */
private fun combinePairs(pairs: List<UniChar>): List<UniChar> {
    if (pairs.isEmpty()) {
        return emptyList()
    }
    val head = pairs.first()
    val tail = pairs.subList(1, pairs.lastIndex + 1)

    return when {
        0xD800 <= head.code && head.code <= 0xDBFF -> combineLead(head, tail)
        0xDC00 <= head.code && head.code <= 0xDFFF -> throw IllegalArgumentException("UTF-16 contains trail surrogate without lead surrogate")
        else -> listOf(head) + combinePairs(tail)
    }
}

/**
 * Combines the lead surrogate with the head of the rest of the input chars,
 * assumed to be a trail surrogate, and continues combining surrogate pairs.
 */
private fun combineLead(lead: UniChar, rest: List<UniChar>): List<UniChar> {
    if (rest.isEmpty()) {
        throw IllegalArgumentException("UTF-16 contains lead surrogate as final character")
    }
    val leadChar = lead.code
    val tailChar = rest.first().code

    if (0xDC00 <= tailChar && tailChar <= 0xDFFF) {
        return listOf(UniChar(rest.first().offset, combineSurrogates(leadChar, tailChar))) +
                combinePairs(rest.subList(1, rest.lastIndex + 1))
    }
    throw IllegalArgumentException("UTF-16 contains lead surrogate without trail surrogate")
}

/**
 * Copied from the Unicode FAQs.
 */
private val surrogateOffset = 0x10000 - (0xD800 * 1024) - 0xDC00

/**
 * Combines two UTF-16 surrogates into a single Unicode character.
 */
private fun combineSurrogates(lead: Int, trail: Int): Int = (lead * 1024 + trail + surrogateOffset)

/**
 * Decodes a UTF-16LE bytes stream to Unicode chars.
 */
private fun undoUTF16LE(bytes: ByteArray, offset: Int): List<UniChar> {
    if (bytes.isEmpty()) {
        return emptyList()
    }
    if (hasFewerThan(2, bytes)) {
        throw IllegalArgumentException("UTF-16LE input contains odd number of bytes")
    }
    val low = bytes[0].toUnsignedInt()
    val high = bytes[1].toUnsignedInt()
    val rest = bytes.copyOfRange(2, bytes.lastIndex + 1)

    return listOf(UniChar(offset + 2, (low + high * 256))) + undoUTF16LE(rest, offset + 2)
}

/**
 * Decodes a UTF-16BE bytes stream to Unicode chars.
 */
private fun undoUTF16BE(bytes: ByteArray, offset: Int): List<UniChar> {
    if (bytes.isEmpty()) {
        return emptyList()
    }
    if (hasFewerThan(2, bytes)) {
        throw IllegalArgumentException("UTF-16BE input contains odd number of bytes")
    }
    val high = bytes[0].toUnsignedInt()
    val low = bytes[1].toUnsignedInt()
    val rest = bytes.copyOfRange(2, bytes.lastIndex + 1)

    return listOf(UniChar(offset + 2, (low + high * 256))) + undoUTF16BE(rest, offset + 2)
}

/**
 * UTF-8 decoding
 */

/**
 *  Decodes a UTF-8 bytes stream to Unicode chars.
 */
private fun undoUTF8(bytes: ByteArray, offset: Int): List<UniChar> {
    if (bytes.isEmpty()) {
        return emptyList()
    }

    val first = bytes[0].toUnsignedInt()
    val rest = if (bytes.size > 1) bytes.copyOfRange(1, bytes.lastIndex + 1) else ByteArray(0)

    return when {
        first < 0x80 -> listOf(UniChar(offset + 1, first)) + undoUTF8(rest, offset + 1)
        first < 0xC0 -> throw IllegalArgumentException("UTF-8 input contains invalid first byte")
        first < 0xE0 -> decodeTwoUTF8(first, offset, rest)
        first < 0xF0 -> decodeThreeUTF8(first, offset, rest)
        first < 0xF8 -> decodeFourUTF8(first, offset, rest)
        else -> throw IllegalArgumentException("UTF-8 input contains invalid first byte")
    }
}

/**
 * Decodes a two-byte UTF-8 character,
 * where the first byte is already available and the second is the head of
 * the bytes, and then continues to undo the UTF-8 encoding.
 */
private fun decodeTwoUTF8(first: Int, offset: Int, bytes: ByteArray): List<UniChar> {
    if (bytes.isEmpty()) {
        throw IllegalArgumentException("UTF-8 double byte char is missing second byte at eof")
    }

    val second = bytes[0].toUnsignedInt()
    val rest = bytes.copyOfRange(1, bytes.lastIndex + 1)

    return when {
        second < 0x80 || 0xBF < second -> throw IllegalArgumentException("UTF-8 triple byte char has invalid second byte")
        else -> listOf(UniChar(offset + 2, combineTwoUTF8(first, second))) + undoUTF8(rest, offset + 2)
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
private fun decodeThreeUTF8(first: Int, offset: Int, bytes: ByteArray): List<UniChar> {
    if (hasFewerThan(2, bytes)) {
        throw IllegalArgumentException("UTF-8 triple byte char is missing bytes at eof")
    }

    val second = bytes[0].toUnsignedInt()
    val third = bytes[1].toUnsignedInt()
    val rest = bytes.copyOfRange(2, bytes.lastIndex + 1)

    return when {
        second < 0x80 || 0xBF < second -> throw IllegalArgumentException("UTF-8 triple byte char has invalid second byte")
        third < 0x80 || 0xBF < third -> throw IllegalArgumentException("UTF-8 triple byte char has invalid third byte")
        else -> listOf(UniChar(offset + 3, combineThreeUTF8(first, second, third))) + undoUTF8(rest, offset + 3)
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
private fun decodeFourUTF8(first: Int, offset: Int, bytes: ByteArray): List<UniChar> {
    if (hasFewerThan(3, bytes)) {
        throw IllegalArgumentException("UTF-8 quad byte char is missing bytes at eof")
    }

    val second = bytes[0].toUnsignedInt()
    val third = bytes[1].toUnsignedInt()
    val fourth = bytes[2].toUnsignedInt()
    val rest = bytes.copyOfRange(3, bytes.lastIndex + 1)

    return when {
        second < 0x80 || 0xBF < second -> throw IllegalArgumentException("UTF-8 quad byte char has invalid second byte")
        third < 0x80 || 0xBF < third -> throw IllegalArgumentException("UTF-8 quad byte char has invalid third byte")
        fourth < 0x80 || 0xBF < fourth -> throw IllegalArgumentException("UTF-8 quad byte char has invalid fourth byte")
        else -> listOf(UniChar(offset + 4, combineFourUTF8(first, second, third, fourth))) + undoUTF8(rest, offset + 4)
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

/**
 * Copies the byte in an Int and returns the int representation of it.
 */
private fun Byte.toUnsignedInt(): Int = this.toInt() and 0xFF
