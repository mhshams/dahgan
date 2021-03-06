package io.dahgan.stream

import java.util.*

/**
 * Wraps the given byte arrays, detects its encoding and finally converts it to characters.
 */
class ByteStream(private val input: ByteArray, private val offset: Int = 0) : Stream {

    companion object {
        private const val X00 = 0x00.toByte()
        private const val XFE = 0xFE.toByte()
        private const val XFF = 0xFF.toByte()
        private const val XEF = 0xEF.toByte()
        private const val XBB = 0xBB.toByte()
        private const val XBF = 0xBF.toByte()

        private val decoders = mapOf(
            Encoding.UTF8 to UTF8Decoder(),
            Encoding.UTF16LE to UTF16Decoder.le(),
            Encoding.UTF16BE to UTF16Decoder.be(),
            Encoding.UTF32LE to UTF32Decoder.le(),
            Encoding.UTF32BE to UTF32Decoder.be()
        )
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
        decoders[encoding]!!.decode(input, offset)
    }

    /**
     * @see Stream#encoding()
     */
    override fun encoding() = encoding

    /**
     * @see Stream#head()
     */
    override fun head() = head

    /**
     * @see Stream#tail()
     */
    override fun tail() = ByteStream(this.input, head.offset)

    /**
     * @see Stream#isEmpty()
     */
    override fun isEmpty() = input.size <= offset

    /**
     * @see Stream#isNotEmpty()
     */
    override fun isNotEmpty() = !isEmpty()

    /**
     * @see Stream#push(head)
     */
    override fun push(head: UniChar) = PushedStream(head, this)

    /**
     * @see Stream#codes()
     */
    override fun codes() = ArrayList<Int>().apply {

        var current = this@ByteStream

        while (current.isNotEmpty()) {
            add(current.head.code)
            current = current.tail()
        }

    }.toIntArray()
}

/**
 * A PushedStream adds the given decoded character as head to the given Stream.
 */
class PushedStream(private val head: UniChar, private val tail: Stream) : Stream {
    /**
     * @see Stream#encoding()
     */
    override fun encoding() = tail.encoding()

    /**
     * @see Stream#head()
     */
    override fun head() = head

    /**
     * @see Stream#tail()
     */
    override fun tail() = tail

    /**
     * @see Stream#isEmpty()
     */
    override fun isEmpty() = false

    /**
     * @see Stream#isNotEmpty()
     */
    override fun isNotEmpty() = true

    /**
     * @see Stream#push(head)
     */
    override fun push(head: UniChar) = PushedStream(head, this)

    /**
     * @see Stream#codes()
     */
    override fun codes() = intArrayOf(head.code, *tail.codes())
}
