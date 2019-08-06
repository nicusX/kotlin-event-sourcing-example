package eventsourcing.domain

import org.slf4j.LoggerFactory
import java.util.*

// FIXME make these real types (inline?)
typealias AggregateID = String

interface AggregateType

abstract class AggregateRoot(val id: AggregateID) {

    // FIXME does this get the runtime class?
    protected val log = LoggerFactory.getLogger(this.javaClass)

    abstract fun aggregateType(): AggregateType

    private val uncommittedChanges = ArrayList<Event>()

    fun getUncommittedChanges() : Iterable<Event> = uncommittedChanges.asIterable()

    fun markChangesAsCommitted() {
        uncommittedChanges.clear()
    }

    fun loadFromHistory(history: Iterable<Event>) {
        log.debug("Reloading aggregate state from history")
        for(event: Event in history)
            applyChange(event)
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

    class ApplyingAnEventToTheIncorrectAggregateVersionExcetpion(eventClass: Class<out Event>, expectedVersion: Long, actualVersion: Long)
        : Exception("Applying ${eventClass.canonicalName} to the wrong aggregate version. Expected:$expectedVersion, actual: $actualVersion")
}

