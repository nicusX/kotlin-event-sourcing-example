package eventsourcing.readmodels

import eventsourcing.domain.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class StudentDTO (
        val studentId: StudentID,
        val email: EMail,
        val fullName: String,
        val version: Long) {

    companion object {
        fun from(event: NewStudentRegistered) =
                StudentDTO (
                        studentId = event.studentId,
                        email = event.email,
                        fullName = event.fullName,
                        version = event.version!!)
    }
}

class StudentView(private val datastore: Datastore<StudentDTO>) : Handles<Event> {

    fun getStudentById(studentId: StudentID): StudentDTO = datastore.get(studentId)

    fun listStudents() : List<StudentDTO> = datastore.list()

    override fun handle(event: Event) {
        when(event) {
            is NewStudentRegistered -> {
                val new = StudentDTO.from(event)
                log.debug("Save new Student {}", new)
                datastore.save(new.studentId, new)
            }
            else -> log.debug("{} not handled. Event ignored", event)
        }
    }

    companion object {
        private val log : Logger = LoggerFactory.getLogger(StudentView::class.java)
    }
}
