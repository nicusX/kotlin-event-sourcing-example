package eventsourcing.domain

import arrow.core.None
import arrow.core.Right
import arrow.core.Some
import arrow.core.getOrElse
import com.nhaarman.mockitokotlin2.*
import eventsourcing.domain.TrainingClass.Companion.scheduleNewClass
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class EventSourcedRepositoryTest {

    @Test
    fun `given a aggregate with uncommitted events, when I save the aggregate, then all uncommitted events are saved in the event store and no uncommited change remains in the aggregate`() {
        val eventStore = mock<EventStore>() {
            on { saveEvents(any(), any(), any(), any())}.thenReturn( Right(emptyList()) )
        }
        val sut = eventSourcedRepo(eventStore)

        val clazz = scheduleNewClass("some-title", LocalDate.now(), 10)
                        .enrollStudent("a-student")

        sut.save(clazz)

        assertThat(clazz.getUncommittedChanges()).isEmpty()
        verify(eventStore, times(1)).saveEvents(eq(TrainingClass.TYPE), eq(clazz.id), argForWhich{ count() == 2  }, eq(None))
        verifyNoMoreInteractions(eventStore)
    }

    @Test
    fun `given an event store with events from an aggregate, when I get the aggregate by Id, I have the aggregate and all events for the aggregate are retrieved from the event store`(){
        val classId = "class-id"
        val eventStore = mock<EventStore> {
            on { getEventsForAggregate(TrainingClass.TYPE, classId) }
                    .doReturn( Some(listOf(
                            NewClassScheduled(classId,"some-title", LocalDate.now(), 10),
                            StudentEnrolled(classId, "a-student"))))
        }
        val sut = eventSourcedRepo(eventStore)

        val clazz = sut.getById(classId)

        assertThat(clazz.isDefined()).isTrue()
        assertThat(clazz.orNull()?.getUncommittedChanges()).isEmpty()
        verify(eventStore).getEventsForAggregate(eq(TrainingClass.TYPE), eq(classId))
        verifyNoMoreInteractions(eventStore)
    }

    @Test
    fun `given an event store, when I get a non-existing aggregate by Id, then I get an empty result`(){
        val classId = "class-id"
        val eventStore = mock<EventStore> {
            on { getEventsForAggregate(TrainingClass.TYPE, classId) }.doReturn( None )
        }
        val sut = eventSourcedRepo(eventStore)

        val clazz = sut.getById(classId)

        assertThat(clazz.isEmpty()).isTrue()
    }

}

private fun eventSourcedRepo(eventStore: EventStore) : EventSourcedRepository<TrainingClass> = object : EventSourcedRepository<TrainingClass>(eventStore) {
    override fun new(id: AggregateID): TrainingClass = TrainingClass(id)
}