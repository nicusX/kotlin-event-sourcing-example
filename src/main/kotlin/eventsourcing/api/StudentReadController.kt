package eventsourcing.api

import eventsourcing.readmodels.RecordNotFound
import eventsourcing.readmodels.StudentDTO
import eventsourcing.readmodels.StudentView
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.net.URI
import kotlin.reflect.jvm.javaMethod

// TODO rewrite using Routing DSL and coRouter
@RestController
class StudentReadController(private val view: StudentView) {

    // TODO Return a simplified DTO in the list
    @GetMapping("/students")
    fun listTrainingClasses(): ResponseEntity<List<StudentDTO>> = ResponseEntity.ok(view.listStudents())

    @GetMapping("/students/{studentId}")
    fun getStudent(@PathVariable studentId: String): ResponseEntity<StudentDTO> = try {
        ResponseEntity.ok(view.getStudentById(studentId))
    } catch (e: RecordNotFound) {
        ResponseEntity.notFound().build()
    }


    companion object {
        fun studentResourceLocation(studentId: String): URI =
                MvcUriComponentsBuilder.fromMethod(
                        StudentReadController::class.java,
                        StudentReadController::getStudent.javaMethod!!,
                        studentId)
                        .build(studentId)
    }
}
