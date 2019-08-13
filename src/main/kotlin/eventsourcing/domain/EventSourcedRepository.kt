package eventsourcing.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception

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
        val aggregateType = aggregate.aggregateType()
        log.debug("Retrieve {} by id:{}", aggregateType, id)
        val events = store.getEventsForAggregate(aggregate.aggregateType(), id)
                ?: throw AggregateNotFoundException(aggregateType, id)
        AggregateRoot.loadFromHistory(aggregate, events)
        return aggregate
    }

    companion object {
        val log : Logger = LoggerFactory.getLogger(EventSourcedRepository::class.java)
    }
}

class AggregateNotFoundException(aggregateType: AggregateType, aggregateID: AggregateID)
    : Exception("Aggregate not found. ${aggregateType.toString()}:${aggregateID.toString()}")
