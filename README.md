# Minimalistic Event-Sourcing example in Kotlin

[![Build Status](https://travis-ci.org/nicusX/kotlin-event-sourcing-minimal.svg?branch=master)](https://travis-ci.org/nicusX/kotlin-event-sourcing-minimal)

Learning exercise, implementing a very simple Event-Sourcing, based on [Greg Young's SimpleCQRS](https://github.com/gregoryyoung/m-r),
rewritten in Kotlin and for a different domain.

The quality of code is suboptimal: the original SimpleCQRS example is very old-fashioned-OOP, everything-mutable code. 
I am improving some aspects without diverging too much from the original architecture.


## General notes

- To allow mocking non-open classes, the Mockito `mock-maker-inline` has been enabled. See https://antonioleiva.com/mockito-2-kotlin/
