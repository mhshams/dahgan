package io.dahgan.parser

import io.dahgan.stream.Stream
import java.util.*

/**
 * 'Tokenizer' converts a (named) input text into a list of 'Token'.
 * Errors are reported as tokens with the Error 'Code', and the unparsed text
 * following an error may be attached as a final token (if the withFollowing is true).
 */
interface Tokenizer {
    fun tokenize(name: String, input: ByteArray, withFollowing: Boolean): List<Token>
}

/**
 * Converts the pattern to a simple 'Tokenizer'.
 */
class PatternTokenizer(private val pattern: Parser) : Tokenizer {

    override fun tokenize(name: String, input: ByteArray, withFollowing: Boolean): List<Token> {

        fun patternParser(parser: Parser, state: State): List<Token> {
            val reply = parser(state)
            val tokens = commitBugs(reply)
            val rState = reply.state

            return when (reply.result) {
                is Result.Failed ->
                    errorTokens(
                        tokens = tokens,
                        state = rState,
                        message = reply.result.message as String,
                        flag = withFollowing
                    )
                is Result.Completed ->
                    tokens
                is Result.More ->
                    tokens + patternParser(reply.result.result, rState)
            }
        }

        return patternParser(wrap(pattern), initialState(name, input))
    }
}

/**
 * Converts the parser returning parser to a simple 'Tokenizer' (only used for tests).
 * The result is reported as a token with the Detected 'Code'.
 */
class ParserTokenizer(private val what: String, val parser: Parser) : Tokenizer {

    override fun tokenize(name: String, input: ByteArray, withFollowing: Boolean): List<Token> {

        fun parserParser(parser: Parser, state: State): List<Token> {

            val reply = parser(state)
            val tokens = commitBugs(reply)
            val rState = reply.state

            return when (reply.result) {
                is Result.Failed ->
                    errorTokens(
                        tokens = tokens,
                        state = rState,
                        message = reply.result.message as String,
                        flag = withFollowing
                    )
                is Result.Completed ->
                    tokens +
                            Token(
                                byteOffset = rState.byteOffset,
                                charOffset = rState.charOffset,
                                line = rState.line,
                                lineChar = rState.lineChar,
                                code = Code.Detected,
                                text = Escapable.of("$what=${reply.result.result}")
                            )
                is Result.More ->
                    tokens + parserParser(reply.result.result, rState)
            }
        }

        return parserParser(wrap(parser), initialState(name, input))
    }
}

/**
 * Returns an initial 'State' for parsing the input (with name for error messages).
 */
private fun initialState(name: String, input: ByteArray): State =
    State(
        name = name,
        input = Stream.of(input),
        decision = "",
        limit = -1,
        forbidden = null,
        isPeek = false,
        isSol = true,
        chars = intArrayOf(),
        charsByteOffset = -1,
        charsCharOffset = -1,
        charsLine = -1,
        charsLineChar = -1,
        byteOffset = 0,
        charOffset = 0,
        line = 1,
        lineChar = 0,
        code = Code.Unparsed,
        last = ' '.toInt(),
        yields = HashMap()
    )

/**
 * Inserts an error token if a commit was made outside a named choice. This should never happen outside tests.
 */
private fun commitBugs(reply: Reply): List<Token> {
    val tokens = reply.tokens
    val state = reply.state

    return if (reply.commit == null)
        tokens
    else
        tokens +
                Token(
                    byteOffset = state.byteOffset,
                    charOffset = state.charOffset,
                    line = state.line,
                    lineChar = state.lineChar,
                    code = Code.Error,
                    text = Escapable.of("Commit to '${reply.commit}' was made outside it")
                )
}

/**
 * Invokes the parser, ensures any unclaimed input characters
 * are wrapped into a token (only happens when testing productions), ensures no
 * input is left unparsed, and returns the parser's result.
 */
private fun wrap(parser: Parser): Parser =
    parser.snd("result", finishToken()) and eof() and peekResult("result")

/**
 * Appends an Error token with the specified message to the end of tokens, and if withFollowing
 * also appends the unparsed text following the error as a final Unparsed token.
 */
private fun errorTokens(
    tokens: List<Token>,
    state: State,
    message: String,
    flag: Boolean
): List<Token> {

    val newTokens = tokens +
            Token(
                byteOffset = state.byteOffset,
                charOffset = state.charOffset,
                line = state.line,
                lineChar = state.lineChar,
                code = Code.Error,
                text = Escapable.of(message)
            )

    return if (flag && state.input.isNotEmpty())
        newTokens +
                Token(
                    byteOffset = state.byteOffset,
                    charOffset = state.charOffset,
                    line = state.line,
                    lineChar = state.lineChar,
                    code = Code.Unparsed,
                    text = Escapable.of(state.input.codes())
                )
    else
        newTokens
}
