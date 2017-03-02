package io.dahgan

import org.junit.Test
import org.junit.runners.Parameterized

class SampleTest : AbstractSpecTest() {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "product file({0})")
        fun products(): Collection<Array<String>> = AbstractSpecTest.products("sample") { true }
    }

    @Test
    fun sample() {
        run()
    }
}