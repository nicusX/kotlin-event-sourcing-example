# Minimalistic Event-Sourcing example in Kotlin

[![Build Status](https://travis-ci.org/nicusX/kotlin-event-sourcing-minimal.svg?branch=master)](https://travis-ci.org/nicusX/kotlin-event-sourcing-minimal)

This project is for demonstration purposes and a learning exercise, implementing a simple Event-Sourcing system, based 
on [Greg Young's SimpleCQRS](https://github.com/gregoryyoung/m-r) in Kotlin.

The domain is different from the original examples and I have added some features common in real systems.

## The Domain

The domain implemented is a training class management.

Supported Commands are:

* Schedule New Class
* Register New Student
* Enroll a Student to a Class
* Unenroll a Student from a Class

It exposes some Read Models:

* Student Details
* List of Students (note this is implemented as a separate Read Model for demonstration purposes)
* List of Classes and Class Details, including contacts of enrolled Students

All commands and read model are exposed through a REST API, notably following a
[REST-without-PUT](https://www.thoughtworks.com/insights/blog/rest-api-design-resource-modeling) approach.

No API documentation. But endpoints may be easily inferred looking at the [implementation](src/main/kotlin/eventsourcing/api)

## The implementation

Both the Event Store and all datastores backing Read Models are in-memory.

The "message bus" publishing Events to all Event Handlers (only Projections here) is in-memory, but asynchronous.

Read Models are asynchronously updated and only eventually consistent with the state of the aggregates, as in a real, 
distributed system. Even though the latency is, in this case, negligible (it is possible to simulate a latency).

The model supports a form of optimistic consistency to protect from concurrent changes to an Aggregate by multiple clients.
Each Command contains the version of aggregate it is expected to be applied to.

The design follows a classic DDD, very OOP and not much functional, style.
Exceptions are used as signal for expected conditions, like a business rule violation.

There is room of improvement here, but it would require introducing some functional library like [Arrow](https://arrow-kt.io).

Most of the code is blocking, with the exception of the "event bus". Again, there is room of improvement here.

## Why SpringBoot?

Because I am lazy ;) and I do not want to spend to time with the boilerplate and my focus here is different

Spring is not actually used much other than for the REST API layer and for running some E2E tests in-memory.

All dependencies are initialised and wired manually in [`Application`](src/main/kotlin/eventsourcing/Application.kt)

## Other implementation notes

To allow mocking non-open classes, the Mockito `mock-maker-inline` has been enabled. See https://antonioleiva.com/mockito-2-kotlin/
