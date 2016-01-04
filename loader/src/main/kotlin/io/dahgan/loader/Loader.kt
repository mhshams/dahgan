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
public fun load(text: String): Any = load(text.toByteArray(Charsets.UTF_8))

/**
 * Loads the first yaml document in the given file and returns the loaded object.
 * Depending on the content, the result can be a simple text, a map or a list.
 */
public fun load(file: File): Any = load(file.readBytes())

/**
 * Loads the first yaml document in the given byte array and returns the loaded object.
 * Depending on the content, the result can be a simple text, a map or a list.
 */
public fun load(bytes: ByteArray): Any = load(yaml().tokenize("load", bytes, false)).instance

/**
 * Loads all yaml documents in the given text and returns the loaded objects.
 * The result is a list of loaded objects.
 */
public fun loadAll(text: String): List<Any> = loadAll(text.toByteArray(Charsets.UTF_8))

/**
 * Loads all yaml documents in the given file and returns the loaded objects.
 * The result is a list of loaded objects.
 */
public fun loadAll(file: File): List<Any> = loadAll(file.readBytes())

/**
 * Loads all yaml documents in the given byte array and returns the loaded objects.
 * The result is a list of loaded objects.
 */
public fun loadAll(bytes: ByteArray): List<Any> {
    var tokens = yaml().tokenize("load-all", bytes, false)

    val documents = ArrayList<Any>()
    while (!tokens.none()) {
        val state = load(tokens)
        documents.add(state.instance)
        tokens = state.tokens.dropWhile { it.code != Code.EndDocument }.drop(1)
        if (!tokens.none() && tokens.first().code == Code.DocumentEnd) {
            tokens = tokens.drop(1)
        }
    }

    return documents
}

private fun load(tokens: Sequence<Token>): State<Any> = DOCUMENT.load(State(Any(), tokens, HashMap()))

class State<T>(val instance: T, val tokens: Sequence<Token>, val context: MutableMap<String, Any>) {
    fun copy(remaining: Sequence<Token>): State<T> = State(instance, remaining, context)

    fun <R> copy(loaded: R): State<R> = State(loaded, tokens, context)

    fun <R> copy(loaded: R, remaining: Sequence<Token>): State<R> = State(loaded, remaining, context)
}

interface Loader {
    fun load(state: State<Any>): State<out Any>
}

private object DOCUMENT : Loader {
    override fun load(state: State<Any>): State<Any> = NODE.load(state.copy(state.tokens.dropWhile { it.code != Code.BeginDocument }.drop(1)))
}

private object NODE : Loader {
    override fun load(state: State<Any>): State<Any> {
        var remaining = state.tokens.dropWhile {
            it.code != Code.BeginNode
        }.drop(1).dropWhile {
            it.code !in arrayOf(Code.BeginProperties, Code.BeginAlias, Code.BeginScalar, Code.BeginMapping, Code.BeginSequence)
        }

        val anchor = if (remaining.first().code == Code.BeginProperties) {
            val theAnchor = ANCHOR.load(state.copy(remaining))
            remaining = theAnchor.tokens
            theAnchor
        } else {
            null
        }

        val loaded = when (remaining.first().code) {
            Code.BeginAlias -> ALIAS.load(state.copy(remaining.drop(1)))
            Code.BeginScalar -> SCALAR.load(state.copy(remaining.drop(1)))
            Code.BeginMapping -> MAPPING.load(state.copy(remaining.drop(1)))
            Code.BeginSequence -> SEQUENCE.load(state.copy(remaining.drop(1)))
            else -> throw IllegalStateException("not expected")
        }

        if (anchor != null) {
            loaded.context.put(anchor.instance.toString(), loaded.instance)
        }

        return loaded
    }
}

private object ANCHOR : Loader {
    override fun load(state: State<Any>): State<Any> {
        val alias = state.tokens.dropWhile {
            it.code != Code.BeginAnchor
        }.takeWhile {
            it.code != Code.EndAnchor
        }.filter {
            it.code == Code.Meta
        }.map {
            it.text.toString()
        }.first()

        return state.copy(alias, state.tokens.dropWhile { it.code !in arrayOf(Code.BeginScalar, Code.BeginMapping, Code.BeginSequence) })
    }
}

private object ALIAS : Loader {
    override fun load(state: State<Any>): State<Any> {
        val alias = state.tokens.takeWhile {
            it.code != Code.EndAlias
        }.filter {
            it.code == Code.Meta
        }.map {
            it.text.toString()
        }.first()

        return state.copy(state.context[alias]!!, state.tokens.dropWhile { it.code != Code.EndAlias })
    }
}

private object SCALAR : Loader {
    override fun load(state: State<Any>): State<Any> {
        val text = state.tokens.dropWhile {
            it.code != Code.Text
        }.takeWhile {
            it.code != Code.EndScalar
        }.filter {
            it.code == Code.Text
        }.map {
            it.text.toString()
        }.joinToString(" ")

        return state.copy(text, state.tokens.dropWhile { it.code != Code.EndScalar })
    }

}

private object MAPPING : Loader {
    override fun load(state: State<Any>): State<Any> {
        val loaded = HashMap<Any, Any>()
        var remaining = state.tokens.dropWhile { it.code != Code.BeginPair }

        while (!remaining.none() && remaining.first().code != Code.EndMapping) {
            var pairState = PAIR.load(state.copy(remaining.drop(1)))
            loaded.put(pairState.instance.first, pairState.instance.second)

            remaining = pairState.tokens.dropWhile { it.code != Code.BeginPair && it.code != Code.EndMapping }
        }

        return state.copy(loaded, if (!remaining.none() && remaining.first().code == Code.EndMapping) remaining.drop(1) else remaining)
    }
}

private object PAIR : Loader {
    override fun load(state: State<Any>): State<Pair<Any, Any>> {
        val keyState = NODE.load(state)
        val valueState = NODE.load(keyState)
        return valueState.copy(Pair(keyState.instance, valueState.instance))
    }
}

private object SEQUENCE : Loader {
    override fun load(state: State<Any>): State<Any> {
        val loaded = ArrayList<Any>()
        var remaining = state.tokens.dropWhile { it.code != Code.BeginNode }

        while (!remaining.none() && remaining.first().code != Code.EndSequence) {
            var nodeState = NODE.load(state.copy(remaining))
            loaded.add(nodeState.instance)
            remaining = nodeState.tokens.dropWhile { it.code != Code.BeginNode && it.code != Code.EndSequence }
        }

        return state.copy(loaded, remaining)
    }
}

