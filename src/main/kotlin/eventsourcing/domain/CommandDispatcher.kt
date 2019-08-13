package eventsourcing.domain

import eventsourcing.domain.commandhandlers.handleEnrollStudent
import eventsourcing.domain.commandhandlers.handleRegisterNewStudent
import eventsourcing.domain.commandhandlers.handleScheduleNewClass
import eventsourcing.domain.commandhandlers.handleUnenrollStudent
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Command handler dispatcher
 *
 * TODO Do we really need a dispatcher?
 */
class CommandDispatcher(private val classRepo: TrainingClassRepository, private val studentRepo : StudentRepository) {

    val scheduleNewClassHandler = handleScheduleNewClass(classRepo)
    val enrollStudentHandler = handleEnrollStudent(classRepo)
    val unenrollStudentHandler = handleUnenrollStudent(classRepo)
    val registerNewStudentHandler = handleRegisterNewStudent(studentRepo)

    // Dispatches
    fun handle(command: Command) : CommandSuccess {
        log.debug("Handing command: {}", command)
        return when(command) {
            is ScheduleNewClass -> scheduleNewClassHandler(command)
            is EnrollStudent -> enrollStudentHandler(command)
            is UnenrollStudent -> unenrollStudentHandler(command)
            is RegisterNewStudent -> registerNewStudentHandler(command)

            else -> throw UnhandledCommandException(command)
        }
    }

    companion object {
        val log : Logger = LoggerFactory.getLogger(CommandDispatcher::class.java)
    }
}

interface CommandSuccess



class UnhandledCommandException(command: Command) : Exception("Command ${command::class.simpleName} is not handled")
