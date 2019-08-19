package eventsourcing.domain

import arrow.core.flatMap
import arrow.core.getOrElse
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import eventsourcing.domain.TrainingClass.Companion.scheduleNewClass
import eventsourcing.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class TrainingClassTest {

    // FIXME Rewrite these tests reducing the boilerplate for Either handling

    @Test
    fun `when I schedule a new Training Class with a size greater than 0, then I get a new class with a New Class Scheduled event queued`() {
        val result = scheduleNewClass("Class Title", LocalDate.now(), 10)

        assertThat(result.isRight()).isTrue()
        val newClass = result.getOrElse { null }
        assertThatAggregateUncommitedChanges(newClass!!)
                .onlyContainsAnEventOfType(NewClassScheduled::class.java)
    }

    @Test
    fun `when I schedule a new Training Class with a size less than 1, then I get an Invalid Class Size `(){

        val result = scheduleNewClass("Class Title", LocalDate.now(), -1)

        assertThat(result.isLeft()).isTrue()
        assertThat(result.swap().getOrElse { null }).isEqualTo(TrainingClassInvariantViolation.InvalidClassSize)
    }

    @Test
    fun `given a Training Class with many spots, when I enroll a student, then Student Enrolled event is queued`() {
        val (sut, classId) = given {
            scheduleNewClass("some-title", LocalDate.now(), 10).getOrElse { null }!!
        }

        sut.enrollStudent("student-001")

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder( listOf(StudentEnrolled(classId, "student-001")) )
    }

    @Test
    fun `given a Training Class with no spots, when I enroll a student, I get am exception and no event is queued `() {
        val (sut, _) = given {
            scheduleNewClass("some-title", LocalDate.now(), 1)
                    .flatMap { it.enrollStudent("ANOTHER-STUDENT") }
                    .getOrElse { null }!!
        }

        val result = sut.enrollStudent("student-001")

        assertThat(result.isLeft()).isTrue()
        assertThat(result.swap().getOrElse { null }).isEqualTo(TrainingClassInvariantViolation.ClassHasNoAvailableSpots)


        assertThatAggregateUncommitedChanges(sut)
                .containsNoEvents()
    }

    @Test
    fun `given a Training Class with many spots and an enrolled student, when I enroll the same student again, I get an exception and no event is queued`() {
        val (sut, _) = given {
            scheduleNewClass("some-title", LocalDate.now(), 10)
                    .flatMap{ it.enrollStudent("student-001")}
                    .getOrElse { null }!!
        }

        val result = sut.enrollStudent("student-001")

        assertThat(result.isLeft()).isTrue()
        assertThat(result.swap().getOrElse { null }).isEqualTo(TrainingClassInvariantViolation.StudentAlreadyEnrolled)

        assertThatAggregateUncommitedChanges(sut)
                .containsNoEvents()
    }

    @Test
    fun `given a Training Class with many spots and an enrolled student, when I unenroll the student, a Student Enrolled event is queued`() {
        val (sut, classId) = given {
            scheduleNewClass("some-title", LocalDate.now(), 10)
                    .flatMap{ it.enrollStudent("student-001") }
                    .getOrElse { null }!!
        }

        val result = sut.unenrollStudent("student-001", "some reasons")

        assertThat(result.isRight()).isTrue()

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder( listOf(StudentUnenrolled(classId, "student-001", "some reasons")) )
    }

    @Test
    fun `given a Training Class with no enrolled student, when I unenroll a student, I get an exception and no queued event`() {
        val (sut, _) = given {
            scheduleNewClass("some-title", LocalDate.now(), 10).getOrElse { null }!!
        }

        val result = sut.unenrollStudent("student-001", "some reasons")

        assertThat(result.isLeft()).isTrue()

        assertThat(result.swap().getOrElse { null }).isEqualTo(TrainingClassInvariantViolation.UnenrollingNotEnrolledStudent)
        assertThatAggregateUncommitedChanges(sut)
                .containsNoEvents()
    }

}
