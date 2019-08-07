package eventsourcing.eventstore

import eventsourcing.EventsAssert.Companion.assertThatEvents
import eventsourcing.domain.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InMemoryEventStoreTest {

    val AN_AGGREGATE_ID : ClassID = "aggr012"
    val AN_AGGREGATE_TYPE : AggregateType = TrainingClassAggregateType
    val SOME_EVENTS_OF_AN_AGGREGATE : List<Event> = listOf(
            StudentEnrolled(AN_AGGREGATE_ID, "student-1"),
            StudentUnenrolled(AN_AGGREGATE_ID, "student-2"),
            StudentEnrolled(AN_AGGREGATE_ID, "student-3")
    )

    val MORE_EVENTS_OF_AN_AGGREGATE : List<Event> = listOf(
            StudentEnrolled(AN_AGGREGATE_ID, "student-4"),
            StudentEnrolled(AN_AGGREGATE_ID, "student-5")
    )

    val ANOTHER_AGGREGATE_ID : ClassID = "aggr045"
    val EVENTS_OF_ANOTHER_AGGREGATE : List<Event> = listOf(
            StudentEnrolled(ANOTHER_AGGREGATE_ID, "student-X"),
            StudentEnrolled(ANOTHER_AGGREGATE_ID, "student-Y")
    )

    @Test
    fun `should store events, with no expected versions, and retrieve them back`() {
        val sut: EventStore = givenAnInMemoryEventStore (
                { withSavedEvents(AN_AGGREGATE_TYPE, AN_AGGREGATE_ID, SOME_EVENTS_OF_AN_AGGREGATE)})

        val extractedEvents = sut.getEventsForAggregate(AN_AGGREGATE_TYPE, AN_AGGREGATE_ID)

        assertThatEvents(extractedEvents).onlyContainsInOrder(SOME_EVENTS_OF_AN_AGGREGATE)
    }

    @Test
    fun `should throw ConcurrencyException if the expected version does not match actual version`() {
        val sut : EventStore = givenAnInMemoryEventStore(
                { withSavedEvents(AN_AGGREGATE_TYPE, AN_AGGREGATE_ID, SOME_EVENTS_OF_AN_AGGREGATE) } )

        assertThrows<ConcurrencyException> {
            sut.saveEvents(AN_AGGREGATE_TYPE, AN_AGGREGATE_ID, MORE_EVENTS_OF_AN_AGGREGATE, 42)
        }
    }

    @Test
    fun `should retrieve only events of the specified aggregate`() {
        val sut : EventStore = givenAnInMemoryEventStore (
                { withSavedEvents(AN_AGGREGATE_TYPE, AN_AGGREGATE_ID, SOME_EVENTS_OF_AN_AGGREGATE) },
                { withSavedEvents(AN_AGGREGATE_TYPE, ANOTHER_AGGREGATE_ID, EVENTS_OF_ANOTHER_AGGREGATE) })


        val extractedEvents = sut.getEventsForAggregate(AN_AGGREGATE_TYPE, AN_AGGREGATE_ID)

        assertThatEvents(extractedEvents).onlyContainsInOrder(SOME_EVENTS_OF_AN_AGGREGATE)
    }

    @Test
    fun `should throw AggregateNotFoundException when retrieving events for a non-existing aggregate`() {
        val sut : EventStore = givenAnInMemoryEventStore(
                { withSavedEvents(AN_AGGREGATE_TYPE, AN_AGGREGATE_ID, SOME_EVENTS_OF_AN_AGGREGATE) } )

        assertThrows<AggregateNotFoundException> {
            sut.getEventsForAggregate(AN_AGGREGATE_TYPE, "this-id-does-not-exist")
        }
    }

}

internal fun givenAnInMemoryEventStore(vararg inits: EventStore.() -> Unit ) : EventStore {
    val es = InMemoryEventStore()
    for( init in inits )
        es.init()
    return es
}

internal fun EventStore.withSavedEvents(aggregateType: AggregateType, aggregateId: AggregateID, events: Iterable<Event>) {
    saveEvents(aggregateType, aggregateId, events)
}
