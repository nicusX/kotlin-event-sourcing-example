package eventsourcing.eventstore

import eventsourcing.domain.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Implements most of the logic of the Event Store, without implementing the storage
 */
abstract class BaseEventStore(private val eventPublisher : EventPublisher<Event>) : EventStore {
    protected data class StreamKey(val aggregateType: AggregateType, val aggregateID: AggregateID) {
        override fun toString(): String = "$aggregateType:$aggregateID"
    }

    protected data class EventDescriptor(val streamKey: StreamKey, val version: Long, val event: Event)

    protected abstract fun stream(key: StreamKey): Iterable<EventDescriptor>

    protected abstract fun appendEventDescriptor(key: StreamKey, eventDescriptor: EventDescriptor)

    protected abstract fun isEmptyStream(key: StreamKey) : Boolean

    // FIXME change the interface to return Iterable<Event>? and let another layer to throw exceptions if the aggregate has no event
    override fun getEventsForAggregate(aggregateType: AggregateType, aggregateId: AggregateID): Iterable<Event> {
        log.debug("Retrieving events for aggregate {}:{}", aggregateType, aggregateId)
        val key = StreamKey(aggregateType, aggregateId)
        if( isEmptyStream(key) ) throw AggregateNotFoundException(aggregateType, aggregateId)
        return stream(key).map{ it.event }
    }

    override fun saveEvents(aggregateType: AggregateType, aggregateId: AggregateID, events: Iterable<Event>, expectedVersion: Long?) {
        val streamKey = StreamKey(aggregateType, aggregateId)
        log.debug("Saving new events for {}. Expected version: {}", streamKey, expectedVersion ?: "-none-")

        if( expectedVersion != null) checkLatestEventVersionInStreamMatches(streamKey, expectedVersion)

        appendAndPublish(streamKey, events, expectedVersion)
    }

    private fun checkLatestEventVersionInStreamMatches(key: StreamKey, expectedVersion: Long) {
        if ( !isEmptyStream(key) ) {
            log.trace("Checking whether the latest event in Stream {} matches {}", key, expectedVersion)
            val latestEventDescriptor = stream(key).last()
            if ( latestEventDescriptor.version != expectedVersion )
                throw ConcurrencyException(key.aggregateType, key.aggregateID, expectedVersion, latestEventDescriptor.version)
        }
    }

    private fun appendAndPublish(streamKey: StreamKey, events: Iterable<Event>, previousAggregateVersion: Long?  )  {
        val baseVersion = previousAggregateVersion ?: -1
        for ( (i, event) in events.withIndex()) {
            val eventVersion = baseVersion + i + 1

            // Events have a version when stored in a stream and published
            val versionedEvent = event.copyWithVersion(eventVersion)
            val eventDescriptor = EventDescriptor(streamKey, eventVersion, versionedEvent)

            log.debug("Appending event {} to Stream {}", eventDescriptor, streamKey)
            appendEventDescriptor(streamKey, eventDescriptor )

            log.trace("Publishing event: {}", versionedEvent)
            eventPublisher.publish(versionedEvent)
        }
    }

    companion object {
        val log : Logger = LoggerFactory.getLogger(BaseEventStore::class.java)
    }
}