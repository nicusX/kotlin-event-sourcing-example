package eventsourcing.api

import eventsourcing.readmodels.studentlist.Student
import eventsourcing.readmodels.studentdetails.StudentDetails
import eventsourcing.readmodels.studentdetails.StudentDetailsReadModel
import eventsourcing.readmodels.studentlist.StudentListReadModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.net.URI
import kotlin.reflect.jvm.javaMethod

// TODO rewrite using Routing DSL and coRouter
@RestController
class StudentReadController(private val studentDetails: StudentDetailsReadModel, private val studentList: StudentListReadModel) {

    @GetMapping("/students")
    fun listTrainingClasses(): ResponseEntity<Iterable<Student>> =
            ResponseEntity.ok(studentList.allStudents())

    @GetMapping("/students/{studentId}")
    fun getStudent(@PathVariable studentId: String): ResponseEntity<StudentDetails> =
            studentDetails.getStudentById(studentId)?.toResponse() ?: ResponseEntity.notFound().build()

    companion object {
        fun studentResourceLocation(studentId: String): URI =
                MvcUriComponentsBuilder.fromMethod(
                        StudentReadController::class.java,
                        StudentReadController::getStudent.javaMethod!!,
                        studentId)
                        .build(studentId)
    }
}

private fun StudentDetails.toResponse() : ResponseEntity<StudentDetails> = ResponseEntity.ok(this)
