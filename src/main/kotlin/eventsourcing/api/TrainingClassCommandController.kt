package eventsourcing.api

import arrow.core.getOrHandle
import com.fasterxml.jackson.annotation.JsonFormat
import eventsourcing.domain.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.validation.Valid

// TODO rewrite using Routing DSL and coRouter
//         eg https://medium.com/@hantsy/using-kotlin-coroutines-with-spring-d2784a300bda
@RestController
class TrainingClassCommandController(private val dispatcher: CommandDispatcher) {

    @PostMapping("/classes/schedule_new")
    fun scheduleNewClass(@Valid @RequestBody req: ScheduleNewClassRequest): ResponseEntity<*> =
            dispatcher.handle(req.toCommand()).map { success: Success ->
                val classId = (success as ScheduleNewClassSuccess).classId
                acceptedResponse(classId)
            }.mapLeft { problem: Problem ->
                when (problem) {
                    is TrainingClassInvariantViolation.InvalidClassSize ->
                        unprocessableEntityResponse("Invalid Class Size")
                    is AggregateNotFound -> notFoundResponse("Aggregate not Found")
                    is EventStoreProblem.ConcurrentChangeDetected -> conflictResponse("Concurrent change detected")
                    else -> serverErrorResponse()
                }
            }.getOrHandle { errorResponse -> errorResponse }


    @PostMapping("/classes/{classId}/enroll_student")
    fun enrollStudent(@PathVariable classId: String, @Valid @RequestBody req: EnrollStudentRequest): ResponseEntity<*> =
            dispatcher.handle(req.toCommandWithClassId(classId)).map { _: Success ->
                acceptedResponse(classId)
            }.mapLeft { problem ->
                when (problem) {
                    is TrainingClassInvariantViolation.StudentAlreadyEnrolled -> unprocessableEntityResponse("Student already enrolled")
                    is TrainingClassInvariantViolation.ClassHasNoAvailableSpots -> unprocessableEntityResponse("No available spots")
                    is AggregateNotFound -> notFoundResponse("Aggregate not Found")
                    is EventStoreProblem.ConcurrentChangeDetected -> conflictResponse("Concurrent change detected")
                    else -> serverErrorResponse()
                }
            }.getOrHandle { errorResponse -> errorResponse }


    @PostMapping("/classes/{classId}/unenroll_student")
    fun unenrollStudent(@PathVariable classId: String, @Valid @RequestBody req: UnenrollStudentRequest): ResponseEntity<*> =
            dispatcher.handle(req.toCommandWithClassId(classId)).map { _: Success ->
                acceptedResponse(classId)
            }.mapLeft { problem ->
                when (problem) {
                    is TrainingClassInvariantViolation.UnenrollingNotEnrolledStudent -> unprocessableEntityResponse("Student not enrolled")
                    is AggregateNotFound -> notFoundResponse()
                    is EventStoreProblem.ConcurrentChangeDetected -> conflictResponse("Concurrent change detected")
                    else -> serverErrorResponse()
                }
            }.getOrHandle { errorResponse -> errorResponse }
}


private fun acceptedResponse(classId: String): ResponseEntity<Any> {
    val headers = HttpHeaders()
    headers.location = TrainingClassReadController.classResourceLocation(classId)
    return ResponseEntity(headers, HttpStatus.ACCEPTED)
}

data class ScheduleNewClassRequest(
        val title: String,
        @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
        val date: LocalDate,
        val size: Int) {
}

data class EnrollStudentRequest(val studentId: String, val classVersion: Long)

data class UnenrollStudentRequest(val studentId: String, val reason: String, val classVersion: Long)


private fun ScheduleNewClassRequest.toCommand(): ScheduleNewClass =
        ScheduleNewClass(this.title, this.date, this.size)

private fun EnrollStudentRequest.toCommandWithClassId(classId: String): EnrollStudent =
        EnrollStudent(classId, studentId, classVersion)

private fun UnenrollStudentRequest.toCommandWithClassId(classId: String): UnenrollStudent =
        UnenrollStudent(classId, studentId, reason, classVersion)
