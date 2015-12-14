package io.dahgan

import io.dahgan.parser.PatternTokenizer

/**
 * Converts the Unicode input (called name in error messages) to a list of 'Token' according to the YAML spec. This is it!
 */
fun yaml() = PatternTokenizer(`l-yaml-stream`)
