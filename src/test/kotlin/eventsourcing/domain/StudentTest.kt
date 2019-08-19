package eventsourcing.domain

import arrow.core.None
import arrow.core.Right
import com.nhaarman.mockitokotlin2.*
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import eventsourcing.ResultAssert.Companion.assertThatResult
import org.junit.jupiter.api.Test

internal class StudentTest {

    @Test
    fun `given a new student registration request, when the new Student email is not already in use, then it succeeds and creates a new Student and a NewStudentRegistered event is queued`() {
        val repository = mockRepository()
        val emailsIndex = mockEmptyRegisteredEmailIndex()
        val fut = Student.Companion::registerNewStudent

        val email = "test@ema.il"
        val result = fut(email, "John Doe", repository, emailsIndex)

        val newStudent = assertThatResult(result).isSuccess()
                .extractSuccess()

        assertThatAggregateUncommitedChanges(newStudent)
                .onlyContainsAnEventOfType(NewStudentRegistered::class.java)

        verify(emailsIndex).isEmailAlreadyInUse(eq(email))
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `given a new student registration request, when email is already in use, then it fails with EmailAlreadyInUse`() {
        val email = "test@ema.il"
        val repository = mockRepository()
        val emailsIndex = mockRegisteredEmailIndexContainingEmails()
        val fut = Student.Companion::registerNewStudent

        val result = fut(email, "John Doe", repository, emailsIndex)

        assertThatResult(result)
                .isFailure()
                .failureIsA<StudentInvariantViolation.EmailAlreadyInUse>()

        verify(emailsIndex).isEmailAlreadyInUse(eq(email))
    }
}

private fun mockRepository(): StudentRepository = mock<StudentRepository> {
    on { getById(any()) }.doReturn(None)
    on { save(any(), any()) }.thenReturn(Right(ChangesSuccessfullySaved))
}

private fun mockEmptyRegisteredEmailIndex(): RegisteredEmailsIndex = mock<RegisteredEmailsIndex> {
    on { isEmailAlreadyInUse(any()) }.thenReturn(false)
}

private fun mockRegisteredEmailIndexContainingEmails(): RegisteredEmailsIndex = mock<RegisteredEmailsIndex> {
    on { isEmailAlreadyInUse(any()) }.thenReturn(true)
}
