package eventsourcing.domain

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

    override fun apply( event: Event) {
        when (event) {
            is NewClassScheduled -> apply(event)
            is StudentEnrolled -> apply(event)
            is StudentUnenrolled -> apply(event)
            else -> throw UnsupportedEventException(event::class.java)
        }
    }

    private fun apply( event: StudentEnrolled) {
        this.availableSpots--
        this.enrolledStudents.add(event.studentId)
    }

    private fun apply( event: StudentUnenrolled) {
        this.availableSpots++
        this.enrolledStudents.remove(event.studentId)
    }

    private fun apply( event: NewClassScheduled) {
        this.availableSpots = event.classSize
    }

    // Behaviours
    // 1. check invariants
    // 2. (if successful) apply changes and queue the new event
    // may have side-effects

    fun enrollStudent(studentId: StudentID) : TrainingClass {
        log.debug("Enrolling student {} to class {}", studentId, this.id)
        if ( this.enrolledStudents.contains(studentId))
            throw StudentAlreadyEnrolledException(studentId, this.id)

        if ( this.availableSpots < 1 )
            throw NoAvailableSpotsException(this.id)

        applyChangeAndQueueEvent(StudentEnrolled(this.id, studentId))
        return this
    }

    fun unenrollStudent(studentId: StudentID, reason: String) : TrainingClass {
        log.debug("Enrolling student {} from class {}. Reason: '{}'", studentId, this.id, reason)
        if (!this.enrolledStudents.contains(studentId))
            throw StudentNotEnrolledException(this.id, studentId)

        applyChangeAndQueueEvent(StudentUnenrolled(this.id, studentId, reason))
        return this
    }

    companion object  {
        fun scheduleNewClass (title: String, date: LocalDate, size: Int) : TrainingClass  {
            if ( size <= 0) throw InvalidClassSizeException()

            val classId = UUID.randomUUID().toString()
            val trainingClass = TrainingClass(classId)
            trainingClass.applyChangeAndQueueEvent(NewClassScheduled(classId, title, date, size))
            return trainingClass
        }

        val log : Logger = LoggerFactory.getLogger(TrainingClass::class.java)
    }
}


class StudentAlreadyEnrolledException(studentId: StudentID, classId: ClassID)
    : Exception("Student $studentId is already enrolled to class $classId")

class StudentNotEnrolledException(studentId: StudentID, classId: ClassID)
    : Exception("Student $studentId is not enrolled to class $classId")

class NoAvailableSpotsException(classId: ClassID)
    : Exception("Class $classId has no available spots")

class InvalidClassSizeException()
    : Exception("Class must have size > 0")
