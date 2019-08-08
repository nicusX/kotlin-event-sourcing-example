package eventsourcing.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Repository<A: AggregateRoot> {
    fun getById(id: AggregateID): A
    fun save(aggregate: A, expectedVersion: Long? = null)
    fun new(id: AggregateID) : A
}

abstract class EventSourcedRepository<A: AggregateRoot>(eventStore: EventStore) : Repository<A> {

    private val store = eventStore

    override fun save(aggregate: A, expectedVersion: Long?) {
        log.debug("Storing uncommitted event for '${aggregate.aggregateType()}' id: $aggregate.id")
        val uncommitedChanges = aggregate.getUncommittedChanges()
        store.saveEvents(aggregate.aggregateType(), aggregate.id, uncommitedChanges, expectedVersion)
        aggregate.markChangesAsCommitted()
    }

    override fun getById(id: AggregateID): A {
        val aggregate = new(id)
        log.debug("Retrieve {} by id:{}", aggregate.aggregateType(), id)
        val events = store.getEventsForAggregate(aggregate.aggregateType(), id)
        AggregateRoot.loadFromHistory(aggregate, events)
        return aggregate
    }

    companion object {
        val log : Logger = LoggerFactory.getLogger(EventSourcedRepository::class.java)
    }
}

