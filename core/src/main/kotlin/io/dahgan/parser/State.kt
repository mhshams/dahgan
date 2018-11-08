package io.dahgan.parser

import io.dahgan.stream.Stream

/**
 * Parsing state
 */
data class State(
    /**
     * The input name for error messages.
     */
    val name: String,

    /**
     * The decoded input Stream.
     */
    val input: Stream,

    /**
     * Current decision name.
     */
    val decision: String,

    /**
     * Lookahead characters limit.
     */
    val limit: Int,

    /**
     * Pattern we must not enter into.
     */
    val forbidden: Parser?,

    /**
     * Disables token generation.
     */
    val isPeek: Boolean,

    /**
     * Is at start of line?
     */
    val isSol: Boolean,

    /**
     * (Reversed) characters collected for a token.
     */
    val chars: IntArray,

    /**
     * Byte offset of first collected character.
     */
    val charsByteOffset: Int,

    /**
     * Char offset of first collected character.
     */
    val charsCharOffset: Int,

    /**
     * Line of first collected character.
     */
    val charsLine: Int,

    /**
     * Character in line of first collected character.
     */
    val charsLineChar: Int,

    /**
     * Offset in bytes in the input.
     */
    val byteOffset: Int,

    /**
     * Offset in characters in the input.
     */
    val charOffset: Int,

    /**
     * Builds on YAML's line break definition.
     */
    val line: Int,

    /**
     * Character number in line.
     */
    val lineChar: Int,

    /**
     * Of token we are collecting chars for.
     */
    val code: Code,

    /**
     * Last matched character.
     */
    val last: Int,

    /**
     * The replies that are stored for future use.
     */
    val yields: MutableMap<String, Any>
) {
    override fun toString() = name
}
