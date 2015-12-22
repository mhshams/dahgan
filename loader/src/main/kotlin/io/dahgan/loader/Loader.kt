package io.dahgan.loader

import io.dahgan.parser.Code
import io.dahgan.parser.Token
import io.dahgan.yaml
import java.io.File
import java.util.*

/**
 */
public fun load(text: String): Any = load(text.toByteArray(Charsets.UTF_8))

public fun load(file: File): Any = load(file.readBytes())

public fun load(bytes: ByteArray): Any = DOCUMENT.load(yaml().tokenize("load", bytes, false)).loaded

public fun loadAll(text: String): List<Any> = loadAll(text.toByteArray(Charsets.UTF_8))

public fun loadAll(file: File): List<Any> = loadAll(file.readBytes())

public fun loadAll(bytes: ByteArray): List<Any> {
    var tokens = yaml().tokenize("load-all", bytes, false)

    val documents = ArrayList<Any>()
    while (!tokens.none()) {
        val state = DOCUMENT.load(tokens)
        documents.add(state.loaded)
        tokens = state.remaining.dropWhile { it.code != Code.EndDocument }.drop(1)
        if (!tokens.none() && tokens.first().code == Code.DocumentEnd) {
            tokens = tokens.drop(1)
        }
    }

    return documents
}

data class LoadingState<T>(val loaded: T, val remaining: Sequence<Token>)

interface Loader {
    fun load(tokens: Sequence<Token>): LoadingState<out Any>
}

private object DOCUMENT : Loader {
    override fun load(tokens: Sequence<Token>): LoadingState<out Any> {
        tokens.forEach {
            println(it)
        }
        return NODE.load(tokens.dropWhile { it.code != Code.BeginDocument }.drop(1))
    }
}

private object NODE : Loader {
    override fun load(tokens: Sequence<Token>): LoadingState<out Any> {
        val remaining = tokens.dropWhile { it.code != Code.BeginNode }.drop(1).dropWhile { it.code !in arrayOf(Code.BeginScalar, Code.BeginMapping, Code.BeginSequence) }

        return when (remaining.first().code) {
            Code.BeginScalar -> SCALAR.load(remaining.drop(1))
            Code.BeginMapping -> MAPPING.load(remaining.drop(1))
            Code.BeginSequence -> SEQUENCE.load(remaining.drop(1))
            else -> throw IllegalStateException("not expected")
        }
    }
}

private object SCALAR : Loader {
    override fun load(tokens: Sequence<Token>): LoadingState<String> {
        val remaining = tokens.dropWhile { it.code != Code.Text }
        return LoadingState(remaining.first().text.toString(), remaining.drop(1))
    }

}

private object MAPPING : Loader {
    override fun load(tokens: Sequence<Token>): LoadingState<HashMap<Any, Any>> {
        val loaded = HashMap<Any, Any>()
        var remaining = tokens.dropWhile { it.code != Code.BeginPair }

        while (!remaining.none() && remaining.first().code != Code.EndMapping) {
            var state = PAIR.load(remaining.drop(1))
            loaded.put(state.loaded.first, state.loaded.second)
            remaining = state.remaining.dropWhile { it.code != Code.BeginPair && it.code != Code.EndMapping }
        }

        return LoadingState(loaded, remaining)
    }
}

private object PAIR : Loader {
    override fun load(tokens: Sequence<Token>): LoadingState<Pair<Any, Any>> {
        val keyState = NODE.load(tokens)
        val valueState = NODE.load(keyState.remaining)
        return LoadingState(Pair(keyState.loaded, valueState.loaded), valueState.remaining)
    }
}

private object SEQUENCE : Loader {
    override fun load(tokens: Sequence<Token>): LoadingState<List<Any>> {
        val loaded = ArrayList<Any>()
        var remaining = tokens.dropWhile { it.code != Code.BeginNode }

        while (!remaining.none() && remaining.first().code != Code.EndSequence) {
            var nodeState = NODE.load(remaining)
            loaded.add(nodeState.loaded)
            remaining = nodeState.remaining.dropWhile { it.code != Code.BeginNode && it.code != Code.EndSequence }
        }

        return LoadingState(loaded, remaining)
    }
}

