package eventsourcing.domain

import arrow.core.getOrElse
import eventsourcing.ResultAssert.Companion.assertThatResult
import eventsourcing.EventsAssert.Companion.assertThatAggregateUncommitedChanges
import eventsourcing.domain.TrainingClass.Companion.scheduleNewClass
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TrainingClassTest {

    @Test
    fun `given a new Training Class scheduling request, when size greater than 0, then it succeeds returning te new class with a NewClassScheduled event queued`() {
        val fut = TrainingClass.Companion::scheduleNewClass

        val result = fut("Class Title", LocalDate.now(), 10)

        val newClass = assertThatResult(result)
                .isSuccess()
                .extractSuccess()
        assertThatAggregateUncommitedChanges(newClass)
                .onlyContainsAnEventOfType(NewClassScheduled::class.java)
    }

    @Test
    fun `given a new Training Class scheduling request, when size is less than 1, then it fails with an InvalidClassSize `() {
        val fut = TrainingClass.Companion::scheduleNewClass

        val result = fut("Class Title", LocalDate.now(), -1)

        assertThatResult(result)
                .isFailure()
                .failureIsA<TrainingClassInvariantViolation.InvalidClassSize>()
    }

    @Test
    fun `given a Training Class with size 10 and no enrolled Students, when I enroll a Student, then it succeeds and a StudentEnrolled event is queued`() {
        val sut = givenTrainingClassWithSize(10)

        val result = sut.enrollStudent("student-001")

        assertThatResult(result).isSuccess()

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder(listOf(StudentEnrolled(sut.id, "student-001")))
    }

    @Test
    fun `given a Training Class with size of 1 and 1 enrolled Student, when I enroll a new Student, then it fails with ClassHasNoAvailableSpots and no event is queued `() {
        val sut = givenTrainingClassWithSizeAndOneEnrolledStudent(1, "STUDENT001")

        val result = sut.enrollStudent("ANOTHER-STUDENT")

        assertThatResult(result)
                .isFailure()
                .failureIsA<TrainingClassInvariantViolation.ClassHasNoAvailableSpots>()

        assertThatAggregateUncommitedChanges(sut).containsNoEvents()
    }

    @Test
    fun `given a Training Class with size 10 and 1 enrolled Student, when I enroll the same Student, then it fails with a StudentAlreadyEnrolled and no event is queued`() {
        val sut = givenTrainingClassWithSizeAndOneEnrolledStudent(10, "STUDENT001")

        val result = sut.enrollStudent("STUDENT001")

        assertThatResult(result)
                .isFailure()
                .failureIsA<TrainingClassInvariantViolation.StudentAlreadyEnrolled>()

        assertThatAggregateUncommitedChanges(sut).containsNoEvents()
    }

    @Test
    fun `given a Training Class with many spots and an enrolled student, when I unenroll the student, then it succeeds and a StudentEnrolled event is queued`() {
        val sut = givenTrainingClassWithSizeAndOneEnrolledStudent(10, "STUDENT001")

        val result = sut.unenrollStudent("STUDENT001", "some reasons")

        assertThatResult(result).isSuccess()

        assertThatAggregateUncommitedChanges(sut)
                .onlyContainsInOrder(listOf(StudentUnenrolled(sut.id, "STUDENT001", "some reasons")))
    }

    @Test
    fun `given a Training Class with no enrolled student, when I unenroll a Student, then it fails with UnenrollingNotEnrolledStudent and no queued event`() {
        val sut = givenTrainingClassWithSize(10)

        val result = sut.unenrollStudent("student-001", "some reasons")

        assertThatResult(result)
                .isFailure()
                .failureIsA<TrainingClassInvariantViolation.UnenrollingNotEnrolledStudent>()

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
