package eventsourcing.domain

import arrow.core.Either

interface Failure

interface Success

typealias Result<A,B> = Either<A, B>

object AggregateNotFound : Failure
