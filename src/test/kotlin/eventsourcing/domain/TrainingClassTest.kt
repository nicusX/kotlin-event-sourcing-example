package eventsourcing.domain

import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import eventsourcing.domain.TrainingClass.Companion.scheduleNewClass
import eventsourcing.given
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class TrainingClassTest {


    @Test
    fun `when I schedule a new Training Class with a size greater than 0, then I get a new class with a New Class Scheduled event queued`() {
        val new = scheduleNewClass("Class Title", LocalDate.now(), 10)

        assertThatAggregateUncommitedChanges(new)
                .onlyContainsAnEventOfType(NewClassScheduled::class.java)
    }

    @Test
    fun `when I schedule a new Training Class with a size less than 1, then I get an Invalid Class Size exception`(){

        assertThrows<InvalidClassSizeException> {
            scheduleNewClass("Class Title", LocalDate.now(), -1)
        }
    }

    @Test
    fun `given a Training Class with many spots, when I enroll a student, then Student Enrolled event is queued`() {
        val (sut, classId) = given {
            scheduleNewClass("some-title", LocalDate.now(), 10)
        }

        sut.enrollStudent("student-001")

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder( listOf(StudentEnrolled(classId, "student-001")) )
    }

    @Test
    fun `given a Training Class with no spots, when I enroll a student, I get am exception and no event is queued `() {
        val (sut, _) = given {
            scheduleNewClass("some-title", LocalDate.now(), 1)
                    .enrollStudent("ANOTHER-STUDENT")
        }

        assertThrows<NoAvailableSpotsException> {
            sut.enrollStudent("student-001")
        }
        assertThatAggregateUncommitedChanges(sut)
                .containsNoEvents()
    }

    @Test
    fun `given a Training Class with many spots and an enrolled student, when I enroll the same student again, I get an exception and no event is queued`() {
        val (sut, _) = given {
            scheduleNewClass("some-title", LocalDate.now(), 10)
                    .enrollStudent("student-001")
        }


        assertThrows<StudentAlreadyEnrolledException> {
            sut.enrollStudent("student-001")
        }
        assertThatAggregateUncommitedChanges(sut)
                .containsNoEvents()
    }

    @Test
    fun `given a Training Class with many spots and an enrolled student, when I unenroll the student, a Student Enrolled event is queued`() {
        val (sut, classId) = given {
            scheduleNewClass("some-title", LocalDate.now(), 10)
                    .enrollStudent("student-001")
        }

        sut.unenrollStudent("student-001", "some reasons")

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder( listOf(StudentUnenrolled(classId, "student-001", "some reasons")) )
    }

    @Test
    fun `given a Training Class with no enrolled student, when I unenroll a student, I get an exception and no queued event`() {
        val (sut, _) = given {
            scheduleNewClass("some-title", LocalDate.now(), 10)
        }

        assertThrows<StudentNotEnrolledException> {
            sut.unenrollStudent("student-001", "some reasons")
        }

        assertThatAggregateUncommitedChanges(sut)
                .containsNoEvents()
    }

}
