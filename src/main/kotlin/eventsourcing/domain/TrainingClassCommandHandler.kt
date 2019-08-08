package eventsourcing.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TrainingClassCommandHandler(private val repository: TrainingClassRepository) {

    fun handle(command: ScheduleNewClass) {
        log.debug("Handling command: {}", command)

        // FIXME think about a solution more testable than a static factory
        val clazz = TrainingClass.scheduleNewClass(command.title, command.date, command.size)
        repository.save(clazz)
    }

    fun handle(command: EnrollStudent) {
        log.debug("Handling command: {}", command)

        val clazz = repository.getById(command.classId)
        clazz.enrollStudent(command.studentId)
        repository.save(clazz, command.originalVersion)
    }

    fun handle(command: UnenrollStudent) {
        log.debug("Handling command: {}", command)

        val clazz = repository.getById(command.classId)
        clazz.unenrollStudent(command.studentId)
        repository.save(clazz, command.originalVersion)
    }

    companion object {
         val log : Logger = LoggerFactory.getLogger(TrainingClassCommandHandler::class.java)
    }
}


