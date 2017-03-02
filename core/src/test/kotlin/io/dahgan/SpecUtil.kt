package io.dahgan

import io.dahgan.parser.*
import io.dahgan.stream.Stream

/**
 * Invokes the parser and then consumes all remaining unparsed input characters.
 */
private fun consume(parser: Parser): Parser {
    val cleanInput = { state: State -> returnReply(state.copy(input = Stream.empty()), "") }

    return parser.snd("result", finishToken()) and cleanInput and peekResult("result")
}

val tokenizers = mapOf(
        "c-chomping-indicator"          to ParserTokenizer("t", `c-chomping-indicator`()),
        "detect-inline-indentation"     to ParserTokenizer("m", consume(`detect-inline-indentation`)),
        "c-printable"                   to PatternTokenizer(`c-printable`),
        "nb-json"                       to PatternTokenizer(`nb-json`),
        "c-byte-order-mark"             to PatternTokenizer(`c-byte-order-mark`),
        "c-mapping-value"               to PatternTokenizer(`c-mapping-value`),
        "c-tag"                         to PatternTokenizer(`c-tag`),
        "c-collect-entry"               to PatternTokenizer(`c-collect-entry`),
        "c-comment"                     to PatternTokenizer(`c-comment`),
        "c-sequence-start"              to PatternTokenizer(`c-sequence-start`),
        "c-mapping-key"                 to PatternTokenizer(`c-mapping-key`),
        "c-sequence-entry"              to PatternTokenizer(`c-sequence-entry`),
        "c-literal"                     to PatternTokenizer(`c-literal`),
        "c-anchor"                      to PatternTokenizer(`c-anchor`),
        "c-reserved"                    to PatternTokenizer(`c-reserved`),
        "c-single-quote"                to PatternTokenizer(`c-single-quote`),
        "c-folded"                      to PatternTokenizer(`c-folded`),
        "c-mapping-start"               to PatternTokenizer(`c-mapping-start`),
        "c-sequence-end"                to PatternTokenizer(`c-sequence-end`),
        "c-alias"                       to PatternTokenizer(`c-alias`),
        "c-directive"                   to PatternTokenizer(`c-directive`),
        "c-reserved"                    to PatternTokenizer(`c-reserved`),
        "c-mapping-end"                 to PatternTokenizer(`c-mapping-end`),
        "c-double-quote"                to PatternTokenizer(`c-double-quote`),
        "c-indicator"                   to PatternTokenizer(`c-indicator`),
        "c-flow-indicator"              to PatternTokenizer(`c-flow-indicator`),
        "b-line-feed"                   to PatternTokenizer(`b-line-feed`),
        "b-carriage-return"             to PatternTokenizer(`b-carriage-return`),
        "b-char"                        to PatternTokenizer(`b-char`),
        "nb-char"                       to PatternTokenizer(`nb-char`),
        "b-break"                       to PatternTokenizer(`b-break`),
        "b-as-line-feed"                to PatternTokenizer(`b-as-line-feed`),
        "b-non-content"                 to PatternTokenizer(`b-non-content`),
        "s-space"                       to PatternTokenizer(`s-space`),
        "s-tab"                         to PatternTokenizer(`s-tab`),
        "s-white"                       to PatternTokenizer(`s-white`),
        "ns-char"                       to PatternTokenizer(`ns-char`),
        "ns-dec-digit"                  to PatternTokenizer(`ns-dec-digit`),
        "ns-hex-digit"                  to PatternTokenizer(`ns-hex-digit`),
        "ns-ascii-letter"               to PatternTokenizer(`ns-ascii-letter`),
        "ns-word-char"                  to PatternTokenizer(`ns-word-char`),
        "ns-uri-char"                   to PatternTokenizer(`ns-uri-char`),
        "ns-tag-char"                   to PatternTokenizer(`ns-tag-char`),
        "c-escape"                      to PatternTokenizer(`c-escape`),
        "ns-esc-null"                   to PatternTokenizer(`ns-esc-null`),
        "ns-esc-bell"                   to PatternTokenizer(`ns-esc-bell`),
        "ns-esc-backspace"              to PatternTokenizer(`ns-esc-backspace`),
        "ns-esc-horizontal-tab"         to PatternTokenizer(`ns-esc-horizontal-tab`),
        "ns-esc-line-feed"              to PatternTokenizer(`ns-esc-line-feed`),
        "ns-esc-vertical-tab"           to PatternTokenizer(`ns-esc-vertical-tab`),
        "ns-esc-form-feed"              to PatternTokenizer(`ns-esc-form-feed`),
        "ns-esc-carriage-return"        to PatternTokenizer(`ns-esc-carriage-return`),
        "ns-esc-escape"                 to PatternTokenizer(`ns-esc-escape`),
        "ns-esc-space"                  to PatternTokenizer(`ns-esc-space`),
        "ns-esc-double-quote"           to PatternTokenizer(`ns-esc-double-quote`),
        "ns-esc-slash"                  to PatternTokenizer(`ns-esc-slash`),
        "ns-esc-backslash"              to PatternTokenizer(`ns-esc-backslash`),
        "ns-esc-next-line"              to PatternTokenizer(`ns-esc-next-line`),
        "ns-esc-non-breaking-space"     to PatternTokenizer(`ns-esc-non-breaking-space`),
        "ns-esc-line-separator"         to PatternTokenizer(`ns-esc-line-separator`),
        "ns-esc-paragraph-separator"    to PatternTokenizer(`ns-esc-paragraph-separator`),
        "ns-esc-8-bit"                  to PatternTokenizer(`ns-esc-8-bit`),
        "ns-esc-16-bit"                 to PatternTokenizer(`ns-esc-16-bit`),
        "ns-esc-32-bit"                 to PatternTokenizer(`ns-esc-32-bit`),
        "c-ns-esc-char"                 to PatternTokenizer(`c-ns-esc-char`),
        "s-separate-in-line"            to PatternTokenizer(`s-separate-in-line`),
        "b-as-space"                    to PatternTokenizer(`b-as-space`),
        "c-nb-comment-text"             to PatternTokenizer(`c-nb-comment-text`),
        "b-comment"                     to PatternTokenizer(`b-comment`),
        "s-b-comment"                   to PatternTokenizer(`s-b-comment`),
        "l-comment"                     to PatternTokenizer(`l-comment`),
        "s-l-comments"                  to PatternTokenizer(`s-l-comments`),
        "l-directive"                   to PatternTokenizer(`l-directive`),
        "ns-reserved-directive"         to PatternTokenizer(`ns-reserved-directive`()),
        "ns-directive-name"             to PatternTokenizer(`ns-directive-name`()),
        "ns-directive-parameter"        to PatternTokenizer(`ns-directive-parameter`()),
        "ns-yaml-directive"             to PatternTokenizer(`ns-yaml-directive`()),
        "ns-yaml-version"               to PatternTokenizer(`ns-yaml-version`()),
        "ns-tag-directive"              to PatternTokenizer(`ns-tag-directive`()),
        "c-tag-handle"                  to PatternTokenizer(`c-tag-handle`()),
        "c-primary-tag-handle"          to PatternTokenizer(`c-primary-tag-handle`()),
        "c-secondary-tag-handle"        to PatternTokenizer(`c-secondary-tag-handle`()),
        "c-named-tag-handle"            to PatternTokenizer(`c-named-tag-handle`()),
        "ns-tag-prefix"                 to PatternTokenizer(`ns-tag-prefix`()),
        "c-ns-local-tag-prefix"         to PatternTokenizer(`c-ns-local-tag-prefix`()),
        "ns-global-tag-prefix"          to PatternTokenizer(`ns-global-tag-prefix`()),
        "c-ns-tag-property"             to PatternTokenizer(`c-ns-tag-property`()),
        "c-verbatim-tag"                to PatternTokenizer(`c-verbatim-tag`()),
        "c-ns-shorthand-tag"            to PatternTokenizer(`c-ns-shorthand-tag`()),
        "c-non-specific-tag"            to PatternTokenizer(`c-non-specific-tag`()),
        "c-ns-anchor-property"          to PatternTokenizer(`c-ns-anchor-property`),
        "ns-anchor-char"                to PatternTokenizer(`ns-anchor-char`()),
        "ns-anchor-name"                to PatternTokenizer(`ns-anchor-name`()),
        "c-ns-alias-node"               to PatternTokenizer(`c-ns-alias-node`),
        "e-scalar"                      to PatternTokenizer(`e-scalar`),
        "e-node"                        to PatternTokenizer(`e-node`),
        "nb-double-char"                to PatternTokenizer(`nb-double-char`),
        "ns-double-char"                to PatternTokenizer(`ns-double-char`),
        "nb-double-one-line"            to PatternTokenizer(`nb-double-one-line`),
        "nb-ns-double-in-line"          to PatternTokenizer(`nb-ns-double-in-line`),
        "c-quoted-quote"                to PatternTokenizer(`c-quoted-quote`),
        "nb-single-char"                to PatternTokenizer(`nb-single-char`),
        "ns-single-char"                to PatternTokenizer(`ns-single-char`),
        "nb-single-one-line"            to PatternTokenizer(`nb-single-one-line`),
        "nb-ns-single-in-line"          to PatternTokenizer(`nb-ns-single-in-line`),
        "ns-plain-safe-out"             to PatternTokenizer(`ns-plain-safe-out`),
        "ns-plain-safe-in"              to PatternTokenizer(`ns-plain-safe-in`),
        "ns-s-block-map-implicit-key"   to PatternTokenizer(`ns-s-block-map-implicit-key`()),
        "l-document-prefix"             to PatternTokenizer(`l-document-prefix`),
        "c-directives-end"              to PatternTokenizer(`c-directives-end`),
        "c-document-end"                to PatternTokenizer(`c-document-end`),
        "l-document-suffix"             to PatternTokenizer(`l-document-suffix`),
        "c-forbidden"                   to PatternTokenizer(`c-forbidden`()),
        "l-bare-document"               to PatternTokenizer(`l-bare-document`),
        "l-explicit-document"           to PatternTokenizer(`l-explicit-document`),
        "l-directives-document"         to PatternTokenizer(`l-directives-document`),
        "l-any-document"                to PatternTokenizer(`l-any-document`),
        "l-yaml-stream"                 to PatternTokenizer(`l-yaml-stream`)
)

