package eventsourcing.domain

import arrow.core.getOrElse
import com.nhaarman.mockitokotlin2.*
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StudentTest {

    @Test
    fun `given a new student registration request, when the email is not in use, then it successfully create a new Student and a NewStudentRegistered event is queued`() {
        val repository  = mock<StudentRepository> {
            on { getByEmail( any()) }.doReturn( null )
        }
        val sut = Student.Companion

        val email = "test@ema.il"
        val result = sut.registerNewStudent(email, "John Doe", repository)

        assertThat(result.isRight()).isTrue()
        val newStudent = result.getOrElse { null }!!
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
        val result = sut.registerNewStudent(email, "John Doe", repository)

        assertThat(result.isLeft())
        assertThat(result.swap().getOrElse { null }).isEqualTo(StudentInvariantViolation.EmailAlreadyInUse)


        verify(repository).getByEmail(eq(email))
    }
}