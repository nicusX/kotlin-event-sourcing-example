package eventsourcing.domain

import eventsourcing.api.CommandDispatcher
import eventsourcing.api.CommandSuccess

data class RegisterNewStudentSuccess(val studentID: StudentID) : CommandSuccess

// FIXME return Either<Problem,CommandSuccess>
fun handleRegisterNewStudent(studentRepository: StudentRepository) = { command: RegisterNewStudent ->
    CommandDispatcher.log.debug("Handling command: {}", command)
    val student = Student.registerNewStudent(command.email, command.fullName, studentRepository)
    studentRepository.save(student)
    RegisterNewStudentSuccess(student.id)
}