val tokenizersWithN = mapOf<String, (Int) -> Tokenizer>(
        "c-b-block-header"                  to { n -> ParserTokenizer("(m,t)", `c-b-block-header`(n)) },
        "detect-collection-indentation"     to { n -> ParserTokenizer("m", consume(`detect-collection-indentation`(n))) },
        "detect-scalar-indentation"         to { n -> ParserTokenizer("m", consume(`detect-scalar-indentation`(n))) },
        "c-indentation-indicator"           to { n -> ParserTokenizer("m", `c-indentation-indicator`(n)) },
        "count-spaces"                      to { n -> ParserTokenizer("m", `count-spaces`(n)) },
        "s-indent"                          to { n -> PatternTokenizer(`s-indent`(n)) },
        "s-indent-lt"                       to { n -> PatternTokenizer(`s-indent-lt`(n)) },
        "s-indent-le"                       to { n -> PatternTokenizer(`s-indent-le`(n)) },
        "s-block-line-prefix"               to { n -> PatternTokenizer(`s-block-line-prefix`(n)) },
        "s-flow-line-prefix"                to { n -> PatternTokenizer(`s-flow-line-prefix`(n)) },
        "s-flow-folded"                     to { n -> PatternTokenizer(`s-flow-folded`(n)) },
        "s-separate-lines"                  to { n -> PatternTokenizer(`s-separate-lines`(n)) },
        "s-double-escaped"                  to { n -> PatternTokenizer(`s-double-escaped`(n)) },
        "s-double-break"                    to { n -> PatternTokenizer(`s-double-break`(n)) },
        "s-double-next-line"                to { n -> PatternTokenizer(`s-double-next-line`(n)) },
        "nb-double-multi-line"              to { n -> PatternTokenizer(`nb-double-multi-line`(n)) },
        "s-single-next-line"                to { n -> PatternTokenizer(`s-single-next-line`(n)) },
        "nb-single-multi-line"              to { n -> PatternTokenizer(`nb-single-multi-line`(n)) },
        "l-strip-empty"                     to { n -> PatternTokenizer(`l-strip-empty`(n)) },
        "l-keep-empty"                      to { n -> PatternTokenizer(`l-keep-empty`(n)) },
        "l-trail-comments"                  to { n -> PatternTokenizer(`l-trail-comments`(n)) },
        "l-nb-literal-text"                 to { n -> PatternTokenizer(`l-nb-literal-text`(n)) },
        "b-nb-literal-next"                 to { n -> PatternTokenizer(`b-nb-literal-next`(n)) },
        "s-nb-folded-text"                  to { n -> PatternTokenizer(`s-nb-folded-text`(n)) },
        "l-nb-folded-lines"                 to { n -> PatternTokenizer(`l-nb-folded-lines`(n)) },
        "s-nb-spaced-text"                  to { n -> PatternTokenizer(`s-nb-spaced-text`(n)) },
        "b-l-spaced"                        to { n -> PatternTokenizer(`b-l-spaced`(n)) },
        "l-nb-spaced-lines"                 to { n -> PatternTokenizer(`l-nb-spaced-lines`(n)) },
        "l-nb-same-lines"                   to { n -> PatternTokenizer(`l-nb-same-lines`(n)) },
        "l-nb-diff-lines"                   to { n -> PatternTokenizer(`l-nb-diff-lines`(n)) },
        "c-l+literal"                       to { n -> PatternTokenizer(`c-l+literal`(n)) },
        "c-l+folded"                        to { n -> PatternTokenizer(`c-l+folded`(n)) },
        "l+block-sequence"                  to { n -> PatternTokenizer(`l+block-sequence`(n)) },
        "c-l-block-seq-entry"               to { n -> PatternTokenizer(`c-l-block-seq-entry`(n)) },
        "ns-l-in-line-sequence"             to { n -> PatternTokenizer(`ns-l-in-line-sequence`(n)) },
        "l+block-mapping"                   to { n -> PatternTokenizer(`l+block-mapping`(n)) },
        "ns-l-block-map-entry"              to { n -> PatternTokenizer(`ns-l-block-map-entry`(n)) },
        "c-l-block-map-explicit-entry"      to { n -> PatternTokenizer(`c-l-block-map-explicit-entry`(n)) },
        "c-l-block-map-explicit-key"        to { n -> PatternTokenizer(`c-l-block-map-explicit-key`(n)) },
        "l-block-map-explicit-value"        to { n -> PatternTokenizer(`l-block-map-explicit-value`(n)) },
        "ns-l-block-map-implicit-entry"     to { n -> PatternTokenizer(`ns-l-block-map-implicit-entry`(n)) },
        "c-l-block-map-implicit-value"      to { n -> PatternTokenizer(`c-l-block-map-implicit-value`(n)) },
        "ns-l-in-line-mapping"              to { n -> PatternTokenizer(`ns-l-in-line-mapping`(n)) },
        "s-l+flow-in-block"                 to { n -> PatternTokenizer(`s-l+flow-in-block`(n)) }
)

