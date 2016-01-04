package io.dahgan.stream

/**
 * Recognized Unicode encodings. As of YAML 1.2 UTF-32 is also required.
 */
enum class Encoding(private val text: String) {
    UTF8("UTF-8"), // UTF-8 encoding (or ASCII)
    UTF16LE("UTF-16LE"), // UTF-16 little endian
    UTF16BE("UTF-16BE"), // UTF-16 big endian
    UTF32LE("UTF-32LE"), // UTF-32 little endian
    UTF32BE("UTF-32BE"); // UTF-32 big endian

    override fun toString() = text
}
