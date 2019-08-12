package eventsourcing.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

typealias AggregateID = String

interface AggregateType

abstract class AggregateRoot(val id: AggregateID) {

    abstract fun aggregateType(): AggregateType

    private val uncommittedChanges = ArrayList<Event>()

    fun getUncommittedChanges() : Iterable<Event> = uncommittedChanges.toList().asIterable()

    fun markChangesAsCommitted() {
        log.debug("Marking all changes as committed")
        uncommittedChanges.clear()
    }

    protected fun applyChangeAndQueueEvent(event : Event) {
        applyChange(event)

        log.debug("Appending event {} to uncommitted changes", event)
        uncommittedChanges.add(event)
    }

    private fun applyChange(event : Event) {
        log.debug("Applying event: {}", event)
        apply(event)
    }

    protected abstract fun apply( event: Event )

    class UnsupportedEventException(eventClass: Class<out Event> )
        : Exception("Unsupported event ${eventClass.canonicalName}")

    companion object {
        val log : Logger = LoggerFactory.getLogger(AggregateRoot::class.java)

        fun <A : AggregateRoot> loadFromHistory(aggregate: A,  history: Iterable<Event>) : A  {
            log.debug("Reloading aggregate {} state from history", aggregate)
            for(event: Event in history) {
                aggregate.applyChange(event)
            }
            return aggregate
        }
    }
}

