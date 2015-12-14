package io.dahgan.parser

/**
 * Production context.
 */
enum class Context(val text: String) {
    /**
     * Outside block sequence.
     */
    BlockOut("block-out"),

    /**
     * Inside block sequence.
     */
    BlockIn("block-in"),

    /**
     * Outside flow collection.
     */
    FlowOut("flow-out"),

    /**
     * Inside flow collection.
     */
    FlowIn("flow-in"),

    /**
     * Implicit block key.
     */
    BlockKey("block-key"),

    /**
     * Implicit flow key.
     */
    FlowKey("flow-key");


    override fun toString(): String = text

    companion object {
        fun from(word: String): Context = when (word) {
            "block_out" -> BlockOut
            "block_in" -> BlockIn
            "flow_out" -> FlowOut
            "flow_in" -> FlowIn
            "block_key" -> BlockKey
            "flow_key" -> FlowKey
            else -> throw IllegalArgumentException("unknown context: $word")
        }
    }
}
