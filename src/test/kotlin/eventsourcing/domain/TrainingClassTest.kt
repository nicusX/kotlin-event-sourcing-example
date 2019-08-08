package eventsourcing.domain

import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import eventsourcing.given
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class TrainingClassTest {

    @Test
    fun `given a class with many spots, when I enroll a student, then Student Enrolled event is queued`() {
        val (sut, classId) = given {
            TrainingClass.scheduleNewClass("some-title", LocalDate.now(), 10)
        }

        sut.enrollStudent("student-001")

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder( listOf(StudentEnrolled(classId, "student-001")) )
    }

    @Test
    fun `given a class with no spots, when I enroll a student, I get am exception and no event is queued `() {
        val (sut, _) = given {
            TrainingClass.scheduleNewClass("some-title", LocalDate.now(), 0)
        }

        assertThrows<NoAvailableSpotException> {
            sut.enrollStudent("student-001")
        }
        assertThatAggregateUncommitedChanges(sut)
                .containsNoEvents()
    }

    @Test
    fun `given a class with many spots and an enrolled student, when I enroll the same student again, I get an exception and no event is queued`() {
        val (sut, _) = given {
            TrainingClass
                    .scheduleNewClass("some-title", LocalDate.now(), 10)
                    .enrollStudent("student-001")
        }


        assertThrows<StudentAlreadyEnrolledException> {
            sut.enrollStudent("student-001")
        }
        assertThatAggregateUncommitedChanges(sut)
                .containsNoEvents()
    }

    @Test
    fun `given a class with many spots and an enrolled student, when I unenroll the student, a Student Enrolled event is queued`() {
        val (sut, classId) = given {
            TrainingClass
                    .scheduleNewClass("some-title", LocalDate.now(), 10)
                    .enrollStudent("student-001")
        }

        sut.unenrollStudent("student-001")

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder( listOf(StudentUnenrolled(classId, "student-001")) )
    }

    @Test
    fun `given a class with no enrolled student, when I unenroll a student, I get an exception and no queued event`() {
        val (sut, _) = given {
            TrainingClass
                    .scheduleNewClass("some-title", LocalDate.now(), 10)
        }

        assertThrows<StudentNotEnrolledException> {
            sut.unenrollStudent("student-001")
        }

        assertThatAggregateUncommitedChanges(sut)
                .containsNoEvents()
    }

}
