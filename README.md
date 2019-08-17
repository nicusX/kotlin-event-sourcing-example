# Simple Event-Sourcing/CQRS example, in Kotlin

[![Build Status](https://travis-ci.org/nicusX/kotlin-event-sourcing-example.svg?branch=master)](https://travis-ci.org/nicusX/kotlin-event-sourcing-minimal)

This project is for demonstration purposes and a learning exercise.

It implements a simple Event-Sourcing system, based on [Greg Young's SimpleCQRS](https://github.com/gregoryyoung/m-r).

Differently from the original example, this is in Kotlin. 
More importantly it uses a different domain with some additional features to make it slightly more realistic.

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
No API documentation, but endpoints may be easily inferred looking at the [implementation](src/main/kotlin/eventsourcing/api)

Some basic business rules are enforced, like not enrolling the same student twice or creating a class with no seats.
There are more business rules to be added and simulating some non-idempotent side-effect, like sending a (fake) email to a 
newly registered Student.

## The implementation

Both the Event Store and all datastores backing Read Models are in-memory.

The "message bus" publishing Events to all Event Handlers (only Projections here) is in-memory, but asynchronous.

The Query side of the system is updated asynchronously and only eventually consistent with the state of the aggregates. 
As it is in real, distributed CQRS system. Though, in this case, latency is negligible (it is possible to simulate an higher latency).

The Write model supports a form of optimistic consistency to protect from concurrent changes to an Aggregate.
Read Models provide the version of Aggregates and Commands contain the version of Aggregate they are expected to be applied to.

The design follows a classic DDD, very OOP and not much functional, style.
Exceptions are used as signal for expected conditions, like a business rule violation.
There is room of improvement here, but it would require introducing some functional library like [Arrow](https://arrow-kt.io).

The Write side of the system is completely synchronous and blocking.

### Why SpringBoot?

Because I am lazy ;) and I do not want to spend to time with the boilerplate and my focus here is different

Spring is not actually used much other than for the REST API layer and for running some E2E tests in-memory.

All dependencies are initialised and wired manually in [`Application`](src/main/kotlin/eventsourcing/Application.kt)

### Other implementation notes

To allow mocking non-open classes, the Mockito `mock-maker-inline` has been enabled. See https://antonioleiva.com/mockito-2-kotlin/
