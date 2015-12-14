package io.dahgan.parser

/**
 * The 'Result' of each invocation is either an error, the actual result, or
 * a continuation for computing the actual result.
 */
sealed class Result {
    /**
     * Parsing aborted with a failure.
     */
    class Failed(val message: Any) : Result() {
        override fun toString() = "Failed $message"
    }

    /**
     * Parsing completed with a result.
     */
    class Completed(val result: Any) : Result() {
        override fun toString() = "Result $result"
    }

    /**
     * Parsing is ongoing with a continuation.
     */
    class More(val result: Parser) : Result() {
        override fun toString() = "More"
    }
}
