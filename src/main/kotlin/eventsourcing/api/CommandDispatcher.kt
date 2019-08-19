package eventsourcing.api

import eventsourcing.domain.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Command handler dispatcher
 * This class is doing nothing more than wiring up all command handlers and their dependencies, to be injected
 * into command controllers
 */
class CommandDispatcher(private val classRepo: TrainingClassRepository, private val studentRepo : StudentRepository, private val registeredEmailsIndex: RegisteredEmailsIndex) {

    val scheduleNewClassHandler: (ScheduleNewClass) -> Result<Failure, ScheduleNewClassSuccess> = handleScheduleNewClass(classRepo)
    val enrollStudentHandler: (EnrollStudent) -> Result<Failure, EnrollStudentSuccess> = handleEnrollStudent(classRepo)
    val unenrollStudentHandler: (UnenrollStudent) -> Result<Failure, UnenrollStudentSuccess> = handleUnenrollStudent(classRepo)
    val registerNewStudentHandler: (RegisterNewStudent) -> Result<Failure, RegisterNewStudentSuccess> = handleRegisterNewStudent(studentRepo, registeredEmailsIndex)

    // Dispatches
    fun handle(command: Command) : Result<Failure, Success> {
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

class UnhandledCommandException(command: Command) : Exception("Command ${command::class.simpleName} is not handled")
