package eventsourcing.domain

import arrow.core.Some
import arrow.core.flatMap

fun handleScheduleNewClass(classRepository: TrainingClassRepository)
        : (ScheduleNewClass) -> Result<Failure, ScheduleNewClassSuccess> = { command: ScheduleNewClass ->
    TrainingClass.scheduleNewClass(command.title, command.date, command.size)
            .flatMap { clazz ->
                classRepository.save(clazz)
                        .map { ScheduleNewClassSuccess(clazz.id) }
            }
}

fun handleEnrollStudent(classRepository: TrainingClassRepository)
        : (EnrollStudent) -> Result<Failure, EnrollStudentSuccess> = { command: EnrollStudent ->
    classRepository.getById(command.classId)
            .toEither { AggregateNotFound }
            .flatMap { clazz -> clazz.enrollStudent(command.studentId)}
            .flatMap { clazz ->  classRepository.save(clazz, Some(command.expectedVersion))}
            .map { EnrollStudentSuccess }
}


fun handleUnenrollStudent(classRepository: TrainingClassRepository)
        : (UnenrollStudent) -> Result<Failure, UnenrollStudentSuccess> = { command : UnenrollStudent ->
    classRepository.getById(command.classId)
            .toEither { AggregateNotFound }
            .flatMap { clazz ->  clazz.unenrollStudent(command.studentId, command.reason)}
            .flatMap { clazz ->  classRepository.save(clazz, Some(command.expectedVersion))}
            .map { UnenrollStudentSuccess }
}

data class ScheduleNewClassSuccess(val classId: ClassID) : Success

object EnrollStudentSuccess : Success

object UnenrollStudentSuccess : Success
