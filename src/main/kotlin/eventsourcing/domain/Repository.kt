package eventsourcing.domain

import arrow.core.Either
import arrow.core.None
import arrow.core.Option

interface Repository<A: AggregateRoot> {
    fun getById(id: AggregateID): Option<A>
    fun save(aggregate: A, expectedVersion: Option<Long> = None) : Either<Problem, Success>
    fun new(id: AggregateID) : A
}
