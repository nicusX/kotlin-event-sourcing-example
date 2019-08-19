package eventsourcing.domain

import arrow.core.None
import arrow.core.Right
import arrow.core.Some
import com.nhaarman.mockitokotlin2.*
import eventsourcing.ResultAssert.Companion.assertThatResult
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TrainingClassCommandHandlersTest {

    @Test
    fun `given a ScheduleNewClass command, when handled, then it succeeds, saves a new Class with a NewClassScheduled uncommited event and return a successful result with the ID of the class`() {
        val repository = mockEmptyRepository()
        val fut = handleScheduleNewClass(repository)

        val command = ScheduleNewClass("class-title", LocalDate.now(), 10)
        val result = fut(command)

        assertThatResult(result)
                .isSuccess()
                .successIsA<ScheduleNewClassSuccess>()

        verify(repository).save(check {
            assertThat(it).isInstanceOf(TrainingClass::class.java)
            assertThatAggregateUncommitedChanges(it).onlyContainsAnEventOfType(NewClassScheduled::class.java)
        }, eq(None))
    }


    @Test
    fun `given an EnrollStudent command, when successfully handled, then it retrieves the class by ID, enroll the student and save it back with the expected version, and return a successful result`() {
        val classId: ClassID = "a-class"
        val clazz = mock<TrainingClass> {
            on { enrollStudent(any()) }.thenAnswer { Right(this.mock) }
        }
        val repository = mockRepositoryContaining(clazz)
        val fut = handleEnrollStudent(repository)

        val command = EnrollStudent(classId, "a-student", 43L)
        val result = fut(command)

        assertThatResult(result).isSuccess()

        verify(clazz).enrollStudent(eq("a-student"))
        verify(repository).getById(eq(classId))
        verify(repository).save(any<TrainingClass>(), eq(Some(43L)))
    }

    @Test
    fun `given an UnenrollStudent command, when handled, it should retrieve the class by ID, unenroll the student and save it back with the expected version, and return a successful result`() {
        val classId: ClassID = "a-class"
        val clazz = mock<TrainingClass> {
            on { unenrollStudent(any(), any()) }.thenAnswer { Right(this.mock) }
        }
        val repository = mockRepositoryContaining(clazz)
        val fut = handleUnenrollStudent(repository)

        val command = UnenrollStudent(classId, "a-student", "some reasons", 43L)
        val result = fut(command)

        assertThatResult(result).isSuccess()

        verify(clazz).unenrollStudent(eq("a-student"), eq("some reasons"))
        verify(repository).getById(eq(classId))
        verify(repository).save(any<TrainingClass>(), eq(Some(43L)))
    }
}

private fun mockRepositoryContaining(clazz: TrainingClass): TrainingClassRepository = mock<TrainingClassRepository> {
    on { getById(any()) }.doReturn(Some(clazz))
    on { save(any(), any()) }.thenReturn(Right(ChangesSuccessfullySaved))
}

private fun mockEmptyRepository(): TrainingClassRepository = mock<TrainingClassRepository> {
    on { getById(any()) }.doReturn(None)
    on { save(any(), any()) }.thenReturn(Right(ChangesSuccessfullySaved))
}
