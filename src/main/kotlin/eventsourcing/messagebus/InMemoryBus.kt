package eventsourcing.messagebus

import eventsourcing.domain.Event
import eventsourcing.domain.EventPublisher
import org.slf4j.LoggerFactory

class InMemoryBus : EventPublisher<Event> {
    override fun publish(event: Event) {
        log.debug("Event published: {}", event)
       // FIXME do something useful
    }

    companion object {
        private val log = LoggerFactory.getLogger(InMemoryBus::class.java)
    }
}