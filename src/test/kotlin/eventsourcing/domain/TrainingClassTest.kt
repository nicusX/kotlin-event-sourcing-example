package eventsourcing.domain

import arrow.core.getOrElse
import eventsourcing.EitherAssert.Companion.assertThatEither
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import eventsourcing.domain.TrainingClass.Companion.scheduleNewClass
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TrainingClassTest {

    @Test
    fun `when I schedule a new Training Class with a size greater than 0, then I get a new class with a New Class Scheduled event queued`() {
        val result = scheduleNewClass("Class Title", LocalDate.now(), 10)

        assertThatEither(result).isRight()

        val newClass = result.getOrElse { null }
        assertThatAggregateUncommitedChanges(newClass!!)
                .onlyContainsAnEventOfType(NewClassScheduled::class.java)
    }

    @Test
    fun `when I schedule a new Training Class with a size less than 1, then I get an Invalid Class Size `() {

        val result = scheduleNewClass("Class Title", LocalDate.now(), -1)

        assertThatEither(result)
                .isLeft()
                .leftIsA<TrainingClassInvariantViolation.InvalidClassSize>()
    }

    @Test
    fun `given a Training Class with many spots, when I enroll a student, then Student Enrolled event is queued`() {
        val sut = givenTrainingClassWithSize(10)

        val result = sut.enrollStudent("student-001")

        assertThatEither(result).isRight()

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder(listOf(StudentEnrolled(sut.id, "student-001")))
    }

    @Test
    fun `given a Training Class with no spots, when I enroll a student, I get am exception and no event is queued `() {
        val sut = givenTrainingClassWithSizeAndOneEnrolledStudent(1, "STUDENT001")

        val result = sut.enrollStudent("ANOTHER-STUDENT")

        assertThatEither(result)
                .isLeft()
                .leftIsA<TrainingClassInvariantViolation.ClassHasNoAvailableSpots>()

        assertThatAggregateUncommitedChanges(sut).containsNoEvents()
    }

    @Test
    fun `given a Training Class with many spots and an enrolled student, when I enroll the same student again, I get an exception and no event is queued`() {
        val sut = givenTrainingClassWithSizeAndOneEnrolledStudent(10, "STUDENT001")

        val result = sut.enrollStudent("STUDENT001")

        assertThatEither(result)
                .isLeft()
                .leftIsA<TrainingClassInvariantViolation.StudentAlreadyEnrolled>()

        assertThatAggregateUncommitedChanges(sut).containsNoEvents()
    }

    @Test
    fun `given a Training Class with many spots and an enrolled student, when I unenroll the student, a Student Enrolled event is queued`() {
        val sut = givenTrainingClassWithSizeAndOneEnrolledStudent(10, "STUDENT001")

        val result = sut.unenrollStudent("STUDENT001", "some reasons")

        assertThatEither(result).isRight()

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder(listOf(StudentUnenrolled(sut.id, "STUDENT001", "some reasons")))
    }

    @Test
    fun `given a Training Class with no enrolled student, when I unenroll a student, I get an exception and no queued event`() {
        val sut = givenTrainingClassWithSize(10)

        val result = sut.unenrollStudent("student-001", "some reasons")

        assertThatEither(result)
                .isLeft()
                .leftIsA<TrainingClassInvariantViolation.UnenrollingNotEnrolledStudent>()

        assertThatAggregateUncommitedChanges(sut).containsNoEvents()
    }

}

private fun givenTrainingClassWithSize(size: Int): TrainingClass {
    val clazz = scheduleNewClass("some-title", LocalDate.now(), size).getOrElse { null }!!
    clazz.markChangesAsCommitted()
    return clazz
}

private fun givenTrainingClassWithSizeAndOneEnrolledStudent(size: Int, studentId: StudentID): TrainingClass {
    val clazz = givenTrainingClassWithSize(size)
    clazz.enrollStudent(studentId)
    clazz.markChangesAsCommitted()
    return clazz
}
