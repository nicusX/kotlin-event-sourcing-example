package eventsourcing.domain

import arrow.core.Either
import arrow.core.flatMap

data class RegisterNewStudentSuccess(val studentID: StudentID) : Success

// TODO May replace chains of map/flatMap with Arrow comprehensions

fun handleRegisterNewStudent(studentRepository: StudentRepository)
        : (RegisterNewStudent) -> Either<Problem, RegisterNewStudentSuccess> = { command: RegisterNewStudent ->
    Student.registerNewStudent(command.email, command.fullName, studentRepository)
            .flatMap { student ->
                studentRepository.save(student)
                        .map {RegisterNewStudentSuccess(student.id)  }
            }
}
