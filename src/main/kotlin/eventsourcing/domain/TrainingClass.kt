package eventsourcing.domain

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
            throw NoAvailableSpotException(this.id)

        applyChangeAndQueueEvent(StudentEnrolled(this.id, studentId))
        return this
    }

    fun unenrollStudent(studentId: StudentID) : TrainingClass {
        if (!this.enrolledStudents.contains(studentId))
            throw StudentNotEnrolledException(this.id, studentId)

        applyChangeAndQueueEvent(StudentUnenrolled(this.id, studentId))
        return this
    }

    companion object {
        fun scheduleNewClass(title: String, date: Date, size: Int) : TrainingClass {
            val classId = UUID.randomUUID().toString()
            val trainingClass = TrainingClass(classId)
            trainingClass.applyChangeAndQueueEvent(NewClassScheduled(classId, title, date, size))
            return trainingClass
        }
    }
}

class TrainingClassRepository(eventStore: EventStore) : EventSourcedRepository<TrainingClass>(eventStore) {
    override fun new(id: AggregateID): TrainingClass = TrainingClass(id)
}


class StudentAlreadyEnrolledException(studentId: StudentID, classId: ClassID)
    : Exception("Student $studentId is already enrolled to class $classId")

class StudentNotEnrolledException(studentId: StudentID, classId: ClassID)
    : Exception("Student $studentId is not enrolled to class $classId")

class NoAvailableSpotException(classId: ClassID)
    : Exception("Class $classId has no available spot")
