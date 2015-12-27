# Dahgan
A YAML Syntax Parser written in [Kotlin](https://kotlinlang.org/)

[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status - Master](https://travis-ci.org/kareez/dahgan.svg?branch=master)](https://travis-ci.org/kareez/dahgan)
[![Build Status - Develop](https://travis-ci.org/kareez/dahgan.svg?branch=develop)](https://travis-ci.org/kareez/dahgan)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.kareez/dahgan/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.kareez/dahgan)

## About
Dahgan is a YAML syntax parser, generated directly from the YAML specification, together with a YEAST tokenizer that allows converting YAML files to YEAST tokens.
The Core module is a [Kotlin](https://kotlinlang.org/) implementation of a haskell-based YAML parser called [YamlReference](https://hackage.haskell.org/package/YamlReference). 

## Core Module
The Core module consist of YAML specification, the parser and the YEAST tokenizer. 

### Tokenizer Sample
Following code is a sample way to explore the tokenizer.
```kotlin
        // the yaml text
        val text = "- foo\n- bar\n- baz"

        // tokenize the text and print the generated tokens
        yaml().tokenize("a name", text.toByteArray(), true).forEach { token -> println(token) }
```

The output of above execution would be following tokens:
```
# B: 0, C: 0, L: 1, c: 0
O

# B: 0, C: 0, L: 1, c: 0
N

# B: 0, C: 0, L: 1, c: 0
Q

# B: 0, C: 0, L: 1, c: 0
I-

# B: 1, C: 1, L: 1, c: 1
w 

# B: 2, C: 2, L: 1, c: 2
N

# B: 2, C: 2, L: 1, c: 2
S

# B: 2, C: 2, L: 1, c: 2
Tfoo

# B: 5, C: 5, L: 1, c: 5
s

# B: 5, C: 5, L: 1, c: 5
n

# B: 5, C: 5, L: 1, c: 5
b\x0a

# B: 6, C: 6, L: 2, c: 0
I-

# B: 7, C: 7, L: 2, c: 1
w 

# B: 8, C: 8, L: 2, c: 2
N

# B: 8, C: 8, L: 2, c: 2
S

# B: 8, C: 8, L: 2, c: 2
Tbar

# B: 11, C: 11, L: 2, c: 5
s

# B: 11, C: 11, L: 2, c: 5
n

# B: 11, C: 11, L: 2, c: 5
b\x0a

# B: 12, C: 12, L: 3, c: 0
I-

# B: 13, C: 13, L: 3, c: 1
w 

# B: 14, C: 14, L: 3, c: 2
N

# B: 14, C: 14, L: 3, c: 2
S

# B: 14, C: 14, L: 3, c: 2
Tbaz

# B: 17, C: 17, L: 3, c: 5
s

# B: 17, C: 17, L: 3, c: 5
n

# B: 17, C: 17, L: 3, c: 5
q

# B: 17, C: 17, L: 3, c: 5
n

# B: 17, C: 17, L: 3, c: 5
o
```

## Loader Module
The Loader module takes the generated tokens from Core module and generated Kotlin objects.

### Loader Sample
Following code is an example usage of YAML loader.
```kotlin
        // the yaml text
        val text = "- foo\n- bar\n- baz"

        (load(text) as List<*>).forEach { scalar -> println(scalar) }
```

The output of above execution would be following:
```
foo
bar
baz
```