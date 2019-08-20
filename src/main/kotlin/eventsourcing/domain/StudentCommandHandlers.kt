package eventsourcing.domain

import arrow.core.flatMap


data class RegisterNewStudentSuccess(val studentID: StudentID) : Success

fun handleRegisterNewStudent(studentRepository: StudentRepository, registeredEmailsIndex: RegisteredEmailsIndex)
        : (RegisterNewStudent) -> Result<Failure, RegisterNewStudentSuccess> = { command: RegisterNewStudent ->
    Student.registerNewStudent(command.email, command.fullName, studentRepository, registeredEmailsIndex)
            .flatMap { student ->
                studentRepository.save(student)
                        .map { RegisterNewStudentSuccess(student.id) }
            }
}
