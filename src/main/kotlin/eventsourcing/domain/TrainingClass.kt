package eventsourcing.domain

import java.time.LocalDate
import java.util.*

// FIXME make this a real type
typealias ClassID = String

object TrainingClassAggregateType : AggregateType {
    override fun toString() = "CLASS"
}

class TrainingClass(id: ClassID) : AggregateRoot(id) {

    override fun aggregateType() = TrainingClassAggregateType

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
        if ( this.enrolledStudents.contains(studentId))
            throw StudentAlreadyEnrolledException(studentId, this.id)

        if ( this.availableSpots < 1 )
            throw NoAvailableSpotsException(this.id)

        applyChangeAndQueueEvent(StudentEnrolled(this.id, studentId))
        return this
    }

    // TODO add a "reason"
    fun unenrollStudent(studentId: StudentID) : TrainingClass {
        if (!this.enrolledStudents.contains(studentId))
            throw StudentNotEnrolledException(this.id, studentId)

        applyChangeAndQueueEvent(StudentUnenrolled(this.id, studentId))
        return this
    }

    // FIXME think about a different way for creating the new aggregate. A static factory makes testing harder
    companion object  {
        fun scheduleNewClass(title: String, date: LocalDate, size: Int) : TrainingClass {
            val classId = UUID.randomUUID().toString()
            val trainingClass = TrainingClass(classId)
            trainingClass.applyChangeAndQueueEvent(NewClassScheduled(classId, title, date, size))
            return trainingClass
        }
    }
}


class StudentAlreadyEnrolledException(studentId: StudentID, classId: ClassID)
    : Exception("Student $studentId is already enrolled to class $classId")

class StudentNotEnrolledException(studentId: StudentID, classId: ClassID)
    : Exception("Student $studentId is not enrolled to class $classId")

class NoAvailableSpotsException(classId: ClassID)
    : Exception("Class $classId has no available spots")
