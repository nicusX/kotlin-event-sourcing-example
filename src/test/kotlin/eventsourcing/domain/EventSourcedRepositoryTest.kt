package eventsourcing.domain

import arrow.core.*
import com.nhaarman.mockitokotlin2.*
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import eventsourcing.OptionAssert.Companion.assertThatOption
import eventsourcing.domain.TrainingClass.Companion.scheduleNewClass
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class EventSourcedRepositoryTest {

    @Test
    fun `given an empty event-sourced repo, when I save a new Aggregate, then all uncommitted events are saved to the Event Store and no uncommited events remains in the Aggregate`() {
        val eventStore = givenAnEventStore()
        val sut = eventSourcedRepo(eventStore)

        val clazz = justScheduledTrainingClass().withStudentEnrollment()
        assertThatAggregateUncommitedChanges(clazz).contains(2)

        sut.save(clazz)

        assertThat(clazz.getUncommittedChanges()).isEmpty()
        verify(eventStore, times(1)).saveEvents(eq(TrainingClass.TYPE), eq(clazz.id), argForWhich { count() == 2 }, eq(None))
        verifyNoMoreInteractions(eventStore)
    }

    @Test
    fun `given an event-sourced repo containing an Aggregate, when I get the Aggregate by ID, then all Events of the Aggregate are retrieved from the Event Store and I have an Aggregate with no uncommitted Events`() {
        val classId = "class-id"
        val eventStore = givenAnEventStoreContainingATrainingClass(classId)
        val sut = eventSourcedRepo(eventStore)

        val result = sut.getById(classId)

        val actualClass = assertThatOption(result).isDefined()
                .extract()
        assertThatAggregateUncommitedChanges(actualClass).containsNoEvent()

        verify(eventStore).getEventsForAggregate(eq(TrainingClass.TYPE), eq(classId))
        verifyNoMoreInteractions(eventStore)
    }

    @Test
    fun `given an empty event-sourced repo, when I get an Aggregate by ID, then all Aggregate Events are requested to the Event Store but no Aggregate is returned`(){
        val classId = "class-id"
        val eventStore = givenAnEventStore()
        val sut = eventSourcedRepo(eventStore)

        val clazz = sut.getById(classId)

        assertThatOption(clazz).isEmpty()

        verify(eventStore).getEventsForAggregate(eq(TrainingClass.TYPE), eq(classId))
        verifyNoMoreInteractions(eventStore)
    }

}

private fun givenAnEventStore(): EventStore = mock<EventStore>() {
    on { saveEvents(any(), any(), any(), any()) }.thenReturn(Right(emptyList()))
    on { getEventsForAggregate(any(), any()) }.doReturn(None)
}

private fun givenAnEventStoreContainingATrainingClass(classId: ClassID): EventStore {
    val es = givenAnEventStore()
    whenever(es.getEventsForAggregate(eq(TrainingClass.TYPE), eq(classId)))
            .thenReturn(Some(listOf(
                    NewClassScheduled(classId, "some-title", LocalDate.now(), 10),
                    StudentEnrolled(classId, "a-student"))))
    return es
}

private fun justScheduledTrainingClass(): TrainingClass =
     scheduleNewClass("some-title", LocalDate.now(), 10).getOrElse { null }!!

private fun TrainingClass.withStudentEnrollment(): TrainingClass =
        this.enrollStudent("A-STUDENT").getOrElse { null }!!

private fun eventSourcedRepo(eventStore: EventStore): EventSourcedRepository<TrainingClass> = object : EventSourcedRepository<TrainingClass>(eventStore) {
    override fun new(id: AggregateID): TrainingClass = TrainingClass(id)
}