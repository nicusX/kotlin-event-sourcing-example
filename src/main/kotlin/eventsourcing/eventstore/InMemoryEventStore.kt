package eventsourcing.eventstore

import eventsourcing.domain.Event
import eventsourcing.domain.EventPublisher

/**
 * Store event streams in memory
 */
class InMemoryEventStore(eventPublisher : EventPublisher<Event>) : BaseEventStore(eventPublisher) {
    private val streams: MutableMap<StreamKey,  MutableList<EventDescriptor>> = mutableMapOf()

    override fun stream(key: StreamKey): Iterable<EventDescriptor> = streams[key]?.toList() ?: emptyList()

    override fun isEmptyStream(key: StreamKey): Boolean = streams[key]?.isEmpty() ?: true

    override fun appendEventDescriptor(key: StreamKey, eventDescriptor: EventDescriptor) {
        val stream = streams[key] ?: mutableListOf()
        stream.add(eventDescriptor)
        streams[key] = stream
    }
}
