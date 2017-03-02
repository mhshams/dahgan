package io.dahgan

import io.dahgan.parser.Chomp
import io.dahgan.parser.Context
import io.dahgan.parser.showTokens
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.util.regex.Pattern

@RunWith(Parameterized::class)
abstract class AbstractSpecTest {
    companion object {
        val BASE_PATH = File(SpecTest::class.java.getResource("/io/dahgan/").file)

        fun products(dir: String, filter: (String) -> Boolean): Collection<Array<String>> =
                File(BASE_PATH, dir).listFiles { _, file ->
                    file.endsWith(".input") && filter(file)
                }.map { file ->
                    arrayOf(file.name, file.absolutePath)
                }.toList()
    }

    @Parameterized.Parameter(0)
    lateinit var name: String

    @Parameterized.Parameter(1)
    lateinit var path: String

    protected fun run() {
        val file = File(path)
        val input = file.readBytes()
        val expected = File(file.parentFile, file.name.replace(".input", ".output")).readText()

        val tokenizer = when(TestType.type(name)) {
            TestType.Plain  -> tokenizer(production(file.name))
            TestType.WithN  -> tokenizerWithN(production(file.name),  paramN(name))
            TestType.WithC  -> tokenizerWithC(production(file.name),  paramC(name))
            TestType.WithT  -> tokenizerWithT(production(file.name),  paramT(name))
            TestType.WithNC -> tokenizerWithNC(production(file.name), paramN(name), paramC(name))
            TestType.WithNT -> tokenizerWithNT(production(file.name), paramN(name), paramT(name))
            else            -> throw IllegalArgumentException("unexpected!!!")
        }
        val output = showTokens(tokenizer.tokenize(name, input, false))

        assertEquals(message(input, expected, output), expected, output)
    }

    protected fun message(input: ByteArray, expected: String, output: String): String =
            "\n>>>" +
                    "\n> INP $name ----------\n${String(input) }" +
                    "\n> OUT ---------\n$expected" +
                    "\n> BUT ---------\n$output" +
                    "<<<\n"

    protected fun production(file: String): String = file.substring(0, file.indexOf('.'))

    protected fun paramN(test: String) = parameter("n", test).toInt()

    protected fun paramC(test: String) = Context.from(parameter("c", test).replace("-", "_"))

    protected fun paramT(test: String) = Chomp.from(parameter("t", test).replace("-", "_"))

    protected fun parameter(p: String, test: String): String  {
        val matcher = Pattern.compile("(.+).$p=([^\\.]+)(.+)").matcher(test)
        if (matcher.matches()) {
            return matcher.group(2)
        }
        throw IllegalArgumentException("unexpected")
    }

    /**
     * Different types of test files.
     */
    enum class TestType(val text: String) {
        Plain(""),           //
        WithN(" n"),         // Production requiring $n$ argument.
        WithC(" c"),         // Production requiring $c$ argument.
        WithT(" t"),         // Production requiring $t$ argument.
        WithNC(" n c"),      // Production requiring $n$ and $c$ arguments.
        WithNT(" n t");      // Production requiring $n$ and $t$ arguments.

        override fun toString(): String = text

        companion object {
            fun type(test: String): TestType {
                return when(Triple(isWith("n", test), isWith("c", test), isWith("t", test))) {
                    Triple(false, false, false) -> Plain
                    Triple(true, false, false)  -> WithN
                    Triple(false, true, false)  -> WithC
                    Triple(false, false, true)  -> WithT
                    Triple(true, true, false)   -> WithNC
                    Triple(true, false, true)   -> WithNT
                    else                        -> throw IllegalArgumentException("unknown test type: $test")
                }
            }

            fun isWith(parameter: String, test: String): Boolean = test.contains(".$parameter=")
        }
    }
}

