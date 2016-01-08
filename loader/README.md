# Loader Module
The Loader module is an example to demonstrate how generated YEAST tokens from Core module can be used for further processes.
It takes the generated tokens from Core module and converts them to Kotlin objects.

#### Examples

Loading a YAML file containing a single scalar:
```kotlin
// the yaml text
val text = "a single line"

println(load(text) as String)
```

The output of above execution would be following:
```
a single line
```

Loading a YAML file containing a sequence of data:
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

Loading a YAML file containing a map of data:
```kotlin
// the yaml text
val text = "foo: bar\nbaz: bux"

(load(text) as Map<*, *>).forEach { entry -> println(entry) }
```

The output of above execution would be following:
```
foo=bar
baz=bux
```

Loading a YAML file containing multiple documents:
```kotlin
// the yaml text
val text = "---\nfoo: bar\n...\n---\nbaz: bux\n...."

(loadAll(text) as List<*>).forEachIndexed { index, document ->
    println("Document #${index + 1}")

    (document as Map<*, *>).forEach { entry -> println(entry) }
}
```

The output of above execution would be following:
```
Document #1
foo=bar
Document #2
baz=bux
```

There are other variants of load and loadAll function in Loader.kt to read text, file or byte array. 
