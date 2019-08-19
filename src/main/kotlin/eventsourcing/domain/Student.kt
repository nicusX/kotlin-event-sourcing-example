package eventsourcing.domain

import arrow.core.Left
import arrow.core.Right
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

typealias StudentID = String
typealias EMail = String

class Student(id: StudentID) : AggregateRoot(id) {

    object TYPE : AggregateType {
        override fun toString() = "student"
    }

    override fun aggregateType() = TYPE

    override fun applyEvent(event: Event): Student =
            when (event) {
                is NewStudentRegistered -> apply(event)
                else -> throw UnsupportedEventException(event::class.java)
            }


    private fun apply(event: NewStudentRegistered): Student {
        // TODO notify the Student (i.e. a non-idempotent side effect)
        return this
    }

    companion object {
        fun registerNewStudent(email: EMail, fullname: String, repository: StudentRepository, registeredEmailsIndex: RegisteredEmailsIndex): Result<StudentInvariantViolation, Student> =
                when {
                    // This is an example of an invariant across multiple aggregates.
                    // In a distributed system it should be something more complex than this, to guarantee uniqueness across multiple nodes.
                    registeredEmailsIndex.isEmailAlreadyInUse(email) -> Left(StudentInvariantViolation.EmailAlreadyInUse)
                    else -> {
                        val studentId = UUID.randomUUID().toString()
                        val student = Student(studentId)
                        Right(student.applyAndQueueEvent(NewStudentRegistered(studentId, email, fullname)))
                    }
                }

        val log: Logger = LoggerFactory.getLogger(Student::class.java)
    }
}

sealed class StudentInvariantViolation : Failure {
    object EmailAlreadyInUse : StudentInvariantViolation()
}
