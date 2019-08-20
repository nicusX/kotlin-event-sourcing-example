package eventsourcing.eventstore

import arrow.core.Option
import eventsourcing.domain.Event
import eventsourcing.domain.EventPublisher

/**
 * Store event streams in memory
 * This is not meant to be optimised
 */
class InMemoryEventStore(eventPublisher : EventPublisher<Event>) : BaseEventStore(eventPublisher) {
    private val streams: MutableMap<StreamKey,  MutableList<EventDescriptor>> = mutableMapOf()

    @Synchronized override fun stream(key: StreamKey): Option<Iterable<EventDescriptor>> =
            Option.fromNullable( streams[key] ).map { it.toList() }

    @Synchronized override fun appendEventDescriptor(key: StreamKey, eventDescriptor: EventDescriptor) {
        val stream = streams[key] ?: mutableListOf()
        stream.add(eventDescriptor)
        streams[key] = stream
    }
}
