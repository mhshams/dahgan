package io.dahgan.loader

import io.dahgan.yaml
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Performance Issue in Loader
 */
class PerformanceTest {

    @Test
    fun sample() {
        val doc = (
                """
                ---
                - items:
                """ + """
                  - name: A
                    option: A
                """.repeat(64)
                ).trimIndent()


        val tokenizeOnly = measureTimeMillis {
            yaml().tokenize("load", doc.toByteArray(Charsets.UTF_8), false)
        }

        val tokenizeAndLoad = measureTimeMillis {
            load(doc)
        }

        println("tokenize: $tokenizeOnly  vs. tokenize and load: $tokenizeAndLoad (ms)")
    }
}