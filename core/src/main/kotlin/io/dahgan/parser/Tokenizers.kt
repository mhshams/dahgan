package io.dahgan.parser

import io.dahgan.stream.Stream
import java.util.*

/**
 * 'Tokenizer' converts a (named) input text into a list of 'Token'. Errors
 * are reported as tokens with the Error 'Code', and the unparsed text
 * following an error may be attached as a final token (if the withFollowing is true).
 */
interface Tokenizer {
    fun tokenize(name: String, input: ByteArray, withFollowing: Boolean): Sequence<Token>
}

/**
 * Converts the pattern to a simple 'Tokenizer'.
 */
class PatternTokenizer(val pattern: Parser) : Tokenizer {

    override fun tokenize(name: String, input: ByteArray, withFollowing: Boolean): Sequence<Token> {

        fun patternParser(parser: Parser, state: State): Sequence<Token> {
            val reply = parser(state)
            val tokens = commitBugs(reply)
            val rState = reply.state

            return when (reply.result) {
                is Result.Failed -> errorTokens(tokens, rState, reply.result.message as String, withFollowing)
                is Result.Completed -> tokens
                is Result.More -> tokens + patternParser(reply.result.result, rState)
            }
        }

        return patternParser(wrap(pattern), initialState(name, input))
    }
}

/**
 * Converts the parser returning parser to a
 * simple 'Tokenizer' (only used for tests). The result is reported as a token
 * with the Detected 'Code' The result is reported as a token with the Detected 'Code'.
 */
class ParserTokenizer(val what: String, val parser: Parser) : Tokenizer {

    override fun tokenize(name: String, input: ByteArray, withFollowing: Boolean): Sequence<Token> {

        fun parserParser(parser: Parser, state: State): Sequence<Token> {
            val reply = parser(state)
            val tokens = commitBugs(reply)
            val rState = reply.state

            return when (reply.result) {
                is Result.Failed -> errorTokens(tokens, rState, reply.result.message as String, withFollowing)
                is Result.Completed -> tokens + Token(rState.byteOffset, rState.charOffset, rState.line,
                        rState.lineChar, Code.Detected, TextWrapper.of("$what=${reply.result.result}"))
                is Result.More -> tokens + parserParser(reply.result.result, rState)
            }
        }

        return parserParser(wrap(parser), initialState(name, input))
    }
}

/**
 * Returns an initial 'State' for parsing the input (with name for error messages).
 */
fun initialState(name: String, input: ByteArray): State = State(name, Stream.of(input), "", -1, null, false, true,
        intArrayOf(), -1, -1, -1, -1, 0, 0, 1, 0, Code.Unparsed, ' '.toInt(), HashMap())
