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
public fun load(bytes: ByteArray): Any = DOCUMENT.load(LoadingContext(), yaml().tokenize("load", bytes, false)).loaded

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
        val state = DOCUMENT.load(LoadingContext(), tokens)
        documents.add(state.loaded)
        tokens = state.remaining.dropWhile { it.code != Code.EndDocument }.drop(1)
        if (!tokens.none() && tokens.first().code == Code.DocumentEnd) {
            tokens = tokens.drop(1)
        }
    }

    return documents
}

data class LoadingState<T>(val loaded: T, val remaining: Sequence<Token>)

data class LoadingContext(private val ctx: MutableMap<String, Any> = HashMap()) {
    fun put(key: String, value: Any) = ctx.put(key, value)

    fun get(key: String): Any? = ctx[key]
}

interface Loader {
    fun load(context: LoadingContext, tokens: Sequence<Token>): LoadingState<out Any>
}

private object DOCUMENT : Loader {
    override fun load(context: LoadingContext, tokens: Sequence<Token>): LoadingState<out Any> {
        return NODE.load(context, tokens.dropWhile { it.code != Code.BeginDocument }.drop(1))
    }
}

private object NODE : Loader {
    override fun load(context: LoadingContext, tokens: Sequence<Token>): LoadingState<out Any> {
        var remaining = tokens.dropWhile {
            it.code != Code.BeginNode
        }.drop(1).dropWhile {
            it.code !in arrayOf(Code.BeginProperties, Code.BeginAlias, Code.BeginScalar, Code.BeginMapping, Code.BeginSequence)
        }

        val anchor = if (remaining.first().code == Code.BeginProperties) {
            val theAnchor = ANCHOR.load(context, remaining)
            remaining = theAnchor.remaining
            theAnchor
        } else {
            null
        }

        val loaded = when (remaining.first().code) {
            Code.BeginAlias -> ALIAS.load(context, remaining.drop(1))
            Code.BeginScalar -> SCALAR.load(context, remaining.drop(1))
            Code.BeginMapping -> MAPPING.load(context, remaining.drop(1))
            Code.BeginSequence -> SEQUENCE.load(context, remaining.drop(1))
            else -> throw IllegalStateException("not expected")
        }

        if (anchor != null) {
            context.put(anchor.loaded.toString(), loaded.loaded)
        }

        return loaded
    }
}

private object ANCHOR : Loader {
    override fun load(context: LoadingContext, tokens: Sequence<Token>): LoadingState<out Any> {
        val alias = tokens.dropWhile {
            it.code != Code.BeginAnchor
        }.takeWhile {
            it.code != Code.EndAnchor
        }.filter {
            it.code == Code.Meta
        }.map {
            it.text.toString()
        }.first()

        return LoadingState(alias, tokens.dropWhile { it.code !in arrayOf(Code.BeginScalar, Code.BeginMapping, Code.BeginSequence) })
    }
}

private object ALIAS : Loader {
    override fun load(context: LoadingContext, tokens: Sequence<Token>): LoadingState<out Any> {
        val alias = tokens.takeWhile {
            it.code != Code.EndAlias
        }.filter {
            it.code == Code.Meta
        }.map {
            it.text.toString()
        }.first()

        return LoadingState(context.get(alias)!!, tokens.dropWhile { it.code != Code.EndAlias })
    }
}

private object SCALAR : Loader {
    override fun load(context: LoadingContext, tokens: Sequence<Token>): LoadingState<String> {
        val text = tokens.dropWhile {
            it.code != Code.Text
        }.takeWhile {
            it.code != Code.EndScalar
        }.filter {
            it.code == Code.Text
        }.map {
            it.text.toString()
        }.joinToString(" ")

        return LoadingState(text, tokens.dropWhile { it.code != Code.EndScalar })
    }

}

private object MAPPING : Loader {
    override fun load(context: LoadingContext, tokens: Sequence<Token>): LoadingState<HashMap<Any, Any>> {
        val loaded = HashMap<Any, Any>()
        var remaining = tokens.dropWhile { it.code != Code.BeginPair }

        while (!remaining.none() && remaining.first().code != Code.EndMapping) {
            var state = PAIR.load(context, remaining.drop(1))
            loaded.put(state.loaded.first, state.loaded.second)
            remaining = state.remaining.dropWhile { it.code != Code.BeginPair && it.code != Code.EndMapping }
        }

        return LoadingState(loaded, if (!remaining.none() && remaining.first().code == Code.EndMapping) remaining.drop(1) else remaining)
    }
}

private object PAIR : Loader {
    override fun load(context: LoadingContext, tokens: Sequence<Token>): LoadingState<Pair<Any, Any>> {
        val keyState = NODE.load(context, tokens)
        val valueState = NODE.load(context, keyState.remaining)
        return LoadingState(Pair(keyState.loaded, valueState.loaded), valueState.remaining)
    }
}

private object SEQUENCE : Loader {
    override fun load(context: LoadingContext, tokens: Sequence<Token>): LoadingState<List<Any>> {
        val loaded = ArrayList<Any>()
        var remaining = tokens.dropWhile { it.code != Code.BeginNode }

        while (!remaining.none() && remaining.first().code != Code.EndSequence) {
            var nodeState = NODE.load(context, remaining)
            loaded.add(nodeState.loaded)
            remaining = nodeState.remaining.dropWhile { it.code != Code.BeginNode && it.code != Code.EndSequence }
        }

        return LoadingState(loaded, remaining)
    }
}

