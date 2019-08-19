package eventsourcing.domain

import arrow.core.*

// TODO May replace chains of map/flatMap with Arrow comprehensions

fun handleScheduleNewClass(classRepository: TrainingClassRepository)
        : (ScheduleNewClass) -> Either<Problem, ScheduleNewClassSuccess> = { command: ScheduleNewClass ->
    TrainingClass.scheduleNewClass(command.title, command.date, command.size)
            .flatMap { clazz ->
                classRepository.save(clazz)
                        .map { ScheduleNewClassSuccess(clazz.id)}
            }
}

fun handleEnrollStudent(classRepository: TrainingClassRepository)
        : (EnrollStudent) -> Either<Problem, EnrollStudentSuccess> = { command: EnrollStudent ->
    classRepository.getById(command.classId)
            .toEither { AggregateNotFound }
            .flatMap { clazz -> clazz.enrollStudent(command.studentId)}
            .flatMap { clazz ->  classRepository.save(clazz, Some(command.originalVersion))}
            .map { EnrollStudentSuccess }
}


fun handleUnenrollStudent(classRepository: TrainingClassRepository)
        : (UnenrollStudent) -> Either<Problem, UnenrollStudentSuccess> = { command : UnenrollStudent ->
    classRepository.getById(command.classId)
            .toEither { AggregateNotFound }
            .flatMap { clazz ->  clazz.unenrollStudent(command.studentId, command.reason)}
            .flatMap { clazz ->  classRepository.save(clazz, Some(command.originalVersion))}
            .map { UnenrollStudentSuccess }
}

data class ScheduleNewClassSuccess(val classId: ClassID) : Success

object EnrollStudentSuccess : Success

object UnenrollStudentSuccess : Success
