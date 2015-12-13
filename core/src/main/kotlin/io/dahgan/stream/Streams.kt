package io.dahgan.stream

import java.util.*

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
