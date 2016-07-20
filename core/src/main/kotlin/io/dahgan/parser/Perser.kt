package io.dahgan.parser

import io.dahgan.parser.Result.*
import io.dahgan.stream.UniChar
import java.util.*

/**
 * A 'Parser' is basically a function computing a 'Reply'.
 */
typealias Parser = (State) -> Reply

/**
 * Tries to parse this and failing that parses other, unless this has committed in which case is fails immediately.
 */
infix fun Parser.or(other: Parser): Parser = decide(this, other)

/**
 * see Parse#or(parser)
 */
infix fun Parser.or(other: Char): Parser = decide(this, of(other))

/**
 * see Parse#or(parser)
 */
infix fun Parser.or(other: Int): Parser = decide(this, of(other))

/**
 * see Parse#or(parser)
 */
infix fun Parser.or(other: IntRange): Parser = decide(this, of(other))

/**
 * Parsers this and if it succeeds, parses the other.
 */
infix fun Parser.and(other: Parser): Parser = { state ->
    fun bindParser(left: Parser, right: Parser): Parser = { state ->
        val reply = left(state)
        when (reply.result) {
            is Failed -> reply.copy(result = Failed(reply.result.message))
            is Completed -> reply.copy(result = More(right))
            is More -> reply.copy(result = More(bindParser(reply.result.result, right)))
        }
    }

    bindParser(this, other)(state)
}

/**
 * see Parse#and(parser)
 */
infix fun Parser.and(other: Char): Parser = this and of(other)

/**
 * Parses this and if it succeeds, stores the result for future use and then parses the other.
 * TODO: is there a better way to keep the parse result ?
 */
fun Parser.snd(name: String, other: Parser): Parser = { state ->
    fun clone(current: Map<String, Any>, result: Any): MutableMap<String, Any> {
        val map = HashMap(current)
        map[name] = result
        return map
    }

    fun bindParser(left: Parser, right: Parser): Parser = { state ->
        val reply = left(state)

        when (reply.result) {
            is Failed -> reply.copy(result = Failed(reply.result.message))
            is Completed -> reply.copy(result = More(right),
                    state = reply.state.copy(yields = clone(state.yields, reply.result.result)))
            is More -> reply.copy(result = More(bindParser(reply.result.result, right)))
        }
    }

    bindParser(this, other)(state)
}

/**
 * Matches parser, except if rejected matches at this point.
 */
infix fun Parser.not(rejected: Parser): Parser = reject(rejected, null) and this

infix fun Parser.not(rejected: Char): Parser = reject(of(rejected), null) and this

/**
 * Commits to decision (in an option) after successfully matching the parser.
 */
infix fun Parser.cmt(decision: String): Parser = this and commit(decision)

/**
 * Commits to decision (in an option) if the current position matches parser, without consuming any characters.
 */
infix fun Parser.omt(decision: String): Parser = peek(this) and commit(decision)

/**
 * Repeats parser exactly n times.
 */
infix fun Parser.tms(n: Int): Parser = if (n <= 0) empty() else this and (this tms n - 1)

/**
 * Matches fewer than n occurrences of parser.
 */
infix fun Parser.lms(n: Int): Parser = when {
    n < 1 -> fail("Fewer than 0 repetitions")
    n == 1 -> reject(this, null)
    else -> "<x" cho ((this cmt "<x") and (this lms n - 1) or empty())
}

/**
 * Parses the specified parser; if it fails, it continues to the recovery parser to recover.
 */
infix fun Parser.recovery(recover: Parser): Parser = { state ->
    val unparsed = { state: State -> finishToken()(state.copy(code = Code.Unparsed)) }
    val reply = this(state)
    if (state.isPeek)
        reply
    else when (reply.result) {
        is Completed -> reply
        is More -> reply.copy(result = More(reply.result.result recovery recover))
        is Failed -> reply.copy(result = More(fake(Code.Error, reply.result.message) and unparsed and recover))
    }
}

/**
 * Converts 'Char' to a parser for a character (that returns nothing).
 */
fun of(char: Char) = nextIf { it == char.toInt() }

/**
 * see of(char)
 */
