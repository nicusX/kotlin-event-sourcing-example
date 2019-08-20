package eventsourcing.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

typealias AggregateID = String

interface AggregateType

/**
 * Groups common behaviours of all Aggregates
 */
abstract class AggregateRoot(val id: AggregateID) {

    abstract fun aggregateType(): AggregateType

    private val uncommittedChanges = ArrayList<Event>()

    fun getUncommittedChanges(): Iterable<Event> = uncommittedChanges.toList().asIterable()

    fun markChangesAsCommitted() {
        log.debug("Marking all changes as committed")
        uncommittedChanges.clear()
    }

    protected fun <A: AggregateRoot> applyAndQueueEvent(event: Event): A {
        log.debug("Applying {}", event)
        applyEvent(event) // Remember: this never fails

        log.debug("Queueing uncommitted change {}", event)
        uncommittedChanges.add(event)
        return this as A
    }

    protected abstract fun applyEvent(event: Event): AggregateRoot

    companion object {
        val log: Logger = LoggerFactory.getLogger(AggregateRoot::class.java)

        /**
         * Rebuild the state of the Aggregate from its Events
         */
        fun <A: AggregateRoot> loadFromHistory(aggregate: A, history: Iterable<Event>): A {
            log.debug("Reloading aggregate {} state from history", aggregate)

            // Rebuilding an Aggregate state from Events is a 'fold' operation
            return history.fold( aggregate, { agg, event  -> agg.applyEvent(event) as A })
        }
    }
}

class UnsupportedEventException(eventClass: Class<out Event>)
    : Exception("Unsupported event ${eventClass.canonicalName}")
