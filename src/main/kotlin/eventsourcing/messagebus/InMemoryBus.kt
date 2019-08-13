package eventsourcing.messagebus

import eventsourcing.domain.Event
import eventsourcing.domain.EventPublisher
import eventsourcing.domain.Handles
import org.slf4j.LoggerFactory

/**
 * Simple, in-memory event-bus, broadcasting events to all registered handlers synchronously
 */
class InMemoryBus : EventPublisher<Event> {
    private val handlers : MutableList<Handles<Event>> = mutableListOf()

    override fun publish(event: Event) {
       log.debug("Event published: {}", event)
       for(h in handlers) {
           h.handle(event)
       }
    }

    override fun register(eventHandler: Handles<Event>) : EventPublisher<Event> {
        log.info("Registering a new event handler {}", eventHandler)
        handlers += eventHandler
        return this
    }

    companion object {
        private val log = LoggerFactory.getLogger(InMemoryBus::class.java)
    }
}