# Minimalistic Event-Sourcing example in Kotlin

[![Build Status](https://travis-ci.org/nicusX/kotlin-event-sourcing-minimal.svg?branch=master)](https://travis-ci.org/nicusX/kotlin-event-sourcing-minimal)

Learning exercise, implementing a very simple Event-Sourcing, based on [Greg Young's SimpleCQRS](https://github.com/gregoryyoung/m-r),
rewritten in Kotlin and for a different domain.


## General notes

- To allow mocking non-open classes, the Mockito `mock-maker-inline` has been enabled. See https://antonioleiva.com/mockito-2-kotlin/
- Apologies for the style of the code.
  The original SimpleCQRS example is old-fashioned OOP, everything is mutable and (ab)use of inheritance a lot.
  This does not fit well with a more functional approach.
  I am improving some aspects but I do not want to diverge too much from the original design.
