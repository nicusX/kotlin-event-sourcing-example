package eventsourcing.domain

import com.nhaarman.mockitokotlin2.*
import eventsourcing.domain.TrainingClass.Companion.scheduleNewClass
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class EventSourcedRepositoryTest {

    @Test
    fun `given a aggregate with uncommitted events, when I save the aggregate, then all uncommitted events are saved in the event store and no uncommited change remains in the aggregate`() {
        val eventStore = mock<EventStore>()
        val sut = eventSourcedRepo(eventStore)

        val clazz = scheduleNewClass("some-title", LocalDate.now(), 10)
                        .enrollStudent("a-student")

        sut.save(clazz)

        assertThat(clazz.getUncommittedChanges()).isEmpty()
        verify(eventStore, times(1)).saveEvents(eq(TrainingClass.TYPE), eq(clazz.id), argForWhich{ count() == 2  }, isNull())
        verifyNoMoreInteractions(eventStore)
    }

    @Test
    fun `given an event store with events from an aggregate, when I get the aggregate by Id, I have the aggregate and all events for the aggregate are retrieved from the event store`(){
        val classId = "class-id"
        val eventStore = mock<EventStore> {
            on { getEventsForAggregate(TrainingClass.TYPE, classId) }
                    .doReturn( listOf(
                            NewClassScheduled(classId,"some-title", LocalDate.now(), 10),
                            StudentEnrolled(classId, "a-student")))
        }
        val sut = eventSourcedRepo(eventStore)

        val clazz = sut.getById(classId)

        assertThat(clazz).isNotNull
        assertThat(clazz.getUncommittedChanges()).isEmpty()
        verify(eventStore).getEventsForAggregate(eq(TrainingClass.TYPE), eq(classId))
        verifyNoMoreInteractions(eventStore)
    }

    @Test
    fun `given an event store, when I get a non-existing aggregate by Id, then an AggregateNotFoundException is thrown`(){
        val classId = "class-id"
        val eventStore = mock<EventStore> {
            on { getEventsForAggregate(TrainingClass.TYPE, classId) }.doReturn( null )
        }
        val sut = eventSourcedRepo(eventStore)

        assertThrows<AggregateNotFoundException> {
            sut.getById(classId)
        }
    }

}

private fun eventSourcedRepo(eventStore: EventStore) : EventSourcedRepository<TrainingClass> = object : EventSourcedRepository<TrainingClass>(eventStore) {
    override fun new(id: AggregateID): TrainingClass = TrainingClass(id)
}