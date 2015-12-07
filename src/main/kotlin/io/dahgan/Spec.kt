package io.dahgan

/**
 * ** Spec productions
 *
 * These are copied directly from the YAML specification, with some changes to adapt to Kotlin language syntax and
 * also ease some of the decisions.
 *
 * See: http://www.yaml.org/spec/1.2/spec.html
 */

/**
 * 5.1 Character Set
 */

/**
 * [1] c-printable ::=  #x9 | #xA | #xD | [#x20-#x7E]          /* 8 bit */
 *                    | #x85 | [#xA0-#xD7FF] | [#xE000-#xFFFD] /* 16 bit */
 *                    | [#x10000-#x10FFFF]                     /* 32 bit */
 *
 * */
val `c-printable` = of(0x9) or 0xA or 0xD or 0x20..0x7E or 0x85 or 0xA0..0xD7FF or 0xE000..0xFFFD or 0x10000..0x10FFFF

/**
 * [2] nb-json ::= #x9 | [#x20-#x10FFFF]
 */
val `nb-json` = of(0x9) or 0x20..0x10FFFF

/**
 * 5.2 Character Encodings
 */

/**
 *  [3] c-byte-order-mark ::= #xFEFF
 */
val `c-byte-order-mark` = bom(0xFEFF)

/**
 * 5.3 Indicator Characters
 */

/**
 * [4] c-sequence-entry ::= “-”
 */
val `c-sequence-entry` = indicator('-')
/**
 * [5] c-mapping-key ::= “?”
 */
val `c-mapping-key` = indicator('?')
/**
 * [6] c-mapping-value ::= “:”
 */
val `c-mapping-value` = indicator(':')

/**
 * [7] c-collect-entry ::= “,”
 */
val `c-collect-entry` = indicator(',')
/**
 * [8] c-sequence-start ::= “[”
 */
val `c-sequence-start` = indicator('[')
/**
 * [9] c-sequence-end ::= “]”
 */
val `c-sequence-end` = indicator(']')
/**
 * [10] c-mapping-start ::= “{”
 */
val `c-mapping-start` = indicator('{')
/**
 * [11] c-mapping-end ::= “}”
 */
val `c-mapping-end` = indicator('}')

/**
 * [12] c-comment ::= “#”
 */
val `c-comment` = indicator('#')

/**
 * [13] c-anchor ::= “&”
 */
val `c-anchor` = indicator('&')
/**
 * [14] c-alias ::= “*”
 */
val `c-alias` = indicator('*')
/**
 * [15] c-tag ::= “!”
 */
val `c-tag` = indicator('!')

/**
 * [16] c-literal ::= “|”
 */
val `c-literal` = indicator('|')
/**
 * [17] c-folded ::= “>”
 */
val `c-folded` = indicator('>')

/**
 * [18] c-single-quote ::= “'”
 */
val `c-single-quote` = indicator('\'')
/**
 * [19] c-double-quote ::= “"”
 */
val `c-double-quote` = indicator('"')

/**
 * [20] c-directive ::= “%”
 */
val `c-directive` = indicator('%')

/**
 * [21] c-reserved ::= “@” | “`”
 */
val `c-reserved` = indicator('@' or '`')

/**
 * [22] c-indicator ::= “-” | “?” | “:” | “,” | “[” | “]” | “{” | “}”
 *                     | “#” | “&” | “*” | “!” | “|” | “>” | “'” | “"”
 *                     | “%” | “@” | “`”
 */
val `c-indicator` = `c-sequence-entry` or `c-mapping-key` or `c-mapping-value` or `c-collect-entry` or
        `c-sequence-start` or `c-sequence-end` or `c-mapping-start` or `c-mapping-end` or `c-comment` or `c-anchor` or
        `c-alias` or `c-tag` or `c-literal` or `c-folded` or `c-single-quote` or `c-double-quote` or `c-directive` or `c-reserved`

/**
 * [23] c-flow-indicator ::= “,” | “[” | “]” | “{” | “}”
 */
val `c-flow-indicator` = `c-collect-entry` or `c-sequence-start` or `c-sequence-end` or `c-mapping-start` or `c-mapping-end`

/**
 * 5.4 Line Break Characters
 */

/**
 * [24] b-line-feed ::= #xA    /* LF */
 */
val `b-line-feed` = of(0xA)
/**
 * [25] b-carriage-return ::= #xD    /* CR */
 */
val `b-carriage-return` = of(0xD)
/**
 * [26] b-char ::= b-line-feed | b-carriage-return
 */
val `b-char` = `b-line-feed` or `b-carriage-return`

/**
 * [27] nb-char ::= c-printable - b-char - c-byte-order-mark
 */
val `nb-char` = `c-printable` not `b-char` not `c-byte-order-mark`

/**
 * [28] b-break ::= (b-carriage-return b-line-feed) /* DOS, Windows */
 *                 | b-carriage-return                 /* MacOS upto 9.x */
 *                 | b-line-feed                       /* UNIX, MacOS X */
 */
val `b-break` = ((`b-carriage-return`  and `b-line-feed`) or `b-carriage-return` or `b-line-feed`) and nextLine()

/**
 * [29] b-as-line-feed ::= b-break
 */
val `b-as-line-feed` = token(Code.LineFeed, `b-break`)

/**
 * [30] b-non-content ::= b-break
 */
val `b-non-content` = token(Code.Break, `b-break`)

/**
 * 5.5 White Space Characters
 */

/**
 * [31] s-space ::= #x20 /* SP */
 */
val `s-space` = of(0x20)
/**
 * [32] s-tab ::= #x9  /* TAB */
 */
val `s-tab` = of(0x9)
/**
 * [33] s-white ::= s-space | s-tab
 */
val `s-white` = `s-space` or `s-tab`

/**
 * [34] ns-char ::= nb-char - s-white
 */
val `ns-char` = `nb-char` not `s-white`

/**
 * 5.6 Miscellaneous Characters
 */

/**
 * [35] ns-dec-digit ::= [#x30-#x39] /* 0-9 */
 */
val `ns-dec-digit` = of(0x30..0x39)

/**
 * [36] ns-hex-digit ::=  ns-dec-digit
 *                      | [#x41-#x46] /* A-F */ | [#x61-#x66] /* a-f */
 */
val `ns-hex-digit` = `ns-dec-digit` or 0x41..0x46 or 0x61..0x66

/**
 * [37] ns-ascii-letter ::= [#x41-#x5A] /* A-Z */ | [#x61-#x7A] /* a-z */
 */
val `ns-ascii-letter` = 0x41..0x5A or 0x61..0x7A

/**
 * [38] ns-word-char ::= ns-dec-digit | ns-ascii-letter | “-”
 */
val `ns-word-char` = `ns-dec-digit` or `ns-ascii-letter` or '-'

/**
 * [39] ns-uri-char ::=  “%” ns-hex-digit ns-hex-digit | ns-word-char | “#”
 *                     | “;” | “/” | “?” | “:” | “@” | “&” | “=” | “+” | “$” | “,”
 *                     | “_” | “.” | “!” | “~” | “*” | “'” | “(” | “)” | “[” | “]”
 */
val `ns-uri-char` = "escape" cho
        ('%' cmt "escape" and `ns-hex-digit` and `ns-hex-digit` or `ns-word-char` or '#' or
                ';' or '/' or '?' or ':' or '@' or '&' or '=' or '+' or '$' or ',' or
                '_' or '.' or '!' or '~' or '*' or '\'' or '(' or ')' or '[' or ']')

/**
 * [40] ns-tag-char ::= ns-uri-char - “!” - c-flow-indicator
 */
val `ns-tag-char` = `ns-uri-char` not `c-tag` not `c-flow-indicator`

/**
 * 5.7 Escaped Characters
 */

/**
 * [41] c-escape ::= “\”
 */
val `c-escape` = indicator('\\')

/**
 * [42] ns-esc-null ::= “0”
 */
val `ns-esc-null` = meta('0')
/**
 * [43] ns-esc-bell ::= “a”
 */
val `ns-esc-bell` = meta('a')
/**
 * [44] ns-esc-backspace ::= “b”
 */
val `ns-esc-backspace` = meta('b')
/**
 * [45] ns-esc-horizontal-tab ::= “t” | #x9
 */
val `ns-esc-horizontal-tab` = meta('t' or 0x9)
/**
 * [46] ns-esc-line-feed ::= “n”
 */
val `ns-esc-line-feed` = meta('n')
/**
 * [47] ns-esc-vertical-tab ::= “v”
 */
val `ns-esc-vertical-tab` = meta('v')
/**
 * [48] ns-esc-form-feed ::= “f”
 */
val `ns-esc-form-feed` = meta('f')
/**
 * [49] ns-esc-carriage-return ::= “r”
 */
val `ns-esc-carriage-return` = meta('r')
/**
 * [50] ns-esc-escape ::= “e”
 */
val `ns-esc-escape` = meta('e')
/**
 * [51] ns-esc-space ::= #x20
 */
val `ns-esc-space` = meta(0x20)
/**
 * [52] ns-esc-double-quote ::= “"”
 */
val `ns-esc-double-quote` = meta('"')
/**
 * [53] ns-esc-slash ::= “/”
 */
val `ns-esc-slash` = meta('/')
/**
 * [54] ns-esc-backslash ::= “\”
 */
val `ns-esc-backslash` = meta('\\')
/**
 * [55] ns-esc-next-line ::= “N”
 */
val `ns-esc-next-line` = meta('N')
/**
 * [56] ns-esc-non-breaking-space ::= “_”
 */
val `ns-esc-non-breaking-space` = meta('_')
/**
 * [57] ns-esc-line-separator ::= “L”
 */
val `ns-esc-line-separator` = meta('L')
/**
 * [58] ns-esc-paragraph-separator ::= “P”
 */
val `ns-esc-paragraph-separator` = meta('P')
/**
 * [59] ns-esc-8-bit ::= “x”
 *                       ( ns-hex-digit × 2 )
 */
val `ns-esc-8-bit` = indicator('x') cmt "escaped" and meta(`ns-hex-digit` tms 2)
/**
 * [60] ns-esc-16-bit ::= “u”
 *                        ( ns-hex-digit × 4 )
 */
val `ns-esc-16-bit` = indicator('u') cmt "escaped" and meta(`ns-hex-digit` tms 4)
/**
 * [61] ns-esc-32-bit ::= “U”
 *                        ( ns-hex-digit × 8 )
 */
val `ns-esc-32-bit` = indicator('U') cmt "escaped" and meta(`ns-hex-digit` tms 8)

/**
 * [62] c-ns-esc-char ::= “\”
 *                        ( ns-esc-null | ns-esc-bell | ns-esc-backspace
 *                        | ns-esc-horizontal-tab | ns-esc-line-feed
 *                        | ns-esc-vertical-tab | ns-esc-form-feed
 *                        | ns-esc-carriage-return | ns-esc-escape | ns-esc-space
 *                        | ns-esc-double-quote | ns-esc-slash | ns-esc-backslash
 *                        | ns-esc-next-line | ns-esc-non-breaking-space
 *                        | ns-esc-line-separator | ns-esc-paragraph-separator
 *                        | ns-esc-8-bit | ns-esc-16-bit | ns-esc-32-bit )
 */
val `c-ns-esc-char` = wrapTokens(Code.BeginEscape, Code.EndEscape,
        `c-escape` cmt "escape" and ("escaped" cho (
                `ns-esc-null` or `ns-esc-bell` or `ns-esc-backspace` or
                        `ns-esc-horizontal-tab` or `ns-esc-line-feed` or
                        `ns-esc-vertical-tab` or `ns-esc-form-feed` or
                        `ns-esc-carriage-return` or `ns-esc-escape` or `ns-esc-space` or
                        `ns-esc-double-quote` or `ns-esc-slash` or `ns-esc-backslash` or
                        `ns-esc-next-line` or `ns-esc-non-breaking-space` or
                        `ns-esc-line-separator` or `ns-esc-paragraph-separator` or
                        `ns-esc-8-bit` or `ns-esc-16-bit` or `ns-esc-32-bit`)))

/**
 * 6.1 Indentation Spaces
 */

/**
 * [63] s-indent(n) ::= s-space × n
 */
fun `s-indent`(n: Int) = token(Code.Indent, `s-space` tms n)

/**
 * [64] s-indent(<n) ::= s-space × m /* Where m < n */
 */
fun `s-indent-lt`(n: Int) = token(Code.Indent, `s-space` lms n)

/**
 * [65] s-indent(≤n) ::= s-space × m /* Where m ≤ n */
 */
fun `s-indent-le`(n: Int) = token(Code.Indent, `s-space` lms n + 1)

/**
 * 6.2 Separation Spaces
 */

/**
 * [66] s-separate-in-line ::= s-white+ | /* Start of line */
 */
val `s-separate-in-line` = token(Code.White, oom(`s-white`)) or sol()

/**
 * 6.3 Line Prefixes
 */

/**
 * [67] s-line-prefix(n,c) ::= c = block-out ⇒ s-block-line-prefix(n)
 *                             c = block-in  ⇒ s-block-line-prefix(n)
 *                             c = flow-out  ⇒ s-flow-line-prefix(n)
 *                             c = flow-in   ⇒ s-flow-line-prefix(n)
 */
fun `s-line-prefix`(n: Int, c: Context) = when (c) {
    Context.BlockOut -> `s-block-line-prefix`(n)
    Context.BlockIn -> `s-block-line-prefix`(n)
    Context.FlowOut -> `s-flow-line-prefix`(n)
    Context.FlowIn -> `s-flow-line-prefix`(n)
    else -> throw IllegalArgumentException("invalid context: $c")
}

/**
 * [68] s-block-line-prefix(n) ::= s-indent(n)
 */
fun `s-block-line-prefix`(n: Int) = `s-indent`(n)

/**
 * [69] s-flow-line-prefix(n) ::= s-indent(n) s-separate-in-line?
 */
fun `s-flow-line-prefix`(n: Int) = `s-indent`(n) and opt(`s-separate-in-line`)

/**
 * 6.4 Empty Lines
 */

/**
 * [70] l-empty(n,c) ::= ( s-line-prefix(n,c) | s-indent(<n) )
 *                       b-as-line-feed
 */
fun `l-empty`(n: Int, c: Context) = (`s-line-prefix`(n, c) or `s-indent-lt`(n))  and `b-as-line-feed`

/**
 * 6.5 Line Folding
 */

/**
 * [71] b-l-trimmed(n,c) ::= b-non-content l-empty(n,c)+
 */
fun `b-l-trimmed`(n: Int, c: Context) = `b-non-content` and oom(`l-empty`(n, c))

/**
 * [72] b-as-space ::= b-break
 */
val `b-as-space` = token(Code.LineFold, `b-break`)

/**
 * [73] b-l-folded(n,c) ::= b-l-trimmed(n,c) | b-as-space
 */
fun `b-l-folded`(n: Int, c: Context) = `b-l-trimmed`(n, c) or `b-as-space`

/**
 * [74] s-flow-folded(n) ::= s-separate-in-line? b-l-folded(n,flow-in)
 *                           s-flow-line-prefix(n)
 */
fun `s-flow-folded`(n: Int) = opt(`s-separate-in-line`) and `b-l-folded`(n, Context.FlowIn) and `s-flow-line-prefix`(n)

/**
 * 6.6 Comments
 */

/**
 * [75] c-nb-comment-text ::= “#” nb-char*
 */
val `c-nb-comment-text` = wrapTokens(Code.BeginComment, Code.EndComment, `c-comment` and meta(zom(`nb-char`)))

/**
 * [76] b-comment ::= b-non-content | /* End of file */
 */
val `b-comment` = `b-non-content` or eof()

/**
 * [77] s-b-comment ::= ( s-separate-in-line c-nb-comment-text? )?
 *                      b-comment
 */
val `s-b-comment` = opt((`s-separate-in-line` and opt(`c-nb-comment-text`))) and `b-comment`

/**
 * [78] l-comment ::= s-separate-in-line c-nb-comment-text? b-comment
 */
val `l-comment` = `s-separate-in-line` and opt(`c-nb-comment-text`) and `b-comment`

/**
 * [79] s-l-comments ::= ( s-b-comment | /* Start of line */ )
 *                       l-comment*
 */
val `s-l-comments` = (`s-b-comment` or sol()) and zom(nonEmpty(`l-comment`))

/**
 * 6.7 Separation Lines
 */

/**
 * 80] s-separate(n,c) ::= c = block-out ⇒ s-separate-lines(n)
 *                         c = block-in  ⇒ s-separate-lines(n)
 *                         c = flow-out  ⇒ s-separate-lines(n)
 *                         c = flow-in   ⇒ s-separate-lines(n)
 *                         c = block-key ⇒ s-separate-in-line
 *                         c = flow-key  ⇒ s-separate-in-line
 */
fun `s-separate`(n: Int, c: Context) = when (c) {
    Context.BlockOut -> `s-separate-lines`(n)
    Context.BlockIn -> `s-separate-lines`(n)
    Context.FlowOut -> `s-separate-lines`(n)
    Context.FlowIn -> `s-separate-lines`(n)
    Context.BlockKey -> `s-separate-in-line`
    Context.FlowKey -> `s-separate-in-line`
}

/**
 * [81] s-separate-lines(n) ::= ( s-l-comments s-flow-line-prefix(n) )
 *                             | s-separate-in-line
 */
fun `s-separate-lines`(n: Int) = `s-l-comments` and `s-flow-line-prefix`(n) or `s-separate-in-line`

/**
 * 6.8 Directives
 */

/**
 * [82] l-directive ::= “%”
 *                     ( ns-yaml-directive
 *                     | ns-tag-directive
 *                     | ns-reserved-directive )
 *                     s-l-comments
 */
val `l-directive` = (wrapTokens(Code.BeginDirective, Code.EndDirective, (`c-directive` cmt "doc") and
        ("directive" cho (`ns-yaml-directive`() or `ns-tag-directive`() or `ns-reserved-directive`())))) and `s-l-comments`

/**
 * [83] ns-reserved-directive ::= ns-directive-name
 *                                ( s-separate-in-line ns-directive-parameter )*
 */
fun `ns-reserved-directive`() = `ns-directive-name`() and zom(`s-separate-in-line` and `ns-directive-parameter`())

/**
 * [84] ns-directive-name ::= ns-char+
 */
fun `ns-directive-name`() = meta(oom(`ns-char`))

/**
 * [85] ns-directive-parameter ::= ns-char+
 */
fun `ns-directive-parameter`() = meta(oom(`ns-char`))

/**
 * 6.8.1 Yaml Directives
 */

/**
 * [86] ns-yaml-directive ::= “Y” “A” “M” “L”
 *                            s-separate-in-line ns-yaml-version
 */
fun `ns-yaml-directive`() = (meta('Y' and 'A' and 'M' and 'L') cmt "directive") and `s-separate-in-line` and `ns-yaml-version`()

/**
 * [87] ns-yaml-version ::= ns-dec-digit+ “.” ns-dec-digit+
 */
fun `ns-yaml-version`() = meta(oom(`ns-dec-digit`) and '.' and oom(`ns-dec-digit`))

/**
 * 6.8.2 Tag Directives
 */

/**
 * [88] ns-tag-directive ::= “T” “A” “G”
 *                           s-separate-in-line c-tag-handle
 *                           s-separate-in-line ns-tag-prefix
 */
fun `ns-tag-directive`() = (meta('T' and 'A' and 'G') cmt "directive") and
        `s-separate-in-line` and `c-tag-handle`() and `s-separate-in-line` and `ns-tag-prefix`()

/**
 * 6.8.2.1 Tag Handles
 */

/**
 * [89] c-tag-handle ::=  c-named-tag-handle
 *                      | c-secondary-tag-handle
 *                      | c-primary-tag-handle
 */
fun `c-tag-handle`() = `c-named-tag-handle`() or `c-secondary-tag-handle`() or `c-primary-tag-handle`()

/**
 * [90] c-primary-tag-handle ::= “!”
 */
fun `c-primary-tag-handle`() = wrapTokens(Code.BeginHandle, Code.EndHandle, `c-tag`)

/**
 * [91] c-secondary-tag-handle ::= “!” “!”
 */
fun `c-secondary-tag-handle`() = wrapTokens(Code.BeginHandle, Code.EndHandle, `c-tag` and `c-tag`)

/**
 * [92] c-named-tag-handle ::= “!” ns-word-char+ “!”
 */
fun `c-named-tag-handle`() = wrapTokens(Code.BeginHandle, Code.EndHandle, `c-tag` and meta(oom(`ns-word-char`) and `c-tag`))

/**
 * 6.8.2.2 Tag Prefixes
 */

/**
 * [93] ns-tag-prefix ::= c-ns-local-tag-prefix | ns-global-tag-prefix
 */
fun `ns-tag-prefix`() = wrapTokens(Code.BeginTag, Code.EndTag, `c-ns-local-tag-prefix`() or `ns-global-tag-prefix`())

/**
 * [94] c-ns-local-tag-prefix ::= “!” ns-uri-char*
 */
fun `c-ns-local-tag-prefix`() = `c-tag` and meta(zom(`ns-uri-char`))

/**
 * [95] ns-global-tag-prefix ::= ns-tag-char ns-uri-char*
 */
fun `ns-global-tag-prefix`() = meta(`ns-tag-char` and zom(`ns-uri-char`))

/**
 * 6.9 Node Properties
 */

/**
 * [96] c-ns-properties(n,c) ::=  ( c-ns-tag-property
 *                                  ( s-separate(n,c) c-ns-anchor-property )? )
 *                              | ( c-ns-anchor-property
 *                                  ( s-separate(n,c) c-ns-tag-property )? )
 */
fun `c-ns-properties`(n: Int, c: Context) = wrapTokens(Code.BeginProperties, Code.EndProperties,
        (`c-ns-tag-property`() and opt(`s-separate`(n, c) and `c-ns-anchor-property`)) or
                (`c-ns-anchor-property` and opt(`s-separate`(n, c) and `c-ns-tag-property`())))

/**
 * 6.9.1 Node Tags
 */

/**
 * [97] c-ns-tag-property ::=  c-verbatim-tag
 *                           | c-ns-shorthand-tag
 *                           | c-non-specific-tag
 */
fun `c-ns-tag-property`() = wrapTokens(Code.BeginTag, Code.EndTag, `c-verbatim-tag`() or `c-ns-shorthand-tag`() or `c-non-specific-tag`())

/**
 * [98] c-verbatim-tag ::= “!” “<” ns-uri-char+ “>”
 */
fun `c-verbatim-tag`() = `c-tag` and indicator('<')  and meta(oom(`ns-uri-char`)) and indicator('>')

/**
 * [99] c-ns-shorthand-tag ::= c-tag-handle ns-tag-char+
 */
fun `c-ns-shorthand-tag`() = `c-tag-handle`() and meta(oom(`ns-tag-char`))

/**
 * [100] c-non-specific-tag ::= “!”
 */
fun `c-non-specific-tag`() = `c-tag`

/**
 * 6.9.2 Node Anchors
 */

/**
 * [101] c-ns-anchor-property ::= “&” ns-anchor-name
 */
val `c-ns-anchor-property` = wrapTokens(Code.BeginAnchor, Code.EndAnchor, `c-anchor` and `ns-anchor-name`())

/**
 * [102] ns-anchor-char ::= ns-char - c-flow-indicator
 */
fun `ns-anchor-char`() = `ns-char` not `c-flow-indicator`

/**
 * [103] ns-anchor-name ::= ns-anchor-char+
 */
fun `ns-anchor-name`() = meta(oom(`ns-anchor-char`()))

/**
 * 7.1 Alias Nodes
 */

/**
 * [104] c-ns-alias-node ::= “*” ns-anchor-name
 */
val `c-ns-alias-node` = wrapTokens(Code.BeginAlias, Code.EndAlias, (`c-alias` cmt "node") and `ns-anchor-name`())

/**
 * 7.2 Empty Nodes
 */

/**
 * [105] e-scalar ::= /* Empty */
 */
val `e-scalar` = wrapTokens(Code.BeginScalar, Code.EndScalar, empty())

/**
 * [106] e-node ::= e-scalar
 */
val `e-node` = wrapTokens(Code.BeginNode, Code.EndNode, `e-scalar`)

/**
 * 7.3.1 Double Quoted Style
 */

/**
 * [107] nb-double-char ::= c-ns-esc-char | ( nb-json - “\” - “"” )
 */
val `nb-double-char` = ("escape" cho (`c-ns-esc-char` or (`nb-json` not `c-escape` not `c-double-quote`)))
/**
 * [108] ns-double-char ::= nb-double-char - s-white
 */
val `ns-double-char` = `nb-double-char` not `s-white`

/**
 * [109] c-double-quoted(n,c) ::= “"” nb-double-text(n,c) “"”
 */
fun `c-double-quoted`(n: Int, c: Context) = wrapTokens(Code.BeginScalar, Code.EndScalar, (`c-double-quote` cmt "node") and
        text(`nb-double-text`(n, c)) and `c-double-quote`)

/**
 * [110] nb-double-text(n,c) ::= c = flow-out  ⇒ nb-double-multi-line(n)
 *                               c = flow-in   ⇒ nb-double-multi-line(n)
 *                               c = block-key ⇒ nb-double-one-line
 *                               c = flow-key  ⇒ nb-double-one-line
 */
fun `nb-double-text`(n: Int, c: Context) = when (c) {
    Context.FlowOut -> `nb-double-multi-line`(n)
    Context.FlowIn -> `nb-double-multi-line`(n)
    Context.BlockKey -> `nb-double-one-line`
    Context.FlowKey -> `nb-double-one-line`
    else -> throw IllegalArgumentException("unexpected")
}

/**
 * [111] nb-double-one-line ::= nb-double-char*
 */
val `nb-double-one-line` = zom(`nb-double-char`)

/**
 * [112] s-double-escaped(n) ::= s-white* “\” b-non-content
 *                               l-empty(n,flow-in)* s-flow-line-prefix(n)
 */
fun `s-double-escaped`(n: Int) = zom(`s-white`) and
        wrapTokens(Code.BeginEscape, Code.EndEscape, (`c-escape` cmt "escape") and `b-non-content`) and
        zom(`l-empty`(n, Context.FlowIn)) and `s-flow-line-prefix`(n)

/**
 * [113] s-double-break(n) ::= s-double-escaped(n) | s-flow-folded(n)
 */
fun `s-double-break`(n: Int) = "escape" cho (`s-double-escaped`(n) or `s-flow-folded`(n))

/**
 * [114] nb-ns-double-in-line ::= ( s-white* ns-double-char )*
 */
val `nb-ns-double-in-line` = zom(zom(`s-white`) and `ns-double-char`)

/**
 * [115] s-double-next-line(n) ::= s-double-break(n)
 *                                 ( ns-double-char nb-ns-double-in-line
 *                                  ( s-double-next-line(n) | s-white* ) )?
 */
fun `s-double-next-line`(n: Int): Parser = `s-double-break`(n) and opt(`ns-double-char` and `nb-ns-double-in-line` and
        (Parser { state -> `s-double-next-line`(n)(state) } or zom(`s-white`)))

/**
 * [116] nb-double-multi-line(n) ::= nb-ns-double-in-line
 *                                  ( s-double-next-line(n) | s-white* )
 */
fun `nb-double-multi-line`(n: Int) = `nb-ns-double-in-line` and (`s-double-next-line`(n) or zom(`s-white`))

/**
 * 7.3.2 Single Quoted Style
 */

/**
 * [117] c-quoted-quote ::= “'” “'”
 */
val `c-quoted-quote` = wrapTokens(Code.BeginEscape, Code.EndEscape, `c-single-quote` cmt "escape" and meta('\''))
/**
 * [118] nb-single-char ::= c-quoted-quote | ( nb-json - “'” )
 */
val `nb-single-char` = "escape" cho (`c-quoted-quote` or (`nb-json` not `c-single-quote`))
/**
 * [119] ns-single-char ::= nb-single-char - s-white
 */
val `ns-single-char` = `nb-single-char` not `s-white`

/**
 * [120] c-single-quoted(n,c) ::= “'” nb-single-text(n,c) “'”
 */
fun `c-single-quoted`(n: Int, c: Context) = wrapTokens(Code.BeginScalar, Code.EndScalar,
        (`c-single-quote` cmt "node") and text(`nb-single-text`(n, c)) and `c-single-quote`)

/**
 * [121] nb-single-text(n,c) ::= c = flow-out  ⇒ nb-single-multi-line(n)
 *                               c = flow-in   ⇒ nb-single-multi-line(n)
 *                               c = block-key ⇒ nb-single-one-line
 *                               c = flow-key  ⇒ nb-single-one-line
 */
fun `nb-single-text`(n: Int, c: Context) = when (c) {
    Context.FlowOut -> `nb-single-multi-line`(n)
    Context.FlowIn -> `nb-single-multi-line`(n)
    Context.BlockKey -> `nb-single-one-line`
    Context.FlowKey -> `nb-single-one-line`
    else -> throw IllegalArgumentException("unexpected")
}

/**
 * [122] nb-single-one-line ::= nb-single-char*
 */
val `nb-single-one-line` = zom(`nb-single-char`)

/**
 * [123] nb-ns-single-in-line ::= ( s-white* ns-single-char )*
 */
val `nb-ns-single-in-line` = zom(zom(`s-white`) and `ns-single-char`)

/**
 * [124] s-single-next-line(n) ::= s-flow-folded(n)
 *                                 ( ns-single-char nb-ns-single-in-line
 *                                   ( s-single-next-line(n) | s-white* ) )?
 */
fun `s-single-next-line`(n: Int): Parser = `s-flow-folded`(n) and (opt(`ns-single-char` and `nb-ns-single-in-line` and
        (Parser { state -> `s-single-next-line`(n)(state) } or zom(`s-white`))))

/**
 * [125] nb-single-multi-line(n) ::= nb-ns-single-in-line
 *                                  ( s-single-next-line(n) | s-white* )
 */
fun `nb-single-multi-line`(n: Int) = `nb-ns-single-in-line` and (`s-single-next-line`(n) or zom(`s-white`))

/**
 * 7.3.3 Plain Style
 */

/**
 * [126] ns-plain-first(c) ::=   ( ns-char - c-indicator )
 *                            | ( ( “?” | “:” | “-” )
 *                              /* Followed by an ns-plain-safe(c)) */ )
 */
@Suppress("UNUSED-PARAMETER")
fun `ns-plain-first`(c: Context) = (`ns-char` not `c-indicator`) or (('?' or ':' or '-') and pla(`ns-char`))

/**
 * [127] ns-plain-safe(c) ::= c = flow-out  ⇒ ns-plain-safe-out
 *                            c = flow-in   ⇒ ns-plain-safe-in
 *                            c = block-key ⇒ ns-plain-safe-out
 *                            c = flow-key  ⇒ ns-plain-safe-in
 */
fun `ns-plain-safe`(c: Context) = when (c) {
    Context.FlowOut -> `ns-plain-safe-out`
    Context.FlowIn -> `ns-plain-safe-in`
    Context.BlockKey -> `ns-plain-safe-out`
    Context.FlowKey -> `ns-plain-safe-in`
    else -> throw IllegalArgumentException("unexpected")
}

/**
 * [128] ns-plain-safe-out ::= ns-char
 */
val `ns-plain-safe-out` = `ns-char` not `c-mapping-value` not `c-comment`
/**
 * [129] ns-plain-safe-in ::= ns-char - c-flow-indicator
 */
val `ns-plain-safe-in` = `ns-plain-safe-out` not `c-flow-indicator`

/**
 * [130] ns-plain-char(c) ::=  ( ns-plain-safe(c) - “:” - “#” )
 *                           | ( /* An ns-char preceding */ “#” )
 *                           | ( “:” /* Followed by an ns-plain-safe(c) */ )
 */
fun `ns-plain-char`(c: Context) = `ns-plain-safe`(c) or (plb(`ns-char`) and '#') or (':' and pla(`ns-char`))

/**
 * [131] ns-plain(n,c) ::= c = flow-out  ⇒ ns-plain-multi-line(n,c)
 *                         c = flow-in   ⇒ ns-plain-multi-line(n,c)
 *                         c = block-key ⇒ ns-plain-one-line(c)
 *                         c = flow-key  ⇒ ns-plain-one-line(c)
 */
fun `ns-plain`(n: Int, c: Context) = wrapTokens(Code.BeginScalar, Code.EndScalar, text(when (c) {
    Context.FlowOut -> `ns-plain-multi-line`(n, c)
    Context.FlowIn -> `ns-plain-multi-line`(n, c)
    Context.BlockKey -> `ns-plain-one-line`(c)
    Context.FlowKey -> `ns-plain-one-line`(c)
    else -> throw IllegalArgumentException("unexpected")
}))

/**
 * [132] nb-ns-plain-in-line(c) ::= ( s-white* ns-plain-char(c) )*
 */
fun `nb-ns-plain-in-line`(c: Context) = zom(zom(`s-white`) and `ns-plain-char`(c))

/**
 * [133] ns-plain-one-line(c) ::= ns-plain-first(c) nb-ns-plain-in-line(c)
 */
fun `ns-plain-one-line`(c: Context) = `ns-plain-first`(c) cmt "node" and `nb-ns-plain-in-line`(c)

/**
 * [134] s-ns-plain-next-line(n,c) ::= s-flow-folded(n)
 *                                     ns-plain-char(c) nb-ns-plain-in-line(c)
 */
fun `s-ns-plain-next-line`(n: Int, c: Context) = `s-flow-folded`(n) and `ns-plain-char`(c) and `nb-ns-plain-in-line`(c)

/**
 * [135] ns-plain-multi-line(n,c) ::= ns-plain-one-line(c)
 *                                    s-ns-plain-next-line(n,c)*
 */
fun `ns-plain-multi-line`(n: Int, c: Context) = `ns-plain-one-line`(c) and zom(`s-ns-plain-next-line`(n, c))

/**
 * 7.4 Flow Collection Styles
 */

/** [136] in-flow(c) ::= c = flow-out  ⇒ flow-in
 *                       c = flow-in   ⇒ flow-in
 *                       c = block-key ⇒ flow-key
 *                       c = flow-key  ⇒ flow-key
 */
fun `in-flow`(c: Context) = when (c) {
    Context.FlowOut -> Context.FlowIn
    Context.FlowIn -> Context.FlowIn
    Context.BlockKey -> Context.FlowKey
    Context.FlowKey -> Context.FlowKey
    else -> throw IllegalArgumentException("unexpected")
}

/**
 * 7.4.1 Flow Sequences
 */

/**
 * [137] c-flow-sequence(n,c) ::= “[” s-separate(n,c)?
 *                                ns-s-flow-seq-entries(n,in-flow(c))? “]”
 */
fun `c-flow-sequence`(n: Int, c: Context) = wrapTokens(Code.BeginSequence, Code.EndSequence, (`c-sequence-start` cmt "node") and
        opt(`s-separate`(n, c)) and opt(`ns-s-flow-seq-entries`(n, `in-flow`(c))) and `c-sequence-end`)

/**
 * [138] ns-s-flow-seq-entries(n,c) ::= ns-flow-seq-entry(n,c) s-separate(n,c)?
 *                                      ( “,” s-separate(n,c)?
 *                                        ns-s-flow-seq-entries(n,c)? )?
 */
fun `ns-s-flow-seq-entries`(n: Int, c: Context): Parser = `ns-flow-seq-entry`(n, c) and opt(`s-separate`(n, c)) and
        opt(`c-collect-entry` and opt(`s-separate`(n, c)) and opt(Parser { state -> `ns-s-flow-seq-entries`(n, c)(state) }))

/**
 * [139] ns-flow-seq-entry(n,c) ::= ns-flow-pair(n,c) | ns-flow-node(n,c)
 */
fun `ns-flow-seq-entry`(n: Int, c: Context) = "pair" cho (`ns-flow-pair`(n, c) or ("node" cho `ns-flow-node`(n, c)))

/**
 * 7.4.2 Flow
 */

/**
 * [140] c-flow-mapping(n,c) ::= “{” s-separate(n,c)?
 *                               ns-s-flow-map-entries(n,in-flow(c))? “}”
 */
fun `c-flow-mapping`(n: Int, c: Context) = wrapTokens(Code.BeginMapping, Code.EndMapping, (`c-mapping-start` cmt "node") and
        opt(`s-separate`(n, c)) and opt(`ns-s-flow-map-entries`(n, `in-flow`(c))) and `c-mapping-end`)

/**
 * [141] ns-s-flow-map-entries(n,c) ::= ns-flow-map-entry(n,c) s-separate(n,c)?
 *                                      ( “,” s-separate(n,c)?
 *                                         ns-s-flow-map-entries(n,c)? )?
 */
fun `ns-s-flow-map-entries`(n: Int, c: Context): Parser = `ns-flow-map-entry`(n, c) and opt(`s-separate`(n, c)) and
        opt(`c-collect-entry` and opt(`s-separate`(n, c)) and opt(Parser { state -> `ns-s-flow-map-entries`(n, c)(state) }))

/**
 * [142] ns-flow-map-entry(n,c) ::=   ( “?” s-separate(n,c)
 *                                      ns-flow-map-explicit-entry(n,c) )
 *                                 | ns-flow-map-implicit-entry(n,c)
 */
fun `ns-flow-map-entry`(n: Int, c: Context) = wrapTokens(Code.BeginPair, Code.EndPair, "key" cho (((`c-mapping-key` cmt "key") and
        `s-separate`(n, c) and `ns-flow-map-explicit-entry`(n, c)) or `ns-flow-map-implicit-entry`(n, c)))

/**
 * [143] ns-flow-map-explicit-entry(n,c) ::=  ns-flow-map-implicit-entry(n,c)
 *                                          | ( e-node /* Key */
 *                                              e-node /* Value */ )
 */
