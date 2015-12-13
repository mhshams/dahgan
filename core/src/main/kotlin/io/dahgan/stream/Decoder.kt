package io.dahgan.stream

/**
 * A Decoder decodes a stream of unicode characters.
 */
interface Decoder {
    /**
     * Decodes a single unicode character in the given offset of given byte array.
     */
    fun decode(bytes: ByteArray, offset: Int): UniChar
}

/**
 * Represents a unicode character and its ending offset in the input stream.
 *
 * @param offset the ending offset of character in the stream
 * @param code the character code
 */
data class UniChar(val offset: Int, val code: Int)
