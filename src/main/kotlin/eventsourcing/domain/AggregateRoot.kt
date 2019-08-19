package eventsourcing.domain

import arrow.core.Either
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

typealias AggregateID = String

interface AggregateType

abstract class AggregateRoot(val id: AggregateID) {

    abstract fun aggregateType(): AggregateType

    private val uncommittedChanges = ArrayList<Event>()

    fun getUncommittedChanges(): Iterable<Event> = uncommittedChanges.toList().asIterable()

    fun markChangesAsCommitted() {
        log.debug("Marking all changes as committed")
        uncommittedChanges.clear()
    }

    protected fun <A: AggregateRoot> applyAndQueueEvent(event: Event): A {
        applyEvent(event)
        log.debug("Appending event {} to uncommitted changes", event)
        uncommittedChanges.add(event)
        return this as A
    }


    protected abstract fun applyEvent(event: Event): AggregateRoot

    class UnsupportedEventException(eventClass: Class<out Event>)
        : Exception("Unsupported event ${eventClass.canonicalName}")

    companion object {
        val log: Logger = LoggerFactory.getLogger(AggregateRoot::class.java)

        fun <A: AggregateRoot> loadFromHistory(aggregate: A, history: Iterable<Event>): A {
            log.debug("Reloading aggregate {} state from history", aggregate)
            for (event: Event in history) {
                aggregate.applyEvent(event)
            }
            return aggregate
        }
    }
}
