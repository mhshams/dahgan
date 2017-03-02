package io.dahgan.parser

/**
 * Result tokens
 *
 * The parsing result is a stream of tokens rather than a parse tree. The idea is to
 * convert the YAML input into byte codes. These byte codes are intended to be written
 * into a byte codes file (or more likely a UNIX pipe) for further processing.
 */

/**
 * Parsed token.
 */
data class Token(
        /**
         * 0-base byte offset in stream.
         */
        val byteOffset: Int,

        /**
         * 0-base character offset in stream.
         */
        val charOffset: Int,

        /**
         * 1-based line number.
         */
        val line: Int,

        /**
         * 0-based character in line.
         */
        val lineChar: Int,

        /**
         * Specific token 'Code'.
         */
        val code: Code,

        /**
         * Contained input chars, if any.
         */
        val text: Escapable
) {


    /**
     * Converts a 'Token' to two YEAST lines: a comment with the position numbers and the actual token line.
     */
    override fun toString() = "# B: $byteOffset, C: $charOffset, L: $line, c: $lineChar\n$code$text\n"
}

/**
 * A container to keep the input, as normal text or array of codes, and lazily escape them if needed.
 */
sealed class Escapable {
    companion object {
        fun of(text: IntArray): Escapable = Code(text)

        fun of(text: String): Escapable = Text(text)
    }

    class Code(val codes: IntArray) : Escapable() {
        override fun toString(): String = escape(codes, "")
    }

    class Text(val text: String) : Escapable() {
        override fun toString(): String = text
    }
}


/**
 * Escapes the given character (code) if needed.
 */
fun escape(code: Int): String = when {
    ' '.toInt() <= code && code != '\\'.toInt() && code <= '~'.toInt() -> "${code.toChar()}"
    code <= 0xFF -> "\\x${toHex(2, code)}"
    code in 256..0xFFFF -> "\\u${toHex(4, code)}"
    else -> "\\U${toHex(8, code)}"
}

/**
 * Escapes all the non-ASCII characters in the given text, as well as escaping
 * the \\ character, using the \\xXX, \\uXXXX and \\UXXXXXXXX escape sequences.
 */
fun escape(text: IntArray, separator: String = ", "): String = text.map(::escape).joinToString(separator)

/**
 * Converts the int to the specified number of hexadecimal digits.
 */
fun toHex(digits: Int, n: Int): String =
        if (digits == 1) "${intToDigit(n)}" else "${toHex(digits - 1, n / 16)}${intToDigit(n % 16)}"

fun intToDigit(n: Int): Char = if (n < 10) (48 + n).toChar() else (87 + n).toChar()

/**
 * Converts a list of tokens to a multi-line YEAST text.
 */
fun showTokens(tokens: Sequence<Token>): String = tokens.fold("") { text, token -> text + token.toString() }
