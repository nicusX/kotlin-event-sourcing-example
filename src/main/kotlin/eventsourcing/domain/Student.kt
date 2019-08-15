package eventsourcing.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.*

typealias StudentID = String
typealias EMail = String

class Student(id: StudentID) : AggregateRoot(id) {

    object TYPE : AggregateType {
        override fun toString() = "student"
    }

    override fun aggregateType() = TYPE

    override fun apply( event: Event) {
        when (event) {
            is NewStudentRegistered -> apply(event)
            else -> throw UnsupportedEventException(event::class.java)
        }
    }

    private fun apply(event: NewStudentRegistered) {
        // TODO notify the Student (i.e. a non-idempotent side effect)
    }

    companion object {
        fun registerNewStudent(email: EMail, fullname: String, repository: StudentRepository) : Student {

            if (repository.emailAlreadyInUse(email)) throw DuplicateEmailException(email)

            val studentId = UUID.randomUUID().toString()
            val student = Student(studentId)

            student.applyChangeAndQueueEvent( NewStudentRegistered(studentId, email, fullname ))
            return student
        }

        // FIXME use a Service querying a specialised Read Model containing emails only
        private fun StudentRepository.emailAlreadyInUse(email: EMail) : Boolean =
                this.getByEmail(email).isPresent

        val log : Logger = LoggerFactory.getLogger(Student::class.java)
    }
}

class DuplicateEmailException(email: EMail) : Exception("Another Student with '$email' already exists")
