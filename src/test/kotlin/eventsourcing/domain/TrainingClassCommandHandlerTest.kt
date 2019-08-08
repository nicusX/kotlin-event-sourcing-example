package eventsourcing.domain

import com.nhaarman.mockitokotlin2.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TrainingClassCommandHandlerTest {

    @Test
    fun `when handling a ScheduleNewClass command, it should save a new Class with a NewClassScheduled uncommited event`() {
        // FIXME test is different form the following as the new class is created by a static factory (unmockable) within the
        //       command handler
        val repository = mock<TrainingClassRepository>()
        val sut = TrainingClassCommandHandler(repository)

        val command = ScheduleNewClass("class-title", LocalDate.now(), 10)
        sut.handle(command)

        verify(repository).save( check {
            assertThat(it).isInstanceOf(TrainingClass::class.java)
            val uncommittedEvents = it.getUncommittedChanges()
            assertThat(uncommittedEvents).hasSize(1)
            assertThat(uncommittedEvents.first()).isInstanceOf( NewClassScheduled::class.java )
        }, isNull())
    }

    @Test
    fun `when handing an EnrollStudent command, it should retrieve the class by ID, enroll the student and save it back with the expected version`() {
        val classId: ClassID = "a-class"
        val clazz = mock<TrainingClass>()
        val repository = mock<TrainingClassRepository> {
            on { getById(any())}.doReturn( clazz )
        }
        val sut = TrainingClassCommandHandler(repository)

        val command = EnrollStudent(classId, "a-student", 43L)
        sut.handle(command)

        verify(clazz).enrollStudent(eq("a-student"))
        verify(repository).getById(eq(classId))
        verify(repository).save( any<TrainingClass>(), eq(43L) )
    }

    @Test
    fun `when handling an UnenrollStudent command, it should retrieve the class by ID, unenroll the student and save it back with the `() {
        val classId: ClassID = "a-class"
        val clazz = mock<TrainingClass>()
        val repository = mock<TrainingClassRepository> {
            on { getById(any())}.doReturn( clazz )
        }
        val sut = TrainingClassCommandHandler(repository)

        val command = UnenrollStudent(classId, "a-student", 43L)
        sut.handle(command)

        verify(clazz).unenrollStudent(eq("a-student"))
        verify(repository).getById(eq(classId))
        verify(repository).save( any<TrainingClass>(), eq(43L) )
    }

}
