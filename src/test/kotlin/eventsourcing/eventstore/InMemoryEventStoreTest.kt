package eventsourcing.eventstore

import eventsourcing.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InMemoryEventStoreTest {

    val AGGREGATE_ID : ClassID = "aggr012"
    val AGGREGATE_TYPE : AggregateType = TrainingClassAggregate
    val SOME_EVENTS : List<Event> = listOf(
            StudentEnrolled(AGGREGATE_ID, "student-1"),
            StudentUnenrolled(AGGREGATE_ID, "student-2"),
            StudentEnrolled(AGGREGATE_ID, "student-3")
    )

    val MORE_EVENTS : List<Event> = listOf(
            StudentEnrolled(AGGREGATE_ID, "student-4"),
            StudentEnrolled(AGGREGATE_ID, "student-5")
    )

    val ANOTHER_AGGREGATE_ID : ClassID = "aggr045"
    val EVENTS_OF_ANOTHER_AGGREGATE : List<Event> = listOf(
            StudentEnrolled(ANOTHER_AGGREGATE_ID, "student-X"),
            StudentEnrolled(ANOTHER_AGGREGATE_ID, "student-Y")
    )

    @Test
    fun `should store events, with no expected versions, and retrieve them back`() {
        val sut : EventStore = InMemoryEventStore()
        sut.saveEvents(AGGREGATE_TYPE, AGGREGATE_ID, SOME_EVENTS)

        val extractedEvents = sut.getEventsForAggregate(AGGREGATE_TYPE, AGGREGATE_ID)

        assertThat(extractedEvents).hasSize(3)
        matchesAllEvents(extractedEvents,SOME_EVENTS)
    }

    @Test
    fun `should throw exception if the expected version does not match actual version`() {
        val sut : EventStore = InMemoryEventStore()
        sut.saveEvents(AGGREGATE_TYPE, AGGREGATE_ID, SOME_EVENTS)

        assertThrows<ConcurrencyException> {
            sut.saveEvents(AGGREGATE_TYPE, AGGREGATE_ID, MORE_EVENTS, 42)
        }
    }

    @Test
    fun `should retrieve only events of the specified aggregate`() {
        val sut : EventStore = InMemoryEventStore()
        sut.saveEvents(AGGREGATE_TYPE, AGGREGATE_ID, SOME_EVENTS)
        sut.saveEvents(AGGREGATE_TYPE, ANOTHER_AGGREGATE_ID, EVENTS_OF_ANOTHER_AGGREGATE)

        val extractedEvents = sut.getEventsForAggregate(AGGREGATE_TYPE, AGGREGATE_ID)

        assertThat(extractedEvents).hasSize(3)
        matchesAllEvents(extractedEvents,SOME_EVENTS)
    }


    private fun matchesAllEvents(actual: Iterable<Event>, expected: List<Event>) {
        for( (i,actualEvent) in actual.withIndex()) {
            assertThat(actualEvent).isEqualTo(expected[i])
        }
    }
}