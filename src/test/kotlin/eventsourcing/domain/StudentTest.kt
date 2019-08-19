package eventsourcing.domain

import arrow.core.None
import arrow.core.Right
import arrow.core.Some
import com.nhaarman.mockitokotlin2.*
import eventsourcing.EitherAssert.Companion.assertThatEither
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import org.junit.jupiter.api.Test

internal class StudentTest {

    @Test
    fun `given a new student registration request, when the email is not in use, then it successfully create a new Student and a NewStudentRegistered event is queued`() {
        val repository = mockEmptyRepository()
        val sut = Student.Companion

        val email = "test@ema.il"
        val result = sut.registerNewStudent(email, "John Doe", repository)

        val newStudent = assertThatEither(result).isRight()
                .extractRight()

        assertThatAggregateUncommitedChanges(newStudent)
                .onlyContainsAnEventOfType(NewStudentRegistered::class.java)

        verify(repository).getByEmail(eq(email))
    }

    @Test
    fun `given a new student registration request, when email is already in use, then it throws DuplicateEmailException`() {
        val email = "test@ema.il"
        val repository = mockRepositoryContainingStudent("student-id", email)
        val sut = Student.Companion

        val result = sut.registerNewStudent(email, "John Doe", repository)

        assertThatEither(result)
                .isLeft()
                .leftIsA<StudentInvariantViolation.EmailAlreadyInUse>()

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
