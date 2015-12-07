package io.dahgan

import org.junit.Test

class XtraTest {

    val bytes = "-#".toByteArray()

    @Test
    fun pattern() {
        val tokenizer = PatternTokenizer(`ns-plain`(1, Context.FlowKey))

        showTokens(tokenizer.tokenize("sample", bytes, false))
    }

    @Test
    fun parser() {
        val tokenizer = ParserTokenizer("(m,t)", `c-b-block-header`(2))

        showTokens(tokenizer.tokenize("sample", byteArrayOf(55, 10), false))
    }
}