package eventsourcing.api

import eventsourcing.domain.RegisterNewStudent
import eventsourcing.domain.RegisterNewStudentSuccess
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
    fun scheduleNewClass(@Valid @RequestBody req: RegisterNewStudentRequest): ResponseEntity<Any>
            = acceptedResponse( (dispatcher.handle(req.toCommand()) as RegisterNewStudentSuccess).studentID )

}

data class RegisterNewStudentRequest(
        val email: String,
        val fullName: String) {
    fun toCommand() = RegisterNewStudent(email, fullName)
}

private fun acceptedResponse(studentId: String) : ResponseEntity<Any> {
    val headers = HttpHeaders()
    headers.location = StudentReadController.studentResourceLocation(studentId)
    return ResponseEntity(headers, HttpStatus.ACCEPTED)
}