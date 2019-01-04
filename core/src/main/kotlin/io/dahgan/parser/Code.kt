package io.dahgan.parser

/**
 * Code represents the one-character YEAST token code char.
 */
enum class Code(val code: String) {

    /**
     *  BOM, contains TF8, TF16LE, TF32BE, etc.
     */
    Bom("U"),

    /**
     * Content text characters.
     */
    Text("T"),

    /**
     * Non-content (meta) text characters.
     */
    Meta("t"),

    /**
     * Separation line break.
     */
    Break("b"),

    /**
     * Line break normalized to content line feed.
     */
    LineFeed("L"),

    /**
     * Line break folded to content space.
     */
    LineFold("l"),

    /**
     * Character indicating structure.
     */
    Indicator("I"),

    /**
     * Separation white space.
     */
    White("w"),

    /**
     * Indentation spaces.
     */
    Indent("i"),

    /**
     * Directives end marker.
     */
    DirectivesEnd("K"),

    /**
     * Document end marker.
     */
    DocumentEnd("k"),

    /**
     * Begins escape sequence.
     */
    BeginEscape("E"),

    /**
     * Ends escape sequence.
     */
    EndEscape("e"),

    /**
     * Begins comment.
     */
    BeginComment("C"),

    /**
     * Ends comment.
     */
    EndComment("c"),

    /**
     * Begins directive.
     */
    BeginDirective("D"),

    /**
     * Ends directive
     */
    EndDirective("d"),

    /**
     * Begins tag
     */
    BeginTag("G"),

    /**
     * Ends tag
     */
    EndTag("g"),

    /**
     * Begins tag handle
     */
    BeginHandle("H"),

    /**
     * Ends tag handle
     */
    EndHandle("h"),

    /**
     * Begins anchor
     */
    BeginAnchor("A"),

    /**
     * Ends anchor
     */
    EndAnchor("a"),

    /**
     * Begins node properties
     */
    BeginProperties("P"),

    /**
     * Ends node properties
     */
    EndProperties("p"),

    /**
     * Begins alias
     */
    BeginAlias("R"),

    /**
     * Ends alias
     */
    EndAlias("r"),

    /**
     * Begins scalar content
     */
    BeginScalar("S"),

    /**
     * Ends scalar content
     */
    EndScalar("s"),

    /**
     * Begins sequence content
     */
    BeginSequence("Q"),

    /**
     * Ends sequence content
     */
    EndSequence("q"),

    /**
     * Begins mapping content
     */
    BeginMapping("M"),

    /**
     * Ends mapping content
     */
    EndMapping("m"),

    /**
     * Begins mapping key:value pair
     */
    BeginPair("X"),

    /**
     * Ends mapping key:value pair
     */
    EndPair("x"),

    /**
     * Begins complete node
     */
    BeginNode("N"),

    /**
     * Ends complete node
     */
    EndNode("n"),

    /**
     * Begins document
     */
    BeginDocument("O"),

    /**
     * Ends document
     */
    EndDocument("o"),

    /**
     * Begins YAML stream
     */
    BeginStream(""),

    /**
     * Ends YAML stream
     */
    EndStream(""),

    /**
     * Parsing error at this point
     */
    Error("!"),

    /**
     * Unparsed due to errors (or at end of test)
     */
    Unparsed("-"),

    /**
     * Detected parameter (for testing)
     */
    Detected("$");

    override fun toString() = code
}

