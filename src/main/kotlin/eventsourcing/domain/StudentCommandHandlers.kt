package eventsourcing.domain

import arrow.core.flatMap

data class RegisterNewStudentSuccess(val studentID: StudentID) : Success

// TODO May replace chains of map/flatMap with Arrow comprehensions

fun handleRegisterNewStudent(studentRepository: StudentRepository, registeredEmailsIndex: RegisteredEmailsIndex)
        : (RegisterNewStudent) -> Result<Failure, RegisterNewStudentSuccess> = { command: RegisterNewStudent ->
    Student.registerNewStudent(command.email, command.fullName, studentRepository, registeredEmailsIndex)
            .flatMap { student ->
                studentRepository.save(student)
                        .map {RegisterNewStudentSuccess(student.id)  }
            }
}
