# Dahgan
A YAML 1.2 syntax parser written in [Kotlin](https://kotlinlang.org/)

[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status - Master](https://travis-ci.org/kareez/dahgan.svg?branch=master)](https://travis-ci.org/kareez/dahgan)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mhshams/dahgan/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mhshams/dahgan)

## About
Dahgan is a YAML syntax parser, generated directly from the YAML 1.2 specification, together with a YEAST tokenizer that allows converting YAML files to YEAST tokens.

The Core module is a [Kotlin](https://kotlinlang.org/) implementation of a Haskell based YAML parser called [YamlReference](https://hackage.haskell.org/package/YamlReference). 

## Modules
#### Core
The Core module consists of YAML 1.2 specification, the parser and the YEAST tokenizer. [Read More](core/README.md)
#### Loader
The Loader module takes the generated tokens from Core module and generates Kotlin objects. [Read More](loader/README.md)
