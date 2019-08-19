package eventsourcing.eventstore

import arrow.core.Option
import arrow.core.Some
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import eventsourcing.EventsAssert.Companion.assertThatEvents
import eventsourcing.ResultAssert.Companion.assertThatResult
import eventsourcing.domain.*
import eventsourcing.domain.EventStoreFailure.ConcurrentChangeDetected
import org.junit.jupiter.api.Test

val AGGREGATE_TYPE : AggregateType = TrainingClass.TYPE
const val AN_AGGREGATE_ID : ClassID = "aggr012"
const val ANOTHER_AGGREGATE_ID : ClassID = "aggr045"


internal class InMemoryEventStoreTest {

    @Test
    fun `given an Event Store containing 3 Events of a single Aggregate, when I retrieve the Events of the Aggregate, then it returns all Events, in order, with version 0 to 2`() {

        val sut: EventStore = givenAnInMemoryEventStore (
                { withSavedEvents(AGGREGATE_TYPE, AN_AGGREGATE_ID, listOf(
                    StudentEnrolled(AN_AGGREGATE_ID, "student-1"),
                    StudentUnenrolled(AN_AGGREGATE_ID, "student-2", "some reasons"),
                    StudentEnrolled(AN_AGGREGATE_ID, "student-3")
                )) })

        val extractedEvents = sut.getEventsForAggregate(AGGREGATE_TYPE, AN_AGGREGATE_ID)

        val expectedVersionedEvents = listOf(
                StudentEnrolled(AN_AGGREGATE_ID, "student-1", 0),
                StudentUnenrolled(AN_AGGREGATE_ID, "student-2", "some reasons", 1),
                StudentEnrolled(AN_AGGREGATE_ID, "student-3", 2)
        )
        assertThatEvents(extractedEvents)
                .isDefined()
                .onlyContainsInOrder(expectedVersionedEvents)
    }

    @Test
    fun `given an Event Store containing 3 events of a single Aggregate, when I store new events specifying version = 2, then it succeeds`() {
        val sut : EventStore = givenAnInMemoryEventStore(
                { withSavedEvents(AGGREGATE_TYPE, AN_AGGREGATE_ID, listOf(
                        StudentEnrolled(AN_AGGREGATE_ID, "student-1"),
                        StudentUnenrolled(AN_AGGREGATE_ID, "student-2","some reasons"),
                        StudentEnrolled(AN_AGGREGATE_ID, "student-3")
                )) } )

        val moreEvents = listOf(
                StudentEnrolled(AN_AGGREGATE_ID, "student-4"),
                StudentEnrolled(AN_AGGREGATE_ID, "student-5")
        )

        val aggregateVersion = 2L
        val result = sut.saveEvents(AGGREGATE_TYPE, AN_AGGREGATE_ID, moreEvents, Some(aggregateVersion))

        assertThatResult(result).isSuccess()
    }


    @Test
    fun `given an Event Store containing 3 events of a single Aggregate, when I store new events specifying a version != 2, than it fail with a ConcurrentChangeDetected`() {
        val sut : EventStore = givenAnInMemoryEventStore(
                { withSavedEvents(AGGREGATE_TYPE, AN_AGGREGATE_ID, listOf(
                        StudentEnrolled(AN_AGGREGATE_ID, "student-1"),
                        StudentUnenrolled(AN_AGGREGATE_ID, "student-2", "some reasons"),
                        StudentEnrolled(AN_AGGREGATE_ID, "student-3")
                )) } )

        val moreEvents = listOf(
                StudentEnrolled(AN_AGGREGATE_ID, "student-4"),
                StudentEnrolled(AN_AGGREGATE_ID, "student-5")
        )

        val aggregateVersion = 42L
        val result: Result<EventStoreFailure, Iterable<Event>> = sut.saveEvents(AGGREGATE_TYPE, AN_AGGREGATE_ID, moreEvents, Some(aggregateVersion))

        assertThatResult(result)
                .isFailure()
                .failureIsA<ConcurrentChangeDetected>()
    }



    @Test
    fun `given an Event Store containing events of 2 Aggregates, when I retrieve events of one aggregate, then it retrieves Events of the specified Aggregate`() {
        val sut : EventStore = givenAnInMemoryEventStore (
                { withSavedEvents(AGGREGATE_TYPE, AN_AGGREGATE_ID, listOf(
                        StudentEnrolled(AN_AGGREGATE_ID, "student-1"),
                        StudentUnenrolled(AN_AGGREGATE_ID, "student-2", "some reasons"),
                        StudentEnrolled(AN_AGGREGATE_ID, "student-3")
                )) },
                { withSavedEvents(AGGREGATE_TYPE, ANOTHER_AGGREGATE_ID, listOf(
                        StudentEnrolled(ANOTHER_AGGREGATE_ID, "student-4"),
                        StudentEnrolled(ANOTHER_AGGREGATE_ID, "student-5")
                )) })


        val extractedEvents: Option<Iterable<Event>> = sut.getEventsForAggregate(AGGREGATE_TYPE, AN_AGGREGATE_ID)

        val expectedVersionedEvents = listOf(
                StudentEnrolled(AN_AGGREGATE_ID, "student-1", 0),
                StudentUnenrolled(AN_AGGREGATE_ID, "student-2", "some reasons",1),
                StudentEnrolled(AN_AGGREGATE_ID, "student-3", 2)
        )

        assertThatEvents(extractedEvents)
                .isDefined()
                .onlyContainsInOrder(expectedVersionedEvents)
    }

    @Test
    fun `given an Event Store containing Events of a single Aggregate, when I retrieve Events for a different Aggregate, then it returns no events`() {
        val sut : EventStore = givenAnInMemoryEventStore(
                { withSavedEvents(AGGREGATE_TYPE, AN_AGGREGATE_ID, listOf(
                        StudentEnrolled(AN_AGGREGATE_ID, "student-1"),
                        StudentUnenrolled(AN_AGGREGATE_ID, "student-2", "some reasons"),
                        StudentEnrolled(AN_AGGREGATE_ID, "student-3")
                )) } )

        val nonExistingAggregateID = "this-id-does-not-exist"
        val events = sut.getEventsForAggregate(AGGREGATE_TYPE, nonExistingAggregateID)
        assertThatEvents(events).isNone()
    }

    @Test
    fun `given an empty Event Store, when I store 3 new Events of the same Aggregate, then it publishes all new Events in order with versions 0,1 and 2`() {
        val publisher = mock<EventPublisher<Event>>()
        val sut : EventStore = givenAnInMemoryEventStore(eventPublisher = publisher)

        val newEvents = listOf(
                StudentEnrolled(AN_AGGREGATE_ID, "student-1"),
                StudentUnenrolled(AN_AGGREGATE_ID, "student-2", "some reasons"),
                StudentEnrolled(AN_AGGREGATE_ID, "student-3"))
        sut.saveEvents(AGGREGATE_TYPE, AN_AGGREGATE_ID, newEvents)

        val expectedVersionedEvents = listOf(
                StudentEnrolled(AN_AGGREGATE_ID, "student-1", 0),
                StudentUnenrolled(AN_AGGREGATE_ID, "student-2", "some reasons", 1),
                StudentEnrolled(AN_AGGREGATE_ID, "student-3", 2)
        )
        for(expectedEvent in expectedVersionedEvents)
            verify(publisher).publish(expectedEvent)
        verifyNoMoreInteractions(publisher)
    }
}

private fun givenAnInMemoryEventStore(vararg inits: EventStore.() -> Unit, eventPublisher : EventPublisher<Event> = mock() ) : EventStore {
    val es = InMemoryEventStore(eventPublisher)
    for( init in inits )
        es.init()
    return es
}

private fun EventStore.withSavedEvents(aggregateType: AggregateType, aggregateId: AggregateID, events: Iterable<Event>) {
    saveEvents(aggregateType, aggregateId, events)
}
