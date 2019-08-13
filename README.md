# Minimalistic Event-Sourcing example in Kotlin

[![Build Status](https://travis-ci.org/nicusX/kotlin-event-sourcing-minimal.svg?branch=master)](https://travis-ci.org/nicusX/kotlin-event-sourcing-minimal)

This is a learning exercise, implementing a very simple Event-Sourcing, based on [Greg Young's SimpleCQRS](https://github.com/gregoryyoung/m-r),
rewritten in Kotlin, for a different domain and with a fairly reasonable test coverage.


## General notes

To allow mocking non-open classes, the Mockito `mock-maker-inline` has been enabled. See https://antonioleiva.com/mockito-2-kotlin/

Apologies for the style of the code.
The original SimpleCQRS example is old-fashioned OOP, everything is mutable, imperative and (ab)uses of inheritance.
I am improving some aspects but I do not want to diverge too much from the original design.
