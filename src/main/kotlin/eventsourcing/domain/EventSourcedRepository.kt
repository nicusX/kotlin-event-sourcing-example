package eventsourcing.domain

import arrow.core.Either
import arrow.core.Option
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception

abstract class EventSourcedRepository<A: AggregateRoot>(eventStore: EventStore) : Repository<A> {

    private val store = eventStore

    override fun save(aggregate: A, expectedVersion: Option<Long>) {
        log.debug("Storing uncommitted event for '${aggregate.aggregateType()}' id: $aggregate.id")
        val uncommitedChanges = aggregate.getUncommittedChanges()

        val savedEvents: Either<EventStoreProblem, Iterable<Event>> = store.saveEvents(aggregate.aggregateType(), aggregate.id, uncommitedChanges, expectedVersion)
        if ( savedEvents.isLeft()) throw ConcurrencyException()

        aggregate.markChangesAsCommitted()
    }

    override fun getById(id: AggregateID): Option<A> {

        val aggregate = new(id)
        val aggregateType = aggregate.aggregateType()
        log.debug("Retrieve {} by id:{}", aggregateType, id)
        return store.getEventsForAggregate(aggregate.aggregateType(), id)
                .map { events -> AggregateRoot.loadFromHistory(aggregate, events)}
    }

    companion object {
        val log : Logger = LoggerFactory.getLogger(EventSourcedRepository::class.java)
    }
}

// FIXME remove this exception
@Deprecated("To be removed")
class ConcurrencyException() : Exception()