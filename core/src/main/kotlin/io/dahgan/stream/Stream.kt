package io.dahgan.stream

/**
 * A Stream wraps the given input (normal byte array) and decodes it lazily, on demand.
 */
interface Stream {
    companion object {
        /**
         * Creates a Stream with given input.
         */
        fun of(input: ByteArray): Stream = ByteStream(input)

        /**
         * Creates an empty Stream
         */
        fun empty(): Stream = ByteStream(byteArrayOf())
    }

    /**
     * Returns the encoding of the input.
     */
    fun encoding(): Encoding

    /**
     * Returns the current head of the input.
     */
    fun head(): UniChar

    /**
     * Returns tail Stream of current Stream.
     */
    fun tail(): Stream

    /**
     * Returns true of the Stream is empty, false otherwise.
     */
    fun isEmpty(): Boolean

    /**
     * Returns true of the Stream is not empty, false otherwise.
     */
    fun isNotEmpty(): Boolean

    /**
     * Pushes an already decoded character in to head of current Stream.
     */
    fun push(head: UniChar): Stream

    /**
     * Returns all available characters in the Stream
     */
    fun codes(): IntArray
}

