package eventsourcing.domain

import arrow.core.Left
import arrow.core.Right
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

typealias StudentID = String
typealias EMail = String

/**
 * Aggregate root representing a Student
 */
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
        // Nothing special to do here.
        // We are keeping the index of registered emails in an auxiliary read model within the  RegisteredEmailsIndex service
        return this
    }

    companion object {

        // This is the only behaviour of this Aggregate at the moment
        fun registerNewStudent(email: EMail, fullname: String, repository: StudentRepository, registeredEmailsIndex: RegisteredEmailsIndex): Result<StudentInvariantViolation, Student> =
                when {

                    // This is an example of an invariant across multiple aggregates.
                    // In a distributed system it should be something more complex than this, to guarantee uniqueness across multiple nodes.
                    registeredEmailsIndex.isEmailAlreadyInUse(email) -> Left(StudentInvariantViolation.EmailAlreadyInUse)

                    // Success
                    else -> {
                        val studentId = UUID.randomUUID().toString()
                        val student = Student(studentId)
                        // TODO notify the Student (i.e. a non-idempotent side effect)
                        Right(student.applyAndQueueEvent(NewStudentRegistered(studentId, email, fullname)))
                    }
                }

        val log: Logger = LoggerFactory.getLogger(Student::class.java)
    }
}

sealed class StudentInvariantViolation : Failure {
    object EmailAlreadyInUse : StudentInvariantViolation()
}