val tokenizersWithC = mapOf<String, (Context) -> Tokenizer>(
        "ns-plain-first"                to { c -> PatternTokenizer(`ns-plain-first`(c)) },
        "ns-plain-safe"                 to { c -> PatternTokenizer(`ns-plain-safe`(c)) },
        "ns-plain-char"                 to { c -> PatternTokenizer(`ns-plain-char`(c)) },
        "nb-ns-plain-in-line"           to { c -> PatternTokenizer(`nb-ns-plain-in-line`(c)) },
        "ns-plain-one-line"             to { c -> PatternTokenizer(`ns-plain-one-line`(c)) },
        "ns-s-implicit-yaml-key"        to { c -> PatternTokenizer(`ns-s-implicit-yaml-key`(c)) },
        "c-s-implicit-json-key"         to { c -> PatternTokenizer(`c-s-implicit-json-key`(c)) }
)

val tokenizersWithT = mapOf<String, (Chomp) -> Tokenizer>(
        "b-chomped-last"         to { t -> PatternTokenizer(`b-chomped-last`(t)) }
)

val tokenizersWithNC = mapOf<String, (Int, Context) -> Tokenizer>(
        "s-line-prefix"                 to { n, c -> PatternTokenizer(`s-line-prefix`(n, c)) },
        "l-empty"                       to { n, c -> PatternTokenizer(`l-empty`(n, c)) },
        "b-l-trimmed"                   to { n, c -> PatternTokenizer(`b-l-trimmed`(n, c)) },
        "b-l-folded"                    to { n, c -> PatternTokenizer(`b-l-folded`(n, c)) },
        "s-separate"                    to { n, c -> PatternTokenizer(`s-separate`(n, c)) },
        "c-ns-properties"               to { n, c -> PatternTokenizer(`c-ns-properties`(n, c)) },
        "c-double-quoted"               to { n, c -> PatternTokenizer(`c-double-quoted`(n, c)) },
        "nb-double-text"                to { n, c -> PatternTokenizer(`nb-double-text`(n, c)) },
        "c-single-quoted"               to { n, c -> PatternTokenizer(`c-single-quoted`(n, c)) },
        "nb-single-text"                to { n, c -> PatternTokenizer(`nb-single-text`(n, c)) },
        "ns-plain"                      to { n, c -> PatternTokenizer(`ns-plain`(n, c)) },
        "s-ns-plain-next-line"          to { n, c -> PatternTokenizer(`s-ns-plain-next-line`(n, c)) },
        "ns-plain-multi-line"           to { n, c -> PatternTokenizer(`ns-plain-multi-line`(n, c)) },
        "c-flow-sequence"               to { n, c -> PatternTokenizer(`c-flow-sequence`(n, c)) },
        "ns-s-flow-seq-entries"         to { n, c -> PatternTokenizer(`ns-s-flow-seq-entries`(n, c)) },
        "ns-flow-seq-entry"             to { n, c -> PatternTokenizer(`ns-flow-seq-entry`(n, c)) },
        "c-flow-mapping"                to { n, c -> PatternTokenizer(`c-flow-mapping`(n, c)) },
        "ns-s-flow-map-entries"         to { n, c -> PatternTokenizer(`ns-s-flow-map-entries`(n, c)) },
        "ns-flow-map-entry"             to { n, c -> PatternTokenizer(`ns-flow-map-entry`(n, c)) },
        "ns-flow-map-explicit-entry"    to { n, c -> PatternTokenizer(`ns-flow-map-explicit-entry`(n, c)) },
        "ns-flow-map-implicit-entry"    to { n, c -> PatternTokenizer(`ns-flow-map-implicit-entry`(n, c)) },
        "ns-flow-map-yaml-key-entry"    to { n, c -> PatternTokenizer(`ns-flow-map-yaml-key-entry`(n, c)) },
        "c-ns-flow-map-empty-key-entry" to { n, c -> PatternTokenizer(`c-ns-flow-map-empty-key-entry`(n, c)) },
        "c-ns-flow-map-separate-value"  to { n, c -> PatternTokenizer(`c-ns-flow-map-separate-value`(n, c)) },
        "c-ns-flow-map-json-key-entry"  to { n, c -> PatternTokenizer(`c-ns-flow-map-json-key-entry`(n, c)) },
        "c-ns-flow-map-adjacent-value"  to { n, c -> PatternTokenizer(`c-ns-flow-map-adjacent-value`(n, c)) },
        "ns-flow-pair"                  to { n, c -> PatternTokenizer(`ns-flow-pair`(n, c)) },
        "ns-flow-pair-entry"            to { n, c -> PatternTokenizer(`ns-flow-pair-entry`(n, c)) },
        "ns-flow-pair-yaml-key-entry"   to { n, c -> PatternTokenizer(`ns-flow-pair-yaml-key-entry`(n, c)) },
        "c-ns-flow-pair-json-key-entry" to { n, c -> PatternTokenizer(`c-ns-flow-pair-json-key-entry`(n, c)) },
        "ns-flow-yaml-content"          to { n, c -> PatternTokenizer(`ns-flow-yaml-content`(n, c)) },
        "c-flow-json-content"           to { n, c -> PatternTokenizer(`c-flow-json-content`(n, c)) },
        "ns-flow-content"               to { n, c -> PatternTokenizer(`ns-flow-content`(n, c)) },
        "ns-flow-yaml-node"             to { n, c -> PatternTokenizer(`ns-flow-yaml-node`(n, c)) },
        "c-flow-json-node"              to { n, c -> PatternTokenizer(`c-flow-json-node`(n, c)) },
        "ns-flow-node"                  to { n, c -> PatternTokenizer(`ns-flow-node`(n, c)) },
        "s-l+block-indented"            to { n, c -> PatternTokenizer(`s-l+block-indented`(n, c)) },
        "s-l+block-node"                to { n, c -> PatternTokenizer(`s-l+block-node`(n, c)) },
        "s-l+block-in-block"            to { n, c -> PatternTokenizer(`s-l+block-in-block`(n, c)) },
        "s-l+block-scalar"              to { n, c -> PatternTokenizer(`s-l+block-scalar`(n, c)) },
        "s-l+block-collection"          to { n, c -> PatternTokenizer(`s-l+block-collection`(n, c)) }
)

val tokenizersWithNT = mapOf<String, (Int, Chomp) -> Tokenizer>(
        "l-chomped-empty"         to { n, t -> PatternTokenizer(`l-chomped-empty`(n, t)) },
        "l-literal-content"       to { n, t -> PatternTokenizer(`l-literal-content`(n, t)) },
        "l-folded-content"        to { n, t -> PatternTokenizer(`l-folded-content`(n, t)) }
)

/**
 * Converts the production with the specified name to a simple Tokenizer,
 * or throws IllegalArgumentException if it isn't known.
 */
fun tokenizer(name: String) = tokenizers.getOrElse(name) { notFound(name) }

/**
 * Converts the production, that requires an n argument, with the specified name to a simple Tokenizer,
 * or throws IllegalArgumentException if it isn't known.
 */
fun tokenizerWithN(name: String, n: Int) = tokenizersWithN.getOrElse(name) { return notFound(name) }(n)

/**
 * Converts the production, that requires a c argument, with the specified name to a simple Tokenizer,
 * or throws IllegalArgumentException if it isn't known.
 */
fun tokenizerWithC(name: String, c: Context) = tokenizersWithC.getOrElse(name) { return notFound(name) }(c)

/**
 * Returns a mapping from a production name to a production tokenizer, that takes a t argument,
 * or throws IllegalArgumentException if it isn't known.
 */
fun tokenizerWithT(name: String, t: Chomp) = tokenizersWithT.getOrElse(name) { return notFound(name) }(t)

/**
 * Converts the production, that requires n and c arguments, with the specified name to a simple Tokenizer,
 * or throws IllegalArgumentException if it isn't known.
 */
fun tokenizerWithNC(name: String, n: Int, c: Context) = tokenizersWithNC.getOrElse(name) { return notFound(name) }(n, c)

/**
 * Converts the production, that requires n and t arguments, with the specified name to a simple Tokenizer,
 * or throws IllegalArgumentException if it isn't known.
 */
fun tokenizerWithNT(name: String, n: Int, t: Chomp) = tokenizersWithNT.getOrElse(name) { return notFound(name) }(n, t)

private fun notFound(name: String): Tokenizer = throw IllegalArgumentException("Tokenizer ($name) not found")
