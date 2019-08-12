package eventsourcing.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TrainingClassCommandHandler(private val repository: TrainingClassRepository) {

    // Command handlers return a result on success and throw exceptions on failure

    fun handle(command: ScheduleNewClass) : ScheduleNewClassSuccess {
        log.debug("Handling command: {}", command)

        // FIXME think about a solution more testable than a static factory
        val clazz = TrainingClass.scheduleNewClass(command.title, command.date, command.size)
        repository.save(clazz)

        return ScheduleNewClassSuccess(clazz.id)
    }

    fun handle(command: EnrollStudent) : EnrollStudentSuccess {
        log.debug("Handling command: {}", command)

        val clazz = repository.getById(command.classId)
        clazz.enrollStudent(command.studentId)
        repository.save(clazz, command.originalVersion)

        return EnrollStudentSuccess
    }

    fun handle(command: UnenrollStudent) : UnenrollStudentSuccess {
        log.debug("Handling command: {}", command)

        val clazz = repository.getById(command.classId)
        clazz.unenrollStudent(command.studentId)
        repository.save(clazz, command.originalVersion)

        return UnenrollStudentSuccess
    }

    companion object {
         val log : Logger = LoggerFactory.getLogger(TrainingClassCommandHandler::class.java)
    }
}

data class ScheduleNewClassSuccess(val classId: ClassID)

object EnrollStudentSuccess

object UnenrollStudentSuccess

