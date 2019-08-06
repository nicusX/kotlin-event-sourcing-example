package eventsourcing.domain

// FIXME make this a real type
typealias ClassID = String

object TrainingClassAggregate : AggregateType {
    override fun toString() = "CLASS"
}

class TrainingClass(id: ClassID) : AggregateRoot(id) {

    override fun aggregateType() = TrainingClassAggregate

    private var availableSeats = 0
    private var enrolledStudents: Set<StudentID> = mutableSetOf()


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
        availableSeats--
        enrolledStudents.plus( event.studentId )
    }

    private fun apply( event: StudentUnenrolled) {
        availableSeats++
        enrolledStudents.minus( event.studentId )
    }

    private fun apply( event: NewClassScheduled) {
        availableSeats = event.classSize
    }

    // Behaviours
    // 1. check invariants
    // 2. (if successful) apply changes and queue the new event
    // may have side-effects

    fun enrollStudent(studentId: StudentID) {
        if ( enrolledStudents.contains(studentId))
            throw StudentAlreadyEnrolledException(studentId, this.id)

        if ( availableSeats < 1 )
            throw ClassFullException(this.id)

        applyChangeAndQueueEvent(StudentEnrolled(this.id, studentId))
    }

    fun unenrollStudent(studentId: StudentID) {
        if (!enrolledStudents.contains(studentId))
            throw StudentNotEnrolledException(this.id, studentId)

        applyChangeAndQueueEvent(StudentUnenrolled(this.id, studentId))
    }
}

class TrainingClassRepository(eventStore: EventStore) : EventSourcedRepository<TrainingClass>(eventStore) {
    override fun new(id: AggregateID): TrainingClass = TrainingClass(id)
}


class StudentAlreadyEnrolledException(studentId: StudentID, classId: ClassID)
    : Exception("Student $studentId is already enrolled to class $classId")

class StudentNotEnrolledException(studentId: StudentID, classId: ClassID)
    : Exception("Student $studentId is not enrolled to class $classId")

class ClassFullException(classId: ClassID)
    : Exception("Class $classId is full")
