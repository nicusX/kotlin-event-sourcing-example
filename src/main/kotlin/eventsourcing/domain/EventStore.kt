package eventsourcing.domain

interface EventStore {
    fun saveEvents(aggregateType: AggregateType, aggregateId: AggregateID, events: Iterable<Event>, expectedVersion: Long? = null)
    fun getEventsForAggregate(aggregateType: AggregateType, aggregateId: AggregateID): Iterable<Event>

    // FIXME publish events to subscribers
}