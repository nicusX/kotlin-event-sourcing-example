package eventsourcing.domain

import arrow.core.None
import arrow.core.Right
import arrow.core.Some
import com.nhaarman.mockitokotlin2.*
import eventsourcing.ResultAssert.Companion.assertThatResult
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StudentCommandHandlersTest {

    @Test
    fun `given a Register New Student Command, when it is handled without any email clashes, then a new Student should be saved and a success containing the Student ID returned`(){
        val command = RegisterNewStudent("test@ema.il", "Full Name")
        val repository = mockEmptyRepository()
        val fut = handleRegisterNewStudent(repository)

        val result = fut(command)

        assertThatResult(result)
                .isSuccess()
                .successIsA<RegisterNewStudentSuccess>()

        verify(repository).getByEmail(eq("test@ema.il"))
        verify(repository).save(check {
            assertThat(it).isInstanceOf(Student::class.java)
            assertThatAggregateUncommitedChanges(it).onlyContainsAnEventOfType(NewStudentRegistered::class.java)
        }, eq(None))
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `given a Register New Student Command, when it is handled with a clash on duplicate email, then it fails with Email Already In Use`() {
        val duplicateEmail = "duplicate@ema.il"
        val command = RegisterNewStudent(duplicateEmail, "Full Name")
        val repository = mockRepositoryContainingEmail(duplicateEmail)
        val fut = handleRegisterNewStudent(repository)

        val result = fut(command)

        assertThatResult(result)
                .isFailure()
                .failureIsA<StudentInvariantViolation.EmailAlreadyInUse>()

        verify(repository).getByEmail(eq(duplicateEmail))
        verifyNoMoreInteractions(repository)
    }
}

private fun mockEmptyRepository(): StudentRepository = mock<StudentRepository> {
    on { getById(any()) }.doReturn(None)
    on { getByEmail(any()) }.doReturn(None)
    on { save(any(), any()) }.thenReturn(Right(ChangesSuccessfullySaved))
}

private fun mockRepositoryContainingEmail(email: EMail): StudentRepository {
    val mock = mockEmptyRepository()
    whenever(mock.getByEmail(eq(email))).thenReturn(Some(Student("A-STUDENT-ID")))
    return mock
}
