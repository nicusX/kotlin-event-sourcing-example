package eventsourcing.domain

import com.nhaarman.mockitokotlin2.*
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class StudentTest {

    @Test
    fun `given a new student registration request, when the email is not in use, then it successfully create a new Student and a NewStudentRegistered event is queued`() {
        val repository  = mock<StudentRepository> {
            on { getByEmail( any()) }.doReturn( null )
        }
        val sut = Student.Companion

        val email = "test@ema.il"
        val newStudent = sut.registerNewStudent(email, "John Doe", repository)

        assertThatAggregateUncommitedChanges(newStudent)
                .onlyContainsAnEventOfType(NewStudentRegistered::class.java)

        verify(repository).getByEmail(eq(email))
    }

    @Test
    fun `given a new student registration request, when email is already in use, then it throws DuplicateEmailException`() {
        val repository  = mock<StudentRepository> {
            on { getByEmail( any()) }.doReturn( Student("an-id") )
        }

        val sut = Student.Companion

        val email = "test@ema.il"

        assertThrows<DuplicateEmailException> {
            sut.registerNewStudent(email, "John Doe", repository)
        }

        verify(repository).getByEmail(eq(email))
    }
}