fun of(code: Int) = nextIf { it == code }

/**
 * Converts an int range to a parser for a character range (that returns nothing).
 */
fun of(range: IntRange) = nextIf { range.start <= it && it <= range.endInclusive }

/**
 * @see Parser#or(parser)
 */
infix fun IntRange.or(other: IntRange): Parser = of(this) or of(other)

/**
 * @see Parser#or(parser)
 */
infix fun Char.or(other: Char): Parser = of(this) or of(other)

/**
 * @see Parser#or(parser)
 */
infix fun Char.or(other: Int): Parser = of(this) or of(other)

/**
 * @see Parser#or(parser)
 */
infix fun Char.and(other: Char): Parser = of(this) and of(other)

/**
 * @see Parser#or(parser)
 */
infix fun Char.and(other: Parser): Parser = of(this) and other

/**
 *  Prepares a 'Reply' with the specified state and result.
 */
fun returnReply(state: State, result: Any): Reply = Reply(Completed(result), emptySequence(), null, state)

/**
 * Prepares a 'Reply' with the specified state and error message.
 */
fun failReply(state: State, message: Any): Reply = Reply(Failed(message), emptySequence(), null, state)

/**
 * Returns a failReply for an unexpected character.
 */
fun unexpectedReply(state: State): Reply =
        if (state.input.isEmpty())
            failReply(state, "Unexpected end of input")
        else
            failReply(state, "Unexpected '${escape(state.input.head().code)}'")

/**
 * Fails with a message.
 */
fun fail(message: Any): Parser = { state -> failReply(state, message) }

/**
 * Succeeds if parser matches some non-empty input characters at this point.
 */
fun nonEmpty(parser: Parser): Parser ={ state ->
    fun nonEmptyParser(offset: Int, parser: Parser): Parser = { state ->
        val reply = parser(state)
        val newSate = reply.state
        when (reply.result) {
            is Failed -> reply
            is Completed -> if (newSate.charOffset > offset) reply else failReply(newSate, "Matched empty pattern")
            is More -> reply.copy(result = More(nonEmptyParser(offset, reply.result.result)))
        }
    }

    nonEmptyParser(state.charOffset, parser)(state)
}

/**
 * Always matches without consuming any input.
 */
fun empty(): Parser = { state -> returnReply(state, "") }

/**
 * Matches the end of the input.
 */
fun eof(): Parser = { state -> if (state.input.isEmpty()) returnReply(state, "") else unexpectedReply(state) }

/**
 * Matches the start of a line.
 */
fun sol(): Parser = { state -> if (state.isSol) returnReply(state, "") else failReply(state, "Expected start of line") }

/**
 * Returns a 'Reply' containing the state and token.
 * Any collected characters are cleared (either there are none, or we
 * put them in this token, or we don't want them).
 */
fun tokenReply(state: State, token: Token): Reply = Reply(Completed(""), sequenceOf(token), null, state.
        copy(chars = intArrayOf(), charsByteOffset = -1, charsCharOffset = -1, charsLine = -1, charsLineChar = -1))

/**
 * Places all collected text into a new token and begins a new
 * one, or does nothing if there are no collected characters.
 */
fun finishToken(): Parser = { state ->
    val newState = state.copy(
            chars = IntArray(0),
            charsByteOffset = -1,
            charsCharOffset = -1,
            charsLine = -1,
            charsLineChar = -1)

    when {
        state.isPeek -> returnReply(newState, "")
        state.chars.isEmpty() -> returnReply(newState, "")
        else -> tokenReply(newState, Token(
                state.charsByteOffset,
                state.charsCharOffset,
                state.charsLine,
                state.charsLineChar,
                state.code,
                Escapable.of(state.chars.reversed().toIntArray())))
    }
}

/**
 * Places all text matched by parser into a 'Token' with the specified code (unless it is empty).
 * Note it collects the text even if there is an error.
 */
fun token(code: Code, parser: Parser): Parser = finishToken() and with(
        { state: State, code: Code? -> state.copy(code = code!!) },
        { state -> state.code },
        code,
        parser and finishToken())

/**
 * Creates a token with the specified code and "fake"
 * text characters, instead of whatever characters are collected so far.
 */
