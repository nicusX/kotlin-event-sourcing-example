package eventsourcing.domain

import arrow.core.Either
import arrow.core.None
import arrow.core.Option

interface EventStore {
    fun saveEvents(
            aggregateType: AggregateType,
            aggregateId: AggregateID,
            events: Iterable<Event>,
            expectedVersion: Option<Long> = None) : Either<Problem, Iterable<Event>>

    fun getEventsForAggregate(aggregateType: AggregateType, aggregateId: AggregateID): Option<Iterable<Event>>
}


sealed class Problem {
    object ConcurrentChangeDetected : Problem()
}