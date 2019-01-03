package io.dahgan.loader

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.junit.Ignore
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Performance Issue in Loader
 */
@Ignore("Only for performance monitoring")
class PerformanceTest {

    private val mapper = ObjectMapper(YAMLFactory())

    @Test
    fun sample() {

        val doc = (
                """
                ---
                - items:
                """ + """
                  # a comment here
                  - name: A
                    option: A
                """.repeat(100)
                ).trimIndent().toByteArray(Charsets.UTF_8)

        val loadTime = measureTimeMillis {
            load(doc)
        }

        val jacksonTime = measureTimeMillis {
            mapper.readTree(doc)
        }

        println("dahgan: $loadTime vs jackson: $jacksonTime (ms)")
    }
}
