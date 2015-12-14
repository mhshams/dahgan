package io.dahgan.parser

/**
 * Each invocation of a 'Parser' yields a 'Reply'. The 'Result' is only one part of the 'Reply'.
 */
data class Reply(
        /**
         * Parsing result.
         */
        val result: Result,

        /**
         * Tokens generated by the parser.
         */
        val tokens: Sequence<Token>,

        /**
         * Commitment to a decision point.
         */
        val commit: String?,

        /**
         * The updated parser state.
         */
        val state: State
) {
    override fun toString() = "Result: $result , Tokens: ${showTokens(tokens)}, Commit: $commit, State: { $state}"
}
