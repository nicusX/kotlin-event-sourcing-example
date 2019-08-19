package eventsourcing.api

import arrow.core.getOrHandle
import eventsourcing.domain.*
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

// TODO rewrite using Routing DSL and coRouter
@RestController
class StudentCommandController(private val dispatcher: CommandDispatcher) {
    @PostMapping("/students/register")
    fun registerNewStudent(@Valid @RequestBody req: RegisterNewStudentRequest): ResponseEntity<*> =
            dispatcher.handle(req.toCommand()).map { success ->
                val studentId = (success as RegisterNewStudentSuccess).studentID
                acceptedResponse(studentId)
            }.mapLeft { failure ->
                when (failure) {
                    is StudentInvariantViolation.EmailAlreadyInUse -> unprocessableEntityResponse("Duplicate email")
                    is AggregateNotFound -> notFoundResponse("Aggregate not Found")
                    is EventStoreFailure.ConcurrentChangeDetected -> conflictResponse("Concurrent change detected")
                    else -> serverErrorResponse()
                }
            }.getOrHandle { errorResponse -> errorResponse }
}

data class RegisterNewStudentRequest(
        val email: String,
        val fullName: String) {
    fun toCommand() = RegisterNewStudent(email, fullName)
}

private fun acceptedResponse(studentId: String): ResponseEntity<Any> {
    val headers = HttpHeaders()
    headers.location = StudentReadController.studentResourceLocation(studentId)
    return ResponseEntity(headers, HttpStatus.ACCEPTED)
}
