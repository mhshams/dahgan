package io.dahgan.loader

import io.dahgan.loader.EndOfLineVisitor.LineFeedVisitor
import io.dahgan.loader.EndOfLineVisitor.LineFoldVisitor
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

private fun load(tokens: List<Token>): List<Any> {
    val contexts = LoaderStack()

    tokens.forEach {
        visitor(it.code).visit(contexts, it)
    }

    val result = contexts.pop().take()
    if (result is List<*>) {
        return result as List<Any>
    }
    throw IllegalStateException("unexpected result: $result")
}

private fun visitor(code: Code): Visitor = when (code) {
    Code.Text -> TextVisitor
    Code.Meta -> TextVisitor
    Code.LineFeed -> LineFeedVisitor
    Code.LineFold -> LineFoldVisitor
    Code.BeginComment -> BeginIgnoreVisitor
    Code.EndComment -> EndIgnoreVisitor
    Code.BeginAnchor -> Begin(SingleContext())
    Code.EndAnchor -> EndVisitor
    Code.BeginAlias -> Begin(SingleContext())
    Code.EndAlias -> EndAliasVisitor
    Code.BeginScalar -> Begin(ScalarContext())
    Code.EndScalar -> EndVisitor
    Code.BeginSequence -> Begin(ListContext())
    Code.EndSequence -> EndVisitor
    Code.BeginMapping -> Begin(MapContext())
    Code.EndMapping -> EndVisitor
    Code.BeginPair -> Begin(PairContext())
    Code.EndPair -> EndVisitor
    Code.BeginNode -> Begin(NodeContext())
    Code.EndNode -> EndNodeVisitor
    Code.BeginDocument -> Begin(SingleContext())
    Code.EndDocument -> EndVisitor
    Code.Error -> ErrorVisitor
    else -> SkipVisitor
}

private abstract class Context {
    protected val data: MutableList<Any> = ArrayList()

    fun add(any: Any) = data.add(any)

    abstract fun take(): Any
}

private class SingleContext : Context() {
    override fun take(): Any = data.first()
}

private class ScalarContext : Context() {
    override fun take(): Any = data.joinToString("")
}

private class NodeContext : Context() {
    override fun take(): Any = if (data.size > 1) Pair(data.first(), data[1]) else Pair("", data.first())
}

private class ListContext : Context() {
    override fun take(): Any = data
}

private class MapContext : Context() {
    override fun take(): Any = (data as List<Pair<*, *>>).toMap()
}

private class PairContext : Context() {
    override fun take(): Any = Pair(data[0], data[1])
}

private interface Visitor {
    fun visit(stack: LoaderStack, token: Token)
}

private class Begin(val context: Context) : Visitor {
    override fun visit(stack: LoaderStack, token: Token) {
        stack.push(context)
    }
}

private object BeginIgnoreVisitor : Visitor {
    override fun visit(stack: LoaderStack, token: Token) {
        stack.push(object : Context() {
            override fun take(): Any = throw UnsupportedOperationException()
        })
    }
}

private object EndIgnoreVisitor : Visitor {
    override fun visit(stack: LoaderStack, token: Token) {
        stack.pop()
    }
}

private object EndVisitor : Visitor {
    override fun visit(stack: LoaderStack, token: Token) {
        val top = stack.pop()
        stack.peek().add(top.take())
    }
}

private object EndNodeVisitor : Visitor {
    override fun visit(stack: LoaderStack, token: Token) {
        val top = stack.pop().take() as Pair<Any, Any>
        if (top.first.toString().isNotEmpty()) {
            stack.anchor(top.first.toString(), top.second)
        }
        stack.peek().add(top.second)
    }
}

private object EndAliasVisitor : Visitor {
    override fun visit(stack: LoaderStack, token: Token) {
        val top = stack.pop()
        stack.peek().add(stack.anchor(top.take().toString()))
    }
}

private object TextVisitor : Visitor {
    override fun visit(stack: LoaderStack, token: Token) {
        stack.peek().add(token.text.toString())
    }
}

private abstract class EndOfLineVisitor(val join: String) : Visitor {
    override fun visit(stack: LoaderStack, token: Token) {
        stack.peek().add(join)
    }

    object LineFoldVisitor : EndOfLineVisitor(" ")

    object LineFeedVisitor : EndOfLineVisitor("\n")
}

private object ErrorVisitor : Visitor {
    override fun visit(stack: LoaderStack, token: Token) {
        throw IllegalStateException("${token.text} - Line #${token.line} , Character #${token.lineChar + 1}")
    }
}

private object SkipVisitor : Visitor {
    override fun visit(stack: LoaderStack, token: Token) = Unit
}


private class LoaderStack {

    private val contexts = mutableListOf<Context>(ListContext())

    private val anchors = HashMap<String, Any>()

    fun push(context: Context) = contexts.add(context)

    fun peek() = contexts.last()

    fun pop() = contexts.removeAt(contexts.lastIndex)

    fun anchor(key: String, value: Any) = anchors.set(key, value)

    fun anchor(key: String) = anchors[key]!!
}
