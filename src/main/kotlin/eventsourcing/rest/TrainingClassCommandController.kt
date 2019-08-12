package eventsourcing.rest

import com.fasterxml.jackson.annotation.JsonFormat
import eventsourcing.domain.*
import eventsourcing.readmodel.RecordNotFound
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.validation.Valid

@RestController
class TrainingClassCommandController(private val handler: TrainingClassCommandHandler) {

    @PostMapping("/classes/schedule_new")
    fun scheduleNewClass(@Valid @RequestBody req: ScheduleNewClassRequest): ResponseEntity<Any>
        = acceptedResponse( handler.handle(req.toCommand()).classId )


    @PostMapping("/classes/{classId}/enroll_student")
    fun enrollStudent(@PathVariable classId: String, @Valid @RequestBody req: EnrollStudentRequest): ResponseEntity<Any> =
            try {
                handler.handle(req.toCommandWithClassId(classId))
                acceptedResponse( classId )
            } catch (nf: RecordNotFound) {
                ResponseEntity.notFound().build()
            } catch (soe: StudentAlreadyEnrolledException) {
                ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResource("Student already enrolled"))
            } catch (nas: NoAvailableSpotsException) {
                ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResource("No available spots"))
            }


    @PostMapping("/classes/{classId}/unenroll_student")
    fun unenrollStudent(@PathVariable classId: String, @Valid @RequestBody req: UnenrollStudentRequest) : ResponseEntity<Any> =
            try {
                handler.handle(req.toCommandWithClassId(classId))
                acceptedResponse( classId )
            } catch (nf: RecordNotFound) {
                ResponseEntity.notFound().build()
            } catch (sne: StudentNotEnrolledException) {
                ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResource("Student not enrolled"))
            }
}

private fun acceptedResponse(classId: String) : ResponseEntity<Any> {
    val headers = HttpHeaders()
    headers.location = TrainingClassReadController.classResourceLocation(classId)
    return ResponseEntity(headers, HttpStatus.ACCEPTED)
}


data class ScheduleNewClassRequest(
        val title: String,
        @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
        val date: LocalDate,
        val size: Int) {

    fun toCommand(): ScheduleNewClass = ScheduleNewClass(this.title, this.date, this.size)
}


data class EnrollStudentRequest(val studentId: String, val classVersion: Long) {
    fun toCommandWithClassId(classId: String): EnrollStudent = EnrollStudent(classId, studentId, classVersion)
}

data class UnenrollStudentRequest(val studentId: String, val classVersion: Long) {
    fun toCommandWithClassId(classId: String): UnenrollStudent = UnenrollStudent(classId, studentId, classVersion)
}