fun fake(code: Code, text: Any): Parser = { state ->
    if (state.isPeek) {
        returnReply(state, "")
    } else {
        tokenReply(state, Token(
                if (state.charsByteOffset == -1) state.byteOffset else state.charsByteOffset,
                if (state.charsCharOffset == -1) state.charOffset else state.charsCharOffset,
                if (state.charsLine == -1) state.line else state.charsLine,
                if (state.charsLineChar == -1) state.lineChar else state.charsLineChar,
                code,
                Escapable.of(text.toString())
        ))
    }
}

/**
 * Collects the text matched by the specified parser into a Meta token.
 */
fun meta(parser: Parser): Parser = token(Code.Meta, parser)

/**
 * @see meta(parser)
 */
fun meta(char: Char): Parser = token(Code.Meta, of(char))

/**
 * @see meta(parser)
 */
fun meta(code: Int): Parser = token(Code.Meta, of(code))

/**
 * Collects the text matched by the specified parser into an Indicator token.
 */
fun indicator(parser: Parser): Parser = token(Code.Indicator, parser)

/**
 * @see indicator(parser)
 */
fun indicator(char: Char): Parser = token(Code.Indicator, of(char))

/**
 * Collects the text matched by the specified parser into a Text token.
 */
fun text(parser: Parser): Parser = token(Code.Text, parser)

/**
 * Returns an empty token.
 */
fun emptyToken(code: Code): Parser = finishToken() and { state ->
    if (state.isPeek)
        returnReply(state, "")
    else
        tokenReply(state, Token(state.byteOffset, state.charOffset, state.line, state.lineChar, code, Escapable.of("")))
}

/**
 * Wraps the specified parser with matching beginCode and endCode tokens.
 */
fun wrapTokens(beginCode: Code, endCode: Code, parser: Parser): Parser =
        emptyToken(beginCode) and  prefixErrorWith(parser, emptyToken(endCode)) and emptyToken(endCode)

/**
 * Invokes the prefix parser if an error is detected during the pattern parser, and then return the error.
 */
fun prefixErrorWith(parser: Parser, prefix: Parser): Parser = { state ->
    val reply = parser(state)
    when (reply.result) {
        is Completed -> reply
        is More -> reply.copy(result = More(prefixErrorWith(reply.result.result, prefix)))
        is Failed -> reply.copy(result = More(prefix and fail(reply.result.message)))
    }
}

/**
 * Tries to match parser, otherwise does nothing.
 */
fun opt(parser: Parser): Parser = parser and empty() or empty()

/**
 * Matches zero or more occurrences of repeat, as long as each one actually consumes input characters.
 */
fun zom(parser: Parser): Parser {
    fun zomParser(): Parser = (parser cmt "*") and { state -> zomParser()(state) } or empty()
    return "*" cho zomParser()
}

/**
 * Matches one or more occurrences of parser, as long as each one actually consumed input characters.
 */
fun oom(parser: Parser): Parser = parser and zom(parser)

/**
 * Tries to parse first, and failing that parses
 * second, unless first has committed in which case is fails immediately.
 */
fun decide(left: Parser, right: Parser): Parser = { state ->
    fun decideParser(point: State, tokens: Sequence<Token>, left: Parser, right: Parser): Parser = { state ->
        val reply = left(state)
        val newTokens = tokens + reply.tokens
        when (reply.result) {
            is Failed -> Reply(More(right), emptySequence(), null, point)
            is Completed -> reply.copy(tokens = newTokens)
            is More ->
                if (reply.commit != null)
                    reply.copy(tokens = newTokens, result = More(reply.result.result))
                else
                    decideParser(point, newTokens, reply.result.result, right)(reply.state)
        }
    }

    decideParser(state, emptySequence(), left, right)(state)
}

/**
 * Provides a decision name to the choice about to
 * be made in parser, to allow to commit to it.
 */
fun choice(decision: String, parser: Parser): Parser = { state ->
    fun choiceParser(parentDecision: String, makingDecision: String, parser: Parser): Parser = { state ->
        val reply = parser(state)
        val commit = when (reply.commit) {
            null -> null
            makingDecision -> null
            else -> reply.commit
        }

        when (reply.result) {
            is More -> reply.copy(commit = commit,
                    result = More(choiceParser(parentDecision, makingDecision, reply.result.result)))
            else -> reply.copy(commit = commit, state = reply.state.copy(decision = parentDecision))
        }
    }

    choiceParser(state.decision, decision, parser)(state.copy(decision = decision))
}

/**
 * Succeeds if parser matches at the previous character. It does not consume any input.
 */
fun prev(parser: Parser): Parser = { state ->
    fun prevParser(point: State, parser: Parser, state: State): Reply {
        val reply = parser(state)
        return when (reply.result) {
            is Failed -> failReply(point, reply.result.message)
            is Completed -> returnReply(point, reply.result.result)
            is More -> prevParser(point, reply.result.result, reply.state)
        }
    }
    prevParser(state, parser, state.copy(isPeek = true, input = state.input.push(UniChar(-1, state.last))))
}

/**
 * Succeeds if parser matches at this point, but does not consume any input.
 */
fun peek(parser: Parser): Parser = { state ->
    fun peekParser(point: State, parser: Parser, state: State): Reply {
        val reply = parser(state)
        return when (reply.result) {
            is Failed -> failReply(point, reply.result.message)
            is Completed -> returnReply(point, reply.result.result)
            is More -> peekParser(point, reply.result.result, reply.state)
        }
    }
    peekParser(state, parser, state.copy(isPeek = true))
}

/**
 * Fails if parser matches at this point, and does nothing otherwise.
 * If name is provided, it is used in the error message, otherwise the messages uses the current character.
 */
fun reject(parser: Parser, name: String?): Parser {
    fun rejectParser(point: State, name: String?, parser: Parser, state: State): Reply {
        val reply = parser(state)
        return when (reply.result) {
            is Failed -> returnReply(point, "")
            is Completed -> if (name == null) unexpectedReply(point) else failReply(point, "Unexpected $name")
            is More -> rejectParser(point, name, reply.result.result, reply.state)
        }
    }

    return { state -> rejectParser(state, name, parser, state.copy(isPeek = true)) }
}

/**
 * Consumes all the character up to and not including the next point where the specified parser is a match.
 */
fun upto(parser: Parser): Parser = zom(nla(parser) and nextIf({ true }))

/**
 *  Commits the parser to all the decisions up to the most recent parent decision.
 *  This makes all tokens generated in this parsing path immediately available to the caller.
 */
fun commit(decision: String): Parser = { state -> Reply(Completed(""), emptySequence(), decision, state) }

/**
 * Increments line counter and resets lineChar.
 */
fun nextLine(): Parser = { state ->
    returnReply(state.copy(isSol = true, line = state.line + 1, lineChar = 0), "")
}

/**
 * Invokes the specified parser with the value of the specified field set to value for the duration of the
 * invocation, using the set and get functions to manipulate it.
 */
fun <T> with(set: (State, T?) -> State, get: (State) -> T, value: T?, parser: Parser): Parser = { state ->

    fun withParser(parentValue: T, parser: Parser): Parser = { state ->
        val reply = parser(state)
        when (reply.result) {
            is More -> reply.copy(result = More(withParser(parentValue, reply.result.result)))
            else -> reply.copy(state = set(reply.state, parentValue))
        }
    }
    withParser(get(state), parser)(set(state, value))
}

/**
 * Parses the specified parser ensuring that it does not contain anything matching the forbidden parser.
 */
fun forbidding(parser: Parser, forbidden: Parser): Parser = with(
        { state: State, forbidden: Parser? -> state.copy(forbidden = forbidden) },
        { state -> state.forbidden },
        forbidden and empty(),
        parser)

/**
 * Parses the specified parser ensuring that it does not consume more than the limit input chars.
 */
fun limitedTo(parser: Parser, limit: Int): Parser = with(
        { state: State, limit: Int? -> state.copy(limit = limit!!) },
        { state -> state.limit },
        limit,
        parser)

/**
 * Fails if the current position matches the 'State' forbidden
 * pattern or if the 'State' lookahead limit is reached. Otherwise it consumes
 * (and buffers) the next input char if it satisfies test.
 */
