package eventsourcing.domain

import arrow.core.None
import arrow.core.Right
import arrow.core.Some
import com.nhaarman.mockitokotlin2.*
import eventsourcing.ResultAssert.Companion.assertThatResult
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import org.junit.jupiter.api.Test

internal class StudentTest {

    @Test
    fun `given a new student registration request, when the new Student email is not already in use, then it succeeds and creates a new Student and a NewStudentRegistered event is queued`() {
        val repository = mockEmptyRepository()
        val fut = Student.Companion::registerNewStudent

        val email = "test@ema.il"
        val result = fut(email, "John Doe", repository)

        val newStudent = assertThatResult(result).isSuccess()
                .extractSuccess()

        assertThatAggregateUncommitedChanges(newStudent)
                .onlyContainsAnEventOfType(NewStudentRegistered::class.java)

        verify(repository).getByEmail(eq(email))
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `given a new student registration request, when email is already in use, then it fails with EmailAlreadyInUse`() {
        val email = "test@ema.il"
        val repository = mockRepositoryContainingStudent("student-id", email)
        val fut = Student.Companion::registerNewStudent

        val result = fut(email, "John Doe", repository)

        assertThatResult(result)
                .isFailure()
                .failureIsA<StudentInvariantViolation.EmailAlreadyInUse>()

        verify(repository).getByEmail(eq(email))
    }
}

private fun mockEmptyRepository(): StudentRepository = mock<StudentRepository> {
    on { getByEmail(any()) }.doReturn(None)
    on { getById(any()) }.doReturn(None)
    on { save(any(), any()) }.thenReturn(Right(ChangesSuccessfullySaved))
}

private fun mockRepositoryContainingStudent(studentId: StudentID, studentEmail: EMail) = mock<StudentRepository> {
    on { getByEmail(eq(studentEmail)) }.doReturn(Some(Student(studentId)))
    on { getById(eq(studentId)) }.doReturn(Some(Student(studentId)))
    on { save(any(), any()) }.thenReturn(Right(ChangesSuccessfullySaved))
}
