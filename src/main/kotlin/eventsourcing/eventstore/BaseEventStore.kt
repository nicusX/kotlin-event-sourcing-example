package eventsourcing.eventstore

import arrow.core.Left
import arrow.core.Option
import arrow.core.Right
import arrow.core.getOrElse
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

    protected abstract fun stream(key: StreamKey): Option<Iterable<EventDescriptor>>

    protected abstract fun appendEventDescriptor(key: StreamKey, eventDescriptor: EventDescriptor)


    override fun getEventsForAggregate(aggregateType: AggregateType, aggregateId: AggregateID): Option<Iterable<Event>> {
        log.debug("Retrieving events for aggregate {}:{}", aggregateType, aggregateId)
        val key = StreamKey(aggregateType, aggregateId)
        return stream(key).map{ it.map { it.event } }
    }

    override fun saveEvents(aggregateType: AggregateType, aggregateId: AggregateID, events: Iterable<Event>, expectedVersion: Option<Long>) : Result<EventStoreFailure, Iterable<Event>> {
        val streamKey = StreamKey(aggregateType, aggregateId)
        log.debug("Saving new events for {}. Expected version: {}", streamKey, expectedVersion)

        return if ( stream(streamKey).concurrentChangeDetected(expectedVersion) ) {
            log.debug("Concurrent change detected")
            Left(EventStoreFailure.ConcurrentChangeDetected)
        } else {
            log.debug("Appending and publishing {} events", events.count())
            Right(appendAndPublish(streamKey, events, expectedVersion))
        }
    }


    private fun Option<Iterable<EventDescriptor>>.concurrentChangeDetected(expectedVersion: Option<Long>) : Boolean =
            expectedVersion.map {  expVersion ->
                this.map { events -> events.last() }
                        .exists { event -> event.version != expVersion }
            }.getOrElse { false }



    private fun appendAndPublish(streamKey: StreamKey, events: Iterable<Event>, previousAggregateVersion: Option<Long> ) : Iterable<Event> {
        val baseVersion : Long = previousAggregateVersion.getOrElse { -1 }
        return sequence {
            for ( (i, event) in events.withIndex()) {
                val eventVersion = baseVersion + i + 1

                // Events have a version when stored in a stream and published
                val versionedEvent = event.copyWithVersion(eventVersion)
                yield(versionedEvent)

                val eventDescriptor = EventDescriptor(streamKey, eventVersion, versionedEvent)

                log.debug("Appending event {} to Stream {}", eventDescriptor, streamKey)
                appendEventDescriptor(streamKey, eventDescriptor )

                log.trace("Publishing event: {}", versionedEvent)
                eventPublisher.publish(versionedEvent)
            }
        }.toList()
    }

    companion object {
        val log : Logger = LoggerFactory.getLogger(BaseEventStore::class.java)
    }
}
