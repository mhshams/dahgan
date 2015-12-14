package io.dahgan.parser

/**
 * Chomp method.
 */
enum class Chomp(val text: String) {
    /**
     * Remove all trailing line breaks.
     */
    Strip("strip"),

    /**
     * Keep first trailing line break.
     */
    Clip("clip"),

    /**
     * Keep all trailing line breaks.
     */
    Keep("keep");

    override fun toString(): String = text

    companion object {
        fun from(word: String): Chomp = when (word) {
            "strip" -> Strip
            "clip" -> Clip
            "keep" -> Keep
            else -> throw IllegalArgumentException("unknown chomp: $word")
        }
    }
}