fun nextIf(test: (Int) -> Boolean): Parser {
    fun consumeNextIf(state: State): Reply {
        return if (!state.input.isEmpty() && test(state.input.head().code)) {
            val char = state.input.head().code
            val chars = if (state.isPeek) IntArray(0) else intArrayOf(char) + state.chars

            val byteOffset = if (state.isPeek) -1 else if (state.chars.isEmpty()) state.byteOffset else state.charsByteOffset
            val charOffset = if (state.isPeek) -1 else if (state.chars.isEmpty()) state.charOffset else state.charsCharOffset
            val line = if (state.isPeek) -1 else if (state.chars.isEmpty()) state.line else state.charsLine
            val lineChar = if (state.isPeek) -1 else if (state.chars.isEmpty()) state.lineChar else state.charsLineChar

            val isSol = if (char == 0xFEFF) state.isSol else false

            val newState = state.copy(
                    input = state.input.tail(),
                    last = char,
                    chars = chars,
                    charsByteOffset = byteOffset,
                    charsCharOffset = charOffset,
                    charsLine = line,
                    charsLineChar = lineChar,
                    isSol = isSol,
                    byteOffset = state.input.head().offset,
                    charOffset = state.charOffset + 1,
                    lineChar = state.lineChar + 1)

            returnReply(newState, "")
        } else {
            unexpectedReply(state)
        }
    }

    fun limitedNextIf(state: State): Reply = when (state.limit) {
        -1 -> consumeNextIf(state)
        0 -> failReply(state, "Lookahead limit reached")
        else -> consumeNextIf(state.copy(limit = state.limit - 1))
    }

    return { state ->
        if (state.forbidden == null) limitedNextIf(state) else {
            val newParser = reject(state.forbidden, "forbidden pattern")
            val reply = newParser(state.copy(forbidden = null))
            when (reply.result) {
                is Failed -> reply
                is Completed -> limitedNextIf(state)
                else -> throw IllegalStateException("ME: unexpected")
            }
        }
    }
}

/**
 * Doesn't actually detect the encoding, we just call it
 * this way to make the productions compatible with the spec. Instead it simply
 * reports the encoding (which was already detected when we started parsing).
 */
fun bom(code: Int): Parser = { state ->
    (of(code) and fake(Code.Bom, state.input.encoding().toString().substring(1)))(state)
}

/**
 * Returns the last consumed character, which is assumed to be a decimal digit, as an integer.
 */
fun asInt(): Parser = { state -> returnReply(state, state.last - 0x30) }

/**
 * Returns the previously stored result value with given name
 */
fun peekResult(result: String): Parser = { state -> returnReply(state, state.yields[result] as Any) }

/**
 * Returns the previously stored results value with given names
 */
fun peekResult(first: String, second: String): Parser = { state ->
    returnReply(state.copy(yields = state.yields), "(${state.yields[first] as Any},${state.yields[second] as Any})")
}

/**
 * Wraps the given value and returns it as parser.
 */
fun result(result: Int): Parser = { state -> returnReply(state, result) }

/**
 * Wraps the given value and returns it as parser.
 */
fun result(result: Chomp): Parser = { state -> returnReply(state, result) }

/**
 * Provides a decision name to the choice about to be made, to allow to @commit@ to it.
 */
infix fun String.cho(parser: Parser): Parser = choice(this, parser)

/**
 * Commits to decision (in an option) after successfully matching the parser.
 */
infix fun Char.cmt(decision: String): Parser = of(this) and commit(decision)

/**
 * Matches the current point without consuming any characters,
 * if the previous character matches the lookbehind parser (single character positive lookbehind)
 */
fun plb(lookbehind: Parser): Parser = prev(lookbehind)

/**
 * Matches the current point without consuming any characters,
 * if it matches the lookahead parser (positive lookahead)
 */
fun pla(lookahead: Parser): Parser = peek(lookahead)

/**
 * Matches the current point without consuming any characters,
 * if it matches the lookahead parser (negative lookahead)
 */
fun nla(lookahead: Parser): Parser = reject(lookahead, null)
