package eventsourcing.domain

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*

typealias ClassID = String

class TrainingClass(id: ClassID) : AggregateRoot(id) {

    object TYPE : AggregateType {
        override fun toString() = "class"
    }

    override fun aggregateType() = TYPE

    private var availableSpots = 0
    private var enrolledStudents: MutableSet<StudentID> = mutableSetOf()


    // Apply Event methods
    // change the state of the aggregate
    // never fail
    // no side-effect

    override fun applyEvent(event: Event): TrainingClass =
            when (event) {
                is NewClassScheduled -> apply(event)
                is StudentEnrolled -> apply(event)
                is StudentUnenrolled -> apply(event)
                else -> throw UnsupportedEventException(event::class.java)
            }


    private fun apply(event: StudentEnrolled): TrainingClass {
        this.availableSpots--
        this.enrolledStudents.add(event.studentId)
        return this
    }

    private fun apply(event: StudentUnenrolled): TrainingClass {
        this.availableSpots++
        this.enrolledStudents.remove(event.studentId)
        return this
    }

    private fun apply(event: NewClassScheduled): TrainingClass {
        this.availableSpots = event.classSize
        return this
    }

    // Behaviours
    // 1. check invariants
    // 2. (if successful) apply changes and queue the new event
    // may have side-effects

    fun enrollStudent(studentId: StudentID): Either<TrainingClassInvariantViolation, TrainingClass> {
        log.debug("Enrolling student {} to class {}", studentId, this.id)
        return when {
            this.enrolledStudents.contains(studentId) -> Left(TrainingClassInvariantViolation.StudentAlreadyEnrolled)
            this.availableSpots < 1 -> Left(TrainingClassInvariantViolation.ClassHasNoAvailableSpots)
            else -> Right(applyAndQueueEvent(StudentEnrolled(this.id, studentId)))
        }
    }

    fun unenrollStudent(studentId: StudentID, reason: String): Either<TrainingClassInvariantViolation, TrainingClass> {
        log.debug("Enrolling student {} from class {}. Reason: '{}'", studentId, this.id, reason)
        return when {
            !this.enrolledStudents.contains(studentId) -> Left(TrainingClassInvariantViolation.UnenrollingNotEnrolledStudent)
            else -> Right(applyAndQueueEvent(StudentUnenrolled(this.id, studentId, reason)))
        }
    }

    companion object {
        fun scheduleNewClass(title: String, date: LocalDate, size: Int): Either<TrainingClassInvariantViolation, TrainingClass> =
                when {
                    size <= 0 -> Left(TrainingClassInvariantViolation.InvalidClassSize)
                    else -> {
                        val classId = UUID.randomUUID().toString()
                        Right(TrainingClass(classId)
                                .applyAndQueueEvent(NewClassScheduled(classId, title, date, size)))

                    }
                }

        val log: Logger = LoggerFactory.getLogger(TrainingClass::class.java)
    }
}

sealed class TrainingClassInvariantViolation : Problem {
    object StudentAlreadyEnrolled : TrainingClassInvariantViolation()
    object UnenrollingNotEnrolledStudent : TrainingClassInvariantViolation()
    object ClassHasNoAvailableSpots : TrainingClassInvariantViolation()
    object InvalidClassSize : TrainingClassInvariantViolation()
}
