package eventsourcing.domain.commandhandlers

import eventsourcing.domain.*

data class ScheduleNewClassSuccess(val classId: ClassID) : CommandSuccess

fun handleScheduleNewClass(classRepository: TrainingClassRepository) = { command: ScheduleNewClass ->
    val clazz = TrainingClass.scheduleNewClass(command.title, command.date, command.size)
    classRepository.save(clazz)
    ScheduleNewClassSuccess(clazz.id)
}


object EnrollStudentSuccess : CommandSuccess

fun handleEnrollStudent(classRepository: TrainingClassRepository) = { command: EnrollStudent ->
    val clazz = classRepository.getById(command.classId)
    clazz.enrollStudent(command.studentId)
    classRepository.save(clazz, command.originalVersion)
    EnrollStudentSuccess
}


object UnenrollStudentSuccess : CommandSuccess

fun handleUnenrollStudent(classRepository: TrainingClassRepository)  = { command : UnenrollStudent ->
    val clazz = classRepository.getById(command.classId)
    clazz.unenrollStudent(command.studentId, command.reason)
    classRepository.save(clazz, command.originalVersion)
    UnenrollStudentSuccess
}