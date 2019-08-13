package eventsourcing.domain

import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TrainingClassCommandHandlerTest {

    @Test
    fun `when handling a ScheduleNewClass command, it should save a new Class with a NewClassScheduled uncommited event and return a successful result`() {
        val repository = mock<TrainingClassRepository>()
        val sut = TrainingClassCommandHandler(repository)

        val command = ScheduleNewClass("class-title", LocalDate.now(), 10)
        val result = sut.handle(command)

        assertThat(result.classId).isNotBlank()

        verify(repository).save( check {
            assertThat(it).isInstanceOf(TrainingClass::class.java)
            val uncommittedEvents = it.getUncommittedChanges()
            assertThat(uncommittedEvents).hasSize(1)
            assertThat(uncommittedEvents.first()).isInstanceOf( NewClassScheduled::class.java )
        }, isNull())
    }

    @Test
    fun `when handing an EnrollStudent command, it should retrieve the class by ID, enroll the student and save it back with the expected version, and return a successful result`() {
        val classId: ClassID = "a-class"
        val clazz = mock<TrainingClass>()
        val repository = mock<TrainingClassRepository> {
            on { getById(any())}.doReturn( clazz )
        }
        val sut = TrainingClassCommandHandler(repository)

        val command = EnrollStudent(classId, "a-student", 43L)
        val result = sut.handle(command)

        assertThat(result).isNotNull // Not really meaningful as the return value is not nullable

        verify(clazz).enrollStudent(eq("a-student"))
        verify(repository).getById(eq(classId))
        verify(repository).save( any<TrainingClass>(), eq(43L) )
    }

    @Test
    fun `when handling an UnenrollStudent command, it should retrieve the class by ID, unenroll the student and save it back with the expected version, and return a successful result`() {
        val classId: ClassID = "a-class"
        val clazz = mock<TrainingClass>()
        val repository = mock<TrainingClassRepository> {
            on { getById(any())}.doReturn( clazz )
        }
        val sut = TrainingClassCommandHandler(repository)

        val command = UnenrollStudent(classId, "a-student", "some reasons",43L)
        val result = sut.handle(command)

        assertThat(result).isNotNull // Not really meaningful as the return value is not nullable

        verify(clazz).unenrollStudent(eq("a-student"), eq("some reasons"))
        verify(repository).getById(eq(classId))
        verify(repository).save( any<TrainingClass>(), eq(43L) )
    }

}
