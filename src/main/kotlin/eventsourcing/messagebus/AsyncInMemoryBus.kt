package eventsourcing.messagebus

import eventsourcing.domain.Event
import eventsourcing.domain.EventPublisher
import eventsourcing.domain.Handles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Implementation of an in-memory event-bus using coroutines to registered events-handler asynchronously
 *
 * The interface to publisher is still blocking
 */
class AsyncInMemoryBus(private val scope: CoroutineScope, bufferSize: Int = 100): EventPublisher<Event> {

    private val bus = BroadcastChannel<Event>(bufferSize)

    override fun publish(event: Event) = runBlocking {
        log.debug("Publishing event {}", event)
        bus.send(event)
        log.trace("Event published")
    }

    override fun register(eventHandler: Handles<Event>) : EventPublisher<Event> {
        log.info("Registering handler: {}", eventHandler)
        scope.launch { broadcastTo(eventHandler) }
        return this
    }

    private suspend fun broadcastTo(handler: Handles<Event>) = coroutineScope {
        log.debug("Starting handler {}", handler)
        bus.consumeEach {
            log.trace("Handler '{}' is handling '{}'", handler, it)
            handler.handle(it)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AsyncInMemoryBus::class.java)
    }

    fun shutdown() {
        bus.close()
    }
}