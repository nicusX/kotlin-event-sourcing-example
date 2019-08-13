package eventsourcing.domain.commandhandlers

import eventsourcing.domain.*

data class RegisterNewStudentSuccess(val studentID: StudentID) : CommandSuccess

fun handleRegisterNewStudent(studentRepository: StudentRepository) = { command: RegisterNewStudent ->
    CommandDispatcher.log.debug("Handling command: {}", command)
    val student = Student.registerNewStudent(command.email, command.fullName, studentRepository)
    studentRepository.save(student)
    RegisterNewStudentSuccess(student.id)
}
