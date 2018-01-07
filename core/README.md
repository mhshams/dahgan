## Core Module
The Core module consists of YAML 1.2 specification, the parser and the YEAST tokenizer. 
It is, in fact, a [Kotlin](https://kotlinlang.org/) implementation of a Haskell based YAML parser called [YamlReference](https://hackage.haskell.org/package/YamlReference). 
More detailed information about the original Haskell package can be found [here](https://hackage.haskell.org/package/YamlReference) and [here](http://www.ben-kiki.org/oren/YamlReference/).

#### Specification
The specification is directly copied from [YAML 1.2 specification](http://yaml.org/spec/1.2/spec.html) with minor changes to follow Kotlin language syntax.
Following is a snippet of the specification (Spec.kt):
```kotlin
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
```

#### Tokenizer
The parsing result is a sequence of YEAST tokens. These tokens can later be used by other modules, or third-party tools,
to generate useful data. For example Loader module takes these tokens to convert them to Kotlin objects (Map, List or String).

Following code is a sample way to explore the tokenizer:
```kotlin
// the yaml text
val text = "foo: bar"

// tokenize the text and print the generated tokens
yaml().tokenize("a name", text.toByteArray(), true).forEach { token -> println(token) }
```

The output of above execution would be following tokens: 
(Each token has the information about the byte offset in the file, character offset in the file, line number and character offset in the line.)
```
# B: 0, C: 0, L: 1, c: 0
O

# B: 0, C: 0, L: 1, c: 0
N

# B: 0, C: 0, L: 1, c: 0
M

# B: 0, C: 0, L: 1, c: 0
X

# B: 0, C: 0, L: 1, c: 0
N

# B: 0, C: 0, L: 1, c: 0
S

# B: 0, C: 0, L: 1, c: 0
Tfoo

# B: 3, C: 3, L: 1, c: 3
s

# B: 3, C: 3, L: 1, c: 3
n

# B: 3, C: 3, L: 1, c: 3
I:

# B: 4, C: 4, L: 1, c: 4
w 

# B: 5, C: 5, L: 1, c: 5
N

# B: 5, C: 5, L: 1, c: 5
S

# B: 5, C: 5, L: 1, c: 5
Tbar

# B: 8, C: 8, L: 1, c: 8
s

# B: 8, C: 8, L: 1, c: 8
n

# B: 8, C: 8, L: 1, c: 8
x

# B: 8, C: 8, L: 1, c: 8
m

# B: 8, C: 8, L: 1, c: 8
n

# B: 8, C: 8, L: 1, c: 8
o
```

Individual productions in the specification can also be used to generate tokens.
```
// the yaml comment text
val text = "# a comment"

// tokenize the comment text and print the generated tokens
PatternTokenizer(`s-b-comment`).tokenize("a comment", text.toByteArray(), true).forEach { token -> println(token) }
```
 
The output of above execution would be following tokens:
```
# B: 0, C: 0, L: 1, c: 0
C

# B: 0, C: 0, L: 1, c: 0
I#

# B: 1, C: 1, L: 1, c: 1
t a comment

# B: 11, C: 11, L: 1, c: 11
c
```
To see more examples of tokenizer, check out the available tests in the source code. 

#### YEAST (YAML Elaborate Atomic Syntax Tokens) Table
Name            | Code  | Description
----------------|-------|---------------------------------------------
Bom             | U     | BOM, contains TF8, TF16LE, TF32BE, etc.
Text            | T     | Content text characters.
Meta            | t     | Non-content (meta) text characters.
Break           | b     | Separation line break.
LineFeed        | L     | Line break normalized to content line feed.
LineFold        | l     | Line break folded to content space.
Indicator       | I     | Character indicating structure.
White           | w     | Separation white space.
Indent          | i     | Indentation spaces.
DirectivesEnd   | K     | Document start marker.
DocumentEnd     | k     | Document end marker.
BeginEscape     | E     | Begins escape sequence.
EndEscape       | e     | Ends escape sequence.
BeginComment    | C     | Begins comment.
EndComment      | c     | Ends comment.
BeginDirective  | D     | Begins directive.
EndDirective    | d     | Ends directive
BeginTag        | G     | Begins tag
EndTag          | g     | Ends tag
BeginHandle     | H     | Begins tag handle
EndHandle       | h     | Ends tag handle
BeginAnchor     | A     | Begins anchor
EndAnchor       | a     | Ends anchor
BeginProperties | P     | Begins node properties
EndProperties   | p     | Ends node properties
BeginAlias      | R     | Begins alias
EndAlias        | r     | Ends alias
BeginScalar     | S     | Begins scalar content
EndScalar       | s     | Ends scalar content
BeginSequence   | Q     | Begins sequence content
EndSequence     | q     | Ends sequence content
BeginMapping    | M     | Begins mapping content
EndMapping      | m     | Ends mapping content
BeginPair       | X     | Begins mapping key:value pair
EndPair         | x     | Ends mapping key:value pair
BeginNode       | N     | Begins complete node
EndNode         | n     | Ends complete node
BeginDocument   | O     | Begins document
EndDocument     | o     | Ends document
BeginStream     |       | Begins YAML stream
EndStream       |       | Ends YAML stream
Error           | !     | Parsing error at this point
Unparsed        | -     | Unparsed due to errors (or at end of test)
Detected        | $     | Detected parameter (for testing)
