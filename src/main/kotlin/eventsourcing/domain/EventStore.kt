package eventsourcing.domain

import java.lang.Exception

interface EventStore {
    fun saveEvents(aggregateType: AggregateType, aggregateId: AggregateID, events: Iterable<Event>, expectedVersion: Long? = null)
    fun getEventsForAggregate(aggregateType: AggregateType, aggregateId: AggregateID): Iterable<Event>

    // FIXME publish events to subscribers
}

class AggregateNotFoundException(aggregateType: AggregateType, aggregateID: AggregateID)
    : Exception("Aggregate not found. ${aggregateType.toString()}:${aggregateID.toString()}")

class ConcurrencyException(aggregateType: AggregateType, aggregateID: AggregateID, expectedVersion: Long, actualVersion: Long)
    : Exception("Concurrency violation on ${aggregateType.toString()}:${aggregateID.toString()}. Expected version: $expectedVersion, Actual version: $actualVersion")