fun `ns-flow-map-explicit-entry`(n: Int, c: Context) = `ns-flow-map-implicit-entry`(n, c) or (`e-node` and `e-node`)

/**
 * [144] ns-flow-map-implicit-entry(n,c) ::=  ns-flow-map-yaml-key-entry(n,c)
 *                                          | c-ns-flow-map-empty-key-entry(n,c)
 *                                          | c-ns-flow-map-json-key-entry(n,c)
 */
fun `ns-flow-map-implicit-entry`(n: Int, c: Context) = "pair" cho (`ns-flow-map-yaml-key-entry`(n, c) or
        `c-ns-flow-map-empty-key-entry`(n, c) or `c-ns-flow-map-json-key-entry`(n, c))

/**
 * [145] ns-flow-map-yaml-key-entry(n,c) ::= ns-flow-yaml-node(n,c)
 *                                           ( s-separate(n,c)?
 *                                             c-ns-flow-map-separate-value(n,c) )
 *                                          | e-node )
 */
fun `ns-flow-map-yaml-key-entry`(n: Int, c: Context) = (("node" cho `ns-flow-yaml-node`(n, c)) cmt "pair") and
        ((opt(`s-separate`(n, c)) and `c-ns-flow-map-separate-value`(n, c)) or `e-node`)

/**
 * [146] c-ns-flow-map-empty-key-entry(n,c) ::= e-node /* Key */
 *                                              c-ns-flow-map-separate-value(n,c)
 */
fun `c-ns-flow-map-empty-key-entry`(n: Int, c: Context) = `e-node` and `c-ns-flow-map-separate-value`(n, c)

/**
 * [147] c-ns-flow-map-separate-value(n,c) ::= “:” /* Not followed by an
 *                                                  ns-plain-safe(c) */
 *                                            ( ( s-separate(n,c) ns-flow-node(n,c) )
 *                                            | e-node /* Value */ )
 */
fun `c-ns-flow-map-separate-value`(n: Int, c: Context) = `c-mapping-value` and (nla(`ns-char`) cmt "pair") and
        ((`s-separate`(n, c) and `ns-flow-node`(n, c)) or `e-node`)

/**
 * [148] c-ns-flow-map-json-key-entry(n,c) ::= c-flow-json-node(n,c)
 *                                              ( ( s-separate(n,c)?
 *                                                  c-ns-flow-map-adjacent-value(n,c) )
 *                                            | e-node )
 */
fun `c-ns-flow-map-json-key-entry`(n: Int, c: Context) = (("node" cho `c-flow-json-node`(n, c)) cmt "pair") and
        ((opt(`s-separate`(n, c)) and `c-ns-flow-map-adjacent-value`(n, c)) or `e-node`)

