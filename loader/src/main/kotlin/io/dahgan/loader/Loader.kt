package io.dahgan.loader

import io.dahgan.parser.Code
import io.dahgan.parser.Token
import io.dahgan.yaml
import java.io.File
import java.util.*

/**
 * Loads the first yaml document in the given text and returns the loaded object.
 * Depending on the content, the result can be a simple text, a map or a list.
 */
fun load(text: String): Any = load(text.toByteArray(Charsets.UTF_8))

/**
 * Loads the first yaml document in the given file and returns the loaded object.
 * Depending on the content, the result can be a simple text, a map or a list.
 */
fun load(file: File): Any = load(file.readBytes())

/**
 * Loads the first yaml document in the given byte array and returns the loaded object.
 * Depending on the content, the result can be a simple text, a map or a list.
 */
fun load(bytes: ByteArray): Any = load(yaml().tokenize("load", bytes, false))[0]

/**
 * Loads all yaml documents in the given text and returns the loaded objects.
 * The result is a list of loaded objects.
 */
fun loadAll(text: String): List<Any> = loadAll(text.toByteArray(Charsets.UTF_8))

/**
 * Loads all yaml documents in the given file and returns the loaded objects.
 * The result is a list of loaded objects.
 */
fun loadAll(file: File): List<Any> = loadAll(file.readBytes())

/**
 * Loads all yaml documents in the given byte array and returns the loaded objects.
 * The result is a list of loaded objects.
 */
fun loadAll(bytes: ByteArray): List<Any> = load(yaml().tokenize("load-all", bytes, false))

private fun load(tokens: Sequence<Token>): List<Any> {
    val anchors = HashMap<String, Any>()

    val context = Stack<Context>()
    context.push(ListContext())

    tokens.forEach {
        visitor(it.code).visit(anchors, context, it)
    }

    val result = context.pop().peek()
    if (result is List<*>) {
        return result as List<Any>
    }
    throw IllegalStateException("unexpected result: $result")
}

private fun visitor(code: Code): Visitor = when (code) {
    Code.Text -> TextVisitor()
    Code.Meta -> TextVisitor()
    Code.LineFeed -> EndOfLineVisitor("\n")
    Code.LineFold -> EndOfLineVisitor(" ")
    Code.BeginComment -> BeginIgnoreVisitor()
    Code.EndComment -> EndIgnoreVisitor()
    Code.BeginAnchor -> BeginVisitor(SingleContext())
    Code.EndAnchor -> EndVisitor()
    Code.BeginAlias -> BeginVisitor(SingleContext())
    Code.EndAlias -> EndAliasVisitor()
    Code.BeginScalar -> BeginVisitor(ScalarContext())
    Code.EndScalar -> EndVisitor()
    Code.BeginSequence -> BeginVisitor(ListContext())
    Code.EndSequence -> EndVisitor()
    Code.BeginMapping -> BeginVisitor(MapContext())
    Code.EndMapping -> EndVisitor()
    Code.BeginPair -> BeginVisitor(PairContext())
    Code.EndPair -> EndVisitor()
    Code.BeginNode -> BeginVisitor(NodeContext())
    Code.EndNode -> EndNodeVisitor()
    Code.BeginDocument -> BeginVisitor(SingleContext())
    Code.EndDocument -> EndVisitor()
    Code.Error -> ErrorVisitor()
    else -> SKIP
}

abstract private class Context {
    protected val data: MutableList<Any> = ArrayList()

    fun add(any: Any) = data.add(any)

    abstract fun peek(): Any
}

private class SingleContext : Context() {
    override fun peek(): Any = data.first()
}

private class ScalarContext : Context() {
    override fun peek(): Any = data.joinToString("")
}

private class NodeContext : Context() {
    override fun peek(): Any = if (data.size > 1) Pair(data.first(), data[1]) else Pair("", data.first())
}

private class ListContext : Context() {
    override fun peek(): Any = data
}

private class MapContext : Context() {
    override fun peek(): Any = data.filterIsInstance<Pair<*, *>>().toMap()
}

private class PairContext : Context() {
    override fun peek(): Any = Pair(data[0], data[1])
}

private interface Visitor {
    fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token)
}

private class BeginVisitor(val context: Context) : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        contexts.push(context)
    }
}

private class BeginIgnoreVisitor : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        contexts.push(object : Context() {
            override fun peek(): Any = throw UnsupportedOperationException()
        })
    }
}

private class EndIgnoreVisitor : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        contexts.pop()
    }
}

private class EndVisitor : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        val top = contexts.pop()
        contexts.peek().add(top.peek())
    }
}

private class EndNodeVisitor : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        val top = contexts.pop().peek() as Pair<Any, Any>
        if (top.first.toString().isNotEmpty()) {
            anchors.put(top.first.toString(), top.second)
        }
        contexts.peek().add(top.second)
    }
}

private class EndAliasVisitor : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        val top = contexts.pop()
        contexts.peek().add(anchors[top.peek().toString()]!!)
    }
}

private class TextVisitor : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        contexts.peek().add(token.text.toString())
    }
}

private class EndOfLineVisitor(val join: String) : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        contexts.peek().add(join)
    }
}

private class ErrorVisitor : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        throw IllegalStateException("${token.text} - Line #${token.line} , Character #${token.lineChar + 1}")
    }
}

private object SKIP : Visitor {
    override fun visit(anchors: MutableMap<String, Any>, contexts: Stack<Context>, token: Token) {
        //do nothing
    }
}
