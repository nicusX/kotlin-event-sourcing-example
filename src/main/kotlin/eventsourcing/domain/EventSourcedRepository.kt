package eventsourcing.domain

import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.getOrHandle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception

abstract class EventSourcedRepository<A: AggregateRoot>(eventStore: EventStore) : Repository<A> {

    private val store = eventStore

    override fun save(aggregate: A, expectedVersion: Long?) {
        log.debug("Storing uncommitted event for '${aggregate.aggregateType()}' id: $aggregate.id")
        val uncommitedChanges = aggregate.getUncommittedChanges()

        val savedEvents: Either<Problem, Iterable<Event>> = store.saveEvents(aggregate.aggregateType(), aggregate.id, uncommitedChanges, Option.fromNullable(expectedVersion))
        if ( savedEvents.isLeft()) throw ConcurrencyException()

        aggregate.markChangesAsCommitted()
    }

    // FIXME change the interface to Option<A>
    override fun getById(id: AggregateID): A {

        val aggregate = new(id)
        val aggregateType = aggregate.aggregateType()
        log.debug("Retrieve {} by id:{}", aggregateType, id)
        val events = store.getEventsForAggregate(aggregate.aggregateType(), id)
                .getOrElse { throw AggregateNotFoundException(aggregateType, id) } // FIX return None instead
        AggregateRoot.loadFromHistory(aggregate, events)
        return aggregate
    }

    companion object {
        val log : Logger = LoggerFactory.getLogger(EventSourcedRepository::class.java)
    }
}

// FIXME remove this exception
@Deprecated("To be removed")
class ConcurrencyException() : Exception()