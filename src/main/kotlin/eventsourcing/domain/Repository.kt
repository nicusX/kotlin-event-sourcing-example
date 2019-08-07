package eventsourcing.domain

import eventsourcing.domain.AggregateRoot.Companion.loadFromHistory
import org.slf4j.LoggerFactory

interface Repository<A: AggregateRoot> {
    fun getById(id: AggregateID): A
    fun save(aggregate: A, expectedVersion: Long)
}

abstract class EventSourcedRepository<A: AggregateRoot>(eventStore: EventStore) : Repository<A> {

    companion object {
        val log = LoggerFactory.getLogger(EventSourcedRepository::class.java)
    }

    private val store = eventStore

    override fun save(aggregate: A, expectedVersion: Long) {
        log.debug("Storing uncommitted event for '${aggregate.aggregateType()}' id: $aggregate.id")
        store.saveEvents(aggregate.aggregateType(), aggregate.id, aggregate.getUncommittedChanges(), expectedVersion)
    }

    override fun getById(id: AggregateID): A {
        val aggregate = new(id)
        log.debug("Retrieve {} by id:{}", aggregate.aggregateType(), id)
        val events = store.getEventsForAggregate(aggregate.aggregateType(), id)
        loadFromHistory(aggregate, events)
        return aggregate
    }

    abstract fun new(id: AggregateID) : A
}