/**
 * [149] c-ns-flow-map-adjacent-value(n,c) ::= “:” ( ( s-separate(n,c)?
 *                                                      ns-flow-node(n,c) )
 *                                                 | e-node ) /* Value */
 */
fun `c-ns-flow-map-adjacent-value`(n: Int, c: Context) = (`c-mapping-value` cmt "pair") and
        ((opt(`s-separate`(n, c)) and `ns-flow-node`(n, c)) or `e-node`)

/**
 * [150] ns-flow-pair(n,c) ::=  ( “?” s-separate(n,c)
 *                                  ns-flow-map-explicit-entry(n,c) )
 *                            | ns-flow-pair-entry(n,c)
 */
fun `ns-flow-pair`(n: Int, c: Context) = wrapTokens(Code.BeginMapping, Code.EndMapping, wrapTokens(Code.BeginPair, Code.EndPair,
        ((`c-mapping-key` cmt "pair") and `s-separate`(n, c) and `ns-flow-map-explicit-entry`(n, c)) or `ns-flow-pair-entry`(n, c)))

/**
 * [151] ns-flow-pair-entry(n,c) ::=  ns-flow-pair-yaml-key-entry(n,c)
 *                                  | c-ns-flow-map-empty-key-entry(n,c)
 *                                  | c-ns-flow-pair-json-key-entry(n,c)
 */
fun `ns-flow-pair-entry`(n: Int, c: Context) = `ns-flow-pair-yaml-key-entry`(n, c) or
        `c-ns-flow-map-empty-key-entry`(n, c) or
        `c-ns-flow-pair-json-key-entry`(n, c)

/**
 * [152] ns-flow-pair-yaml-key-entry(n,c) ::= ns-s-implicit-yaml-key(flow-key)
 *                                            c-ns-flow-map-separate-value(n,c)
 */
fun `ns-flow-pair-yaml-key-entry`(n: Int, c: Context) = `ns-s-implicit-yaml-key`(Context.FlowKey) and
        `c-ns-flow-map-separate-value`(n, c)

/**
 * [153] c-ns-flow-pair-json-key-entry(n,c) ::= c-s-implicit-json-key(flow-key)
 *                                              c-ns-flow-map-adjacent-value(n,c)
 */
fun `c-ns-flow-pair-json-key-entry`(n: Int, c: Context) = `c-s-implicit-json-key`(Context.FlowKey) and `c-ns-flow-map-adjacent-value`(n, c)

/**
 * [154] ns-s-implicit-yaml-key(c) ::= ns-flow-yaml-node(n/a,c) s-separate-in-line?
 *                                     /* At most 1024 characters altogether */
 */
fun `ns-s-implicit-yaml-key`(c: Context) = limitedTo("node" cho `ns-flow-yaml-node`(-1, c) and opt(`s-separate-in-line`), 1024)

/**
 * [155] c-s-implicit-json-key(c) ::= c-flow-json-node(n/a,c) s-separate-in-line?
 *                                    /* At most 1024 characters altogether */
 */
fun `c-s-implicit-json-key`(c: Context) = limitedTo("node" cho `c-flow-json-node`(-1, c) and opt(`s-separate-in-line`), 1024)
// 7.5 Flow Nodes

/**
 * [156] ns-flow-yaml-content(n,c) ::= ns-plain(n,c)
 */
fun `ns-flow-yaml-content`(n: Int, c: Context) = `ns-plain`(n, c)

/**
 * [157] c-flow-json-content(n,c) ::=  c-flow-sequence(n,c) | c-flow-mapping(n,c)
 *                                   | c-single-quoted(n,c) | c-double-quoted(n,c)
 */
fun `c-flow-json-content`(n: Int, c: Context) = Parser { state -> `c-flow-sequence`(n, c)(state) } or
        `c-flow-mapping`(n, c) or `c-single-quoted`(n, c) or `c-double-quoted`(n, c)

/**
 * [158] ns-flow-content(n,c) ::= ns-flow-yaml-content(n,c) | c-flow-json-content(n,c)
 */
fun `ns-flow-content`(n: Int, c: Context) = `ns-flow-yaml-content`(n, c) or Parser { state -> `c-flow-json-content`(n, c)(state) }

/**
 * [159] ns-flow-yaml-node(n,c) ::=  c-ns-alias-node
 *                                 | ns-flow-yaml-content(n,c)
 *                                 | ( c-ns-properties(n,c)
 *                                      ( ( s-separate(n,c)
 *                                          ns-flow-yaml-content(n,c) )
 *                                      | e-scalar ) )
 */
fun `ns-flow-yaml-node`(n: Int, c: Context) = wrapTokens(Code.BeginNode, Code.EndNode,
        (`c-ns-alias-node` or `ns-flow-yaml-content`(n, c) or (`c-ns-properties`(n, c) and
                ((`s-separate`(n, c) and `ns-flow-yaml-content`(n, c)) or `e-scalar`))))

/**
 * [160] c-flow-json-node(n,c) ::= ( c-ns-properties(n,c) s-separate(n,c) )?
 *                                 c-flow-json-content(n,c)
 */
fun `c-flow-json-node`(n: Int, c: Context) = wrapTokens(Code.BeginNode, Code.EndNode, opt(`c-ns-properties`(n, c) and
        `s-separate`(n, c)) and Parser { state -> `c-flow-json-content`(n, c)(state) })

/**
 * [161] ns-flow-node(n,c) ::=  c-ns-alias-node
 *                            | ns-flow-content(n,c)
 *                            | ( c-ns-properties(n,c)
 *                              ( ( s-separate(n,c)
 *                                  ns-flow-content(n,c) )
 *                                | e-scalar ) )
 */
fun `ns-flow-node`(n: Int, c: Context) = wrapTokens(Code.BeginNode, Code.EndNode, (`c-ns-alias-node` or `ns-flow-content`(n, c) or
        (`c-ns-properties`(n, c) and ((`s-separate`(n, c) and `ns-flow-content`(n, c)) or `e-scalar`))))

/**
 * 8.1.1 Block Scalar Headers
 */

/**
 * [162] c-b-block-header(m,t) ::= ( ( c-indentation-indicator(m)
 *                                     c-chomping-indicator(t) )
 *                                 | ( c-chomping-indicator(t)
 *                                     c-indentation-indicator(m) ) )
 *                                 s-b-comment
 */
fun `c-b-block-header`(n: Int) = "header" cho (`c-indentation-indicator`(n).snd("m", `c-chomping-indicator`()).
        snd("t", (`s-white` or `b-char`) omt "header") and `s-b-comment` and peekResult("m", "t") or
        `c-chomping-indicator`().snd("t", `c-indentation-indicator`(n)).snd("m", `s-b-comment`) and peekResult("m", "t"))

/**
 * 8.1.1.1 Block Indentation Indicator
 */

/**
 * [163] c-indentation-indicator(m) ::= ns-dec-digit ⇒ m = ns-dec-digit - #x30
 *                                      /* Empty */  ⇒ m = auto-detect()
 */
fun `c-indentation-indicator`(n: Int) = indicator(`ns-dec-digit` not '0') and asInt() or `detect-scalar-indentation`(n)

fun `detect-scalar-indentation`(n: Int) = peek(zom(`nb-char`) and
        opt(`b-non-content` and zom(`l-empty`(n, Context.BlockIn))) and `count-spaces`(-n))

fun `count-spaces`(n: Int): Parser = (`s-space` and Parser { state -> `count-spaces`(n + 1)(state) }) or result(Math.max(1, n))

/**
 * 8.1.1.2 Chomping Indicator
 */

/**
 * [164] c-chomping-indicator(t) ::= “-”         ⇒ t = strip
 *                                   “+”         ⇒ t = keep
 *                                   /* Empty */ ⇒ t = clip
 */
fun `c-chomping-indicator`() = (indicator('-') and result(Chomp.Strip)) or (indicator('+') and result(Chomp.Keep)) or result(Chomp.Clip)

fun `end-block-scalar`(t: Chomp) = when (t) {
    Chomp.Strip -> emptyToken(Code.EndScalar)
    Chomp.Clip -> emptyToken(Code.EndScalar)
    Chomp.Keep -> empty()
}

/**
 * [165] b-chomped-last(t) ::= t = strip ⇒ b-non-content | /* End of file */
 *                             t = clip  ⇒ b-as-line-feed | /* End of file */
 *                             t = keep  ⇒ b-as-line-feed | /* End of file */
 */
fun `b-chomped-last`(t: Chomp) = when (t) {
    Chomp.Strip -> emptyToken(Code.EndScalar) and `b-non-content`
    Chomp.Clip -> `b-as-line-feed` and emptyToken(Code.EndScalar)
    Chomp.Keep -> `b-as-line-feed`
}

/**
 * [166] l-chomped-empty(n,t) ::= t = strip ⇒ l-strip-empty(n)
 *                                t = clip  ⇒ l-strip-empty(n)
 *                                t = keep  ⇒ l-keep-empty(n)
 */
fun `l-chomped-empty`(n: Int, t: Chomp) = when (t) {
    Chomp.Strip -> `l-strip-empty`(n)
    Chomp.Clip -> `l-strip-empty`(n)
    Chomp.Keep -> `l-keep-empty`(n)
}

/**
 * [167] l-strip-empty(n) ::= ( s-indent(≤n) b-non-content )*
 *                            l-trail-comments(n)?
 */
fun `l-strip-empty`(n: Int) = zom(`s-indent-le`(n) and `b-non-content`) and opt(`l-trail-comments`(n))

/**
 * [168] l-keep-empty(n) ::= l-empty(n,block-in)*
 *                           l-trail-comments(n)?
 */
fun `l-keep-empty`(n: Int) = zom(`l-empty`(n, Context.BlockIn)) and emptyToken(Code.EndScalar) and opt(`l-trail-comments`(n))

/**
 * [169] l-trail-comments(n) ::= s-indent(<n) c-nb-comment-text b-comment
 *                               l-comment*
 */
fun `l-trail-comments`(n: Int) = `s-indent-lt`(n) and `c-nb-comment-text` and `b-comment` and zom(nonEmpty(`l-comment`))

/**
 * 8.1.2 Literal Style
 */

/**
 * [170] c-l+literal(n) ::= “|” c-b-block-header(m,t)
 *                           l-literal-content(n+m,t)
 */
fun `c-l+literal`(n: Int) = emptyToken(Code.BeginScalar) and (`c-literal` cmt "node") and
        prefixErrorWith(`c-b-block-header`(n), emptyToken(Code.EndScalar)) and
        text(Parser { state ->
            val m = state.yields["m"] as Int
            val t = state.yields["t"] as Chomp

            `l-literal-content`(n + m, t)(state)
        })

/**
 * [171] l-nb-literal-text(n) ::= l-empty(n,block-in)*
 *                                s-indent(n) nb-char+
 */
fun `l-nb-literal-text`(n: Int) = zom(`l-empty`(n, Context.BlockIn)) and `s-indent`(n) and oom(`nb-char`)

/**
 * [172] b-nb-literal-next(n) ::= b-as-line-feed
 *                                l-nb-literal-text(n)
 */
fun `b-nb-literal-next`(n: Int) = `b-as-line-feed` and `l-nb-literal-text`(n)

/**
 * [173] l-literal-content(n,t) ::= ( l-nb-literal-text(n) b-nb-literal-next(n)*
 *                                    b-chomped-last(t) )?
 *                                  l-chomped-empty(n,t)
 */
fun `l-literal-content`(n: Int, t: Chomp) = ((`l-nb-literal-text`(n) and zom(`b-nb-literal-next`(n)) and
        `b-chomped-last`(t)) or `end-block-scalar`(t)) and `l-chomped-empty`(n, t)

/**
 * 8.1.3 Folded Style
 */

/**
 * [174] c-l+folded(n) ::= “>” c-b-block-header(m,t)
 *                          l-folded-content(n+m,t)
 */
fun `c-l+folded`(n: Int) = emptyToken(Code.BeginScalar) and (`c-folded` cmt "node") and
        prefixErrorWith(`c-b-block-header`(n), emptyToken(Code.EndScalar)) and
        text(Parser { state ->
            val m = state.yields["m"] as Int
            val t = state.yields["t"] as Chomp

            `l-folded-content`(n + m, t)(state)
        })

/**
 * [175] s-nb-folded-text(n) ::= s-indent(n) ns-char nb-char*
 */
fun `s-nb-folded-text`(n: Int) = `s-indent`(n) and (`ns-char` cmt "fold") and zom(`nb-char`)

/**
 * [176] l-nb-folded-lines(n) ::= s-nb-folded-text(n)
 *                                ( b-l-folded(n,block-in) s-nb-folded-text(n) )*
 */
fun `l-nb-folded-lines`(n: Int) = `s-nb-folded-text`(n) and zom(`b-l-folded`(n, Context.BlockIn) and `s-nb-folded-text`(n))

/**
 * [177] s-nb-spaced-text(n) ::= s-indent(n) s-white nb-char*
 */
fun `s-nb-spaced-text`(n: Int) = `s-indent`(n) and (`s-white` cmt "fold") and zom(`nb-char`)

/**
 * [178] b-l-spaced(n) ::= b-as-line-feed
 *                         l-empty(n,block-in)*
 */
fun `b-l-spaced`(n: Int) = `b-as-line-feed` and zom(`l-empty`(n, Context.BlockIn))

/**
 * [179] l-nb-spaced-lines(n) ::= s-nb-spaced-text(n)
 *                                ( b-l-spaced(n) s-nb-spaced-text(n) )*
 */
fun `l-nb-spaced-lines`(n: Int) = `s-nb-spaced-text`(n) and zom(`b-l-spaced`(n) and `s-nb-spaced-text`(n))

/**
 * [180] l-nb-same-lines(n) ::= l-empty(n,block-in)*
 *                              ( l-nb-folded-lines(n) | l-nb-spaced-lines(n) )
 */
fun `l-nb-same-lines`(n: Int) = zom(`l-empty`(n, Context.BlockIn)) and
        ("fold" cho (`l-nb-folded-lines`(n) or `l-nb-spaced-lines`(n)))

/**
 * [181] l-nb-diff-lines(n) ::= l-nb-same-lines(n)
 *                              ( b-as-line-feed l-nb-same-lines(n) )*
 */
fun `l-nb-diff-lines`(n: Int) = `l-nb-same-lines`(n) and zom(`b-as-line-feed` and `l-nb-same-lines`(n))

/**
 * [182] l-folded-content(n,t) ::= ( l-nb-diff-lines(n) b-chomped-last(t) )?
 *                                 l-chomped-empty(n,t)
 */
fun `l-folded-content`(n: Int, t: Chomp) = ((`l-nb-diff-lines`(n) and `b-chomped-last`(t)) or `end-block-scalar`(t)) and
        `l-chomped-empty`(n, t)

/**
 * 8.2.1 Block Sequences
 */

fun `detect-collection-indentation`(n: Int) = peek(zom(nonEmpty(`l-comment`)) and `count-spaces`(-n))

val `detect-inline-indentation` = peek(`count-spaces`(0))

/**
 * [183] l+block-sequence(n) ::= ( s-indent(n+m) c-l-block-seq-entry(n+m) )+
 *                               /* For some fixed auto-detected m > 0 */
 */
fun `l+block-sequence`(n: Int) = `detect-collection-indentation`(n).snd("m",
        wrapTokens(Code.BeginSequence, Code.EndSequence, Parser { state ->
            val m = state.yields["m"] as Int
            oom(`s-indent`(n + m) and `c-l-block-seq-entry`(n + m))(state)
        }))

/**
 * [184] c-l-block-seq-entry(n) ::= “-” /* Not followed by an ns-char */
 *                                  s-l+block-indented(n,block-in)
 */
fun `c-l-block-seq-entry`(n: Int): Parser = `c-sequence-entry` and (nla(`ns-char`) cmt "node") and
        `s-l+block-indented`(n, Context.BlockIn)

/**
 * [185] s-l+block-indented(n,c) ::=  ( s-indent(m)
 *                                      ( ns-l-compact-sequence(n+1+m)
 *                                      | ns-l-compact-mapping(n+1+m) ) )
 *                                  | s-l+block-node(n,c)
 *                                  | ( e-node s-l-comments )
 */
fun `s-l+block-indented`(n: Int, c: Context): Parser = `detect-inline-indentation`.snd("m",
        (("node" cho (Parser { state ->
            val m = state.yields["m"] as Int
            (`s-indent`(m) and (`ns-l-in-line-sequence`(n + 1 + m) or `ns-l-in-line-mapping`(n + 1 + m)))(state)
        } or `s-l+block-node`(n, c) or `e-node` and opt(`s-l-comments`) and unparsed (n + 1))) recovery unparsed(n + 1)))

/**
 * [186] ns-l-compact-sequence(n) ::= c-l-block-seq-entry(n)
 *                                    ( s-indent(n) c-l-block-seq-entry(n) )*
 */
fun `ns-l-in-line-sequence`(n: Int) = wrapTokens(Code.BeginNode, Code.EndNode, wrapTokens(Code.BeginSequence, Code.EndSequence,
        `c-l-block-seq-entry`(n) and zom(`s-indent`(n) and `c-l-block-seq-entry`(n))))

/**
 * 8.2.2 Block Mappings
 */

/**
 * [187] l+block-mapping(n) ::= ( s-indent(n+m) ns-l-block-map-entry(n+m) )+
 *                              /* For some fixed auto-detected m > 0 */
 */
fun `l+block-mapping`(n: Int) = `detect-collection-indentation`(n).snd("m", wrapTokens(Code.BeginMapping, Code.EndMapping,
        Parser { state ->
            val m = state.yields["m"] as Int
            oom(`s-indent`(n + m) and `ns-l-block-map-entry`(n + m))(state)
        }))

/**
 * [188] ns-l-block-map-entry(n) ::=  c-l-block-map-explicit-entry(n)
 *                                  | ns-l-block-map-implicit-entry(n)
 */
fun `ns-l-block-map-entry`(n: Int) = wrapTokens(Code.BeginPair, Code.EndPair, `c-l-block-map-explicit-entry`(n) or
        `ns-l-block-map-implicit-entry`(n))

/**
 * [189] c-l-block-map-explicit-entry(n) ::= c-l-block-map-explicit-key(n)
 *                                           ( l-block-map-explicit-value(n)
 *                                           | e-node )
 */
fun `c-l-block-map-explicit-entry`(n: Int) = `c-l-block-map-explicit-key`(n) and (`l-block-map-explicit-value`(n) or `e-node`)

/**
 * [190] c-l-block-map-explicit-key(n) ::= “?” s-l+block-indented(n,block-out)
 */
fun `c-l-block-map-explicit-key`(n: Int) = (`c-mapping-key` cmt "node") and `s-l+block-indented`(n, Context.BlockOut)

/**
 * [191] l-block-map-explicit-value(n) ::= s-indent(n)
 *                                         “:” s-l+block-indented(n,block-out)
 */
fun `l-block-map-explicit-value`(n: Int) = `s-indent`(n) and `c-mapping-value` and `s-l+block-indented`(n, Context.BlockOut)

/**
 * [192] ns-l-block-map-implicit-entry(n) ::= ( ns-s-block-map-implicit-key
 *                                            | e-node )
 *                                            c-l-block-map-implicit-value(n)
 */
fun `ns-l-block-map-implicit-entry`(n: Int) = (`ns-s-block-map-implicit-key`() or `e-node` ) and `c-l-block-map-implicit-value`(n)

/**
 * [193] ns-s-block-map-implicit-key ::=  c-s-implicit-json-key(block-key)
 *                                      | ns-s-implicit-yaml-key(block-key)
 */
fun `ns-s-block-map-implicit-key`() = `c-s-implicit-json-key`(Context.BlockKey) or `ns-s-implicit-yaml-key`(Context.BlockKey)

/**
 * [194] c-l-block-map-implicit-value(n) ::= “:” ( s-l+block-node(n,block-out)
 *                                               | ( e-node s-l-comments ) )
 */
fun `c-l-block-map-implicit-value`(n: Int): Parser = (`c-mapping-value` cmt "node") and
        ((`s-l+block-node`(n, Context.BlockOut) or `e-node` and opt(`s-l-comments`) and unparsed(n + 1)) recovery unparsed(n + 1))

/**
 * [195] ns-l-compact-mapping(n) ::= ns-l-block-map-entry(n)
 *                                  ( s-indent(n) ns-l-block-map-entry(n) )*
 */
fun `ns-l-in-line-mapping`(n: Int) = wrapTokens(Code.BeginNode, Code.EndNode, wrapTokens(Code.BeginMapping, Code.EndMapping,
        `ns-l-block-map-entry`(n) and zom(`s-indent`(n) and `ns-l-block-map-entry`(n))))

/**
 * 8.2.3 Block Nodes
 */

fun unparsed(n: Int) = (sol() or unparsed_text() and unparsed_break()) and
        zom(nonEmpty(unparsed_indent(n) and (unparsed_text() and unparsed_break())))

fun unparsed_indent(n: Int) = token(Code.Unparsed, `s-space` tms n)

fun unparsed_text() = token(Code.Unparsed, upto(eof() or `c-forbidden`() or `b-break`))

fun unparsed_break() = eof() or peek(`c-forbidden`()) or token(Code.Unparsed, `b-break`) or empty()

/**
 * [196] s-l+block-node(n,c) ::= s-l+block-in-block(n,c) | s-l+flow-in-block(n)
 */
fun `s-l+block-node`(n: Int, c: Context) = `s-l+block-in-block`(n, c) or `s-l+flow-in-block`(n)

/**
 * [197] s-l+flow-in-block(n) ::= s-separate(n+1,flow-out)
 *                                ns-flow-node(n+1,flow-out) s-l-comments
 */
fun `s-l+flow-in-block`(n: Int) = `s-separate`(n + 1, Context.FlowOut) and `ns-flow-node`(n + 1, Context.FlowOut) and `s-l-comments`

/**
 * [198] s-l+block-in-block(n,c) ::= s-l+block-scalar(n,c) | s-l+block-collection(n,c)
 */
fun `s-l+block-in-block`(n: Int, c: Context) = wrapTokens(Code.BeginNode, Code.EndNode, (`s-l+block-scalar`(n, c) or `s-l+block-collection`(n, c)))

/**
 * [199] s-l+block-scalar(n,c) ::= s-separate(n+1,c)
 *                                 ( c-ns-properties(n+1,c) s-separate(n+1,c) )?
 *                                 ( c-l+literal(n) | c-l+folded(n) )
 */
fun `s-l+block-scalar`(n: Int, c: Context) = `s-separate`(n + 1, c) and
        opt(`c-ns-properties`(n + 1, c) and `s-separate`(n + 1, c)) and (`c-l+literal`(n) or `c-l+folded`(n))

/**
 * [200] s-l+block-collection(n,c) ::= ( s-separate(n+1,c) c-ns-properties(n+1,c) )?
 *                                     s-l-comments
 *                                     ( l+block-sequence(seq-spaces(n,c))
 *                                     | l+block-mapping(n) )
 */
fun `s-l+block-collection`(n: Int, c: Context) = opt(`s-separate`(n + 1, c) and (`c-ns-properties`(n + 1, c) and pla(`s-l-comments`))) and
        (`s-l-comments`) and (`l+block-sequence`(`seq-spaces`(n, c)) or `l+block-mapping`(n))

/**
 * [201] seq-spaces(n,c) ::= c = block-out ⇒ n-1
 *                           c = block-in  ⇒ n
 */
fun `seq-spaces`(n: Int, c: Context) = when (c) {
    Context.BlockOut -> n - 1
    Context.BlockIn -> n
    else -> throw IllegalArgumentException("unexpected")
}

/**
 * 9.1.1 Document Prefix
 */

/**
 * [202] l-document-prefix ::= c-byte-order-mark? l-comment*
 */
val `l-document-prefix` = opt(`c-byte-order-mark`) and zom(nonEmpty(`l-comment`))

/**
 * 9.1.2 Document Markers
 */

/**
 * [203] c-directives-end ::= “-” “-” “-”
 */
val `c-directives-end` = token(Code.DirectivesEnd, '-' and '-' and '-')
/**
 * [204] c-document-end ::= “.” “.” “.”
 */
val `c-document-end` = token(Code.DocumentEnd, '.' and '.' and '.')
/**
 * [205] l-document-suffix ::= c-document-end s-l-comments
 */
val `l-document-suffix` = `c-document-end` and `s-l-comments`

/**
 * [206] c-forbidden ::= /* Start of line */
 *                       ( c-directives-end | c-document-end )
 *                       ( b-char | s-white | /* End of file */ )
 */
fun `c-forbidden`() = sol() and (`c-directives-end` or `c-document-end`) and (`b-char` or `s-white` or eof())

/**
 * 9.1.3 Explicit Documents
 */

/**
 * [207] l-bare-document ::= s-l+block-node(-1,block-in)
 *                           /* Excluding c-forbidden content */
 */
val `l-bare-document` = forbidding("node" cho `s-l+block-node`(-1, Context.BlockIn), `c-forbidden`())

/**
 * 9.1.4 Explicit Documents
 */

/**
 * [208] l-explicit-document ::= c-directives-end
 *                              ( l-bare-document
 *                              | ( e-node s-l-comments ) )
 */
val `l-explicit-document` = (`c-directives-end` cmt "doc") and
        ((`l-bare-document` or `e-node` and opt(`s-l-comments`) and unparsed(0)) recovery unparsed(0))

/**
 * 9.1.5 Directives Documents
 */

/**
 * [209] l-directive-document ::= l-directive+
 *                                l-explicit-document
 */
val `l-directives-document` = oom(`l-directive`) and `l-explicit-document`

/**
 * 9.2 Streams
 */

/**
 * [210] l-any-document ::=  l-directive-document
 *                         | l-explicit-document
 *                         | l-bare-document
 */
val `l-any-document` = wrapTokens(Code.BeginDocument, Code.EndDocument,
        "doc" cho ((`l-directives-document` or `l-explicit-document` or `l-bare-document`) recovery unparsed(0)))

/**
 * [211] l-yaml-stream ::= l-document-prefix* l-any-document?
 *                         ( l-document-suffix+ l-document-prefix* l-any-document?
 *                         | l-document-prefix* l-explicit-document? )*
 */
val `l-yaml-stream` = zom(nonEmpty(`l-document-prefix`)) and
        (eof() or pla(`c-document-end` and (`b-char` or `s-white` or eof())) or `l-any-document`) and
        zom(nonEmpty("more" cho (oom(`l-document-suffix` cmt "more") and zom(nonEmpty(`l-document-prefix`)) and
                (eof() or `l-any-document`) or zom(nonEmpty(`l-document-prefix`)) and
                ("doc" cho opt(wrapTokens(Code.BeginDocument, Code.EndDocument, `l-explicit-document`))))))
