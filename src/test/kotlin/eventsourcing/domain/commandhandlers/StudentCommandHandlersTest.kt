package eventsourcing.domain.commandhandlers

import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import eventsourcing.domain.NewStudentRegistered
import eventsourcing.domain.RegisterNewStudent
import eventsourcing.domain.Student
import eventsourcing.domain.StudentRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StudentCommandHandlersTest {

    @Test
    fun `given RegisterNewStudent command, when handled, it should save a new Student with an uncommited NewStudentRegistered event and return a successful result with the ID of the student`() {
        val repository = mock<StudentRepository>()
        val fut = handleRegisterNewStudent(repository)

        val command = RegisterNewStudent("test@ema.il", "Full Name")
        val result = fut(command)

        assertThat(result.studentID).isNotBlank()

        verify(repository).save(check {
            assertThat(it).isInstanceOf( Student::class.java )
            assertThatAggregateUncommitedChanges(it).onlyContainsAnEventOfType( NewStudentRegistered::class.java )
        }, isNull())

    }
}