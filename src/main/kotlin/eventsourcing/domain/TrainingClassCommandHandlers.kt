package eventsourcing.domain

import arrow.core.Some
import arrow.core.getOrElse
import eventsourcing.api.AggregateNotFoundException
import eventsourcing.api.CommandSuccess

data class ScheduleNewClassSuccess(val classId: ClassID) : CommandSuccess

// FIXME return Either<Problem,CommandSuccess>
fun handleScheduleNewClass(classRepository: TrainingClassRepository) = { command: ScheduleNewClass ->
    val clazz = TrainingClass.scheduleNewClass(command.title, command.date, command.size)
    classRepository.save(clazz)
    ScheduleNewClassSuccess(clazz.id)
}


object EnrollStudentSuccess : CommandSuccess

// FIXME return Either<Problem,CommandSuccess>
fun handleEnrollStudent(classRepository: TrainingClassRepository) = { command: EnrollStudent ->
    classRepository.getById(command.classId)
            .map { clazz ->
                clazz.enrollStudent(command.studentId)
                classRepository.save(clazz, Some(command.originalVersion))
                EnrollStudentSuccess
            }.getOrElse { throw AggregateNotFoundException() }
}


object UnenrollStudentSuccess : CommandSuccess

// FIXME return Either<Problem,CommandSuccess>
fun handleUnenrollStudent(classRepository: TrainingClassRepository)  = { command : UnenrollStudent ->
    classRepository.getById(command.classId)
            .map { clazz ->
                clazz.unenrollStudent(command.studentId, command.reason)
                classRepository.save(clazz, Some(command.originalVersion))
                UnenrollStudentSuccess
            }.getOrElse { throw AggregateNotFoundException() }
}
