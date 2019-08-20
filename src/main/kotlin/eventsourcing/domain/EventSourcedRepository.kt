package eventsourcing.domain

import arrow.core.Option
import arrow.core.Right
import arrow.core.flatMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Repository implementing persistence through Event Sourcing:
 * - save uncommited changes (Events) of an Aggregate
 * - rebuild Aggregate from its Events
 */
abstract class EventSourcedRepository<A: AggregateRoot>(eventStore: EventStore) : Repository<A> {

    private val store = eventStore

    override fun save(aggregate: A, expectedVersion: Option<Long>) : Result<Failure, ChangesSuccessfullySaved> {
        log.debug("Storing uncommitted event for '${aggregate.aggregateType()}:$aggregate.id'")
        val uncommitedChanges = aggregate.getUncommittedChanges()

        return store.saveEvents(aggregate.aggregateType(), aggregate.id, uncommitedChanges, expectedVersion)
                .flatMap{ _ ->
                    aggregate.markChangesAsCommitted()
                    Right(ChangesSuccessfullySaved)
                }
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

object ChangesSuccessfullySaved : Success
