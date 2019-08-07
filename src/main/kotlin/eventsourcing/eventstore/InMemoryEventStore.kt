package eventsourcing.eventstore

import eventsourcing.domain.*
import org.slf4j.LoggerFactory
import java.lang.Exception


class InMemoryEventStore : EventStore {
    companion object {
        val log = LoggerFactory.getLogger(InMemoryEventStore::class.java)
    }

    private data class StreamKey(val aggregateType: AggregateType, val aggregateID: AggregateID)
    private data class EventDescriptor(val streamKey: StreamKey, val version: Long, val event: Event)
    private data class EventStream(val eventDescriptors: MutableList<EventDescriptor> = mutableListOf())

    private val streams: MutableMap<StreamKey, EventStream> = mutableMapOf()

    override fun getEventsForAggregate(aggregateType: AggregateType, aggregateId: AggregateID): Iterable<Event> {
        log.debug("Retrieving events for aggregate {}:{}", aggregateType, aggregateId)
        val streamKey = StreamKey(aggregateType, aggregateId)
        return streams[streamKey]?.eventDescriptors?.map { it.event } ?: throw AggregateNotFoundException(aggregateType, aggregateId)
    }

    override fun saveEvents(aggregateType: AggregateType, aggregateId: AggregateID, events: Iterable<Event>, expectedVersion: Long?) {
        log.debug("Appending new events to aggregate {}:{} (expected version: {})", aggregateType, aggregateId, expectedVersion)
        val streamKey = StreamKey(aggregateType, aggregateId)
        val stream = streams[streamKey] ?: EventStream()

        checkLatestEventVersionMatchesExpected(streamKey, stream.eventDescriptors, expectedVersion)

        stream.append(streamKey, events, expectedVersion ?: 0)

        streams[streamKey] = stream

        // TODO Publish events to subscribers
    }

    private fun checkLatestEventVersionMatchesExpected(streamKey: StreamKey, eventDescriptors: List<EventDescriptor>, expectedVersion: Long?) {
        if ( expectedVersion != null && !eventDescriptors.isEmpty() ) {
            val latestEventDescriptor =  eventDescriptors.last()
            if ( latestEventDescriptor.version != expectedVersion )
                throw ConcurrencyException(streamKey.aggregateType, streamKey.aggregateID, expectedVersion, latestEventDescriptor.version)
        }
    }

    private fun EventStream.append(streamKey: StreamKey, events: Iterable<Event>, aggregateVersion: Long  ) {
        // FIXME the event should contain its version, but the version cannot be assigned until this point
        for ( (i, event) in events.withIndex()) {
            val eventVersion = aggregateVersion + i
            this.eventDescriptors.add( EventDescriptor(streamKey, eventVersion, event) )
        }
    }
}
