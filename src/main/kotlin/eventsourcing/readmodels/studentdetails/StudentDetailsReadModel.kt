package eventsourcing.readmodels.studentdetails

import arrow.core.Option
import eventsourcing.domain.Event
import eventsourcing.domain.Handles
import eventsourcing.domain.NewStudentRegistered
import eventsourcing.readmodels.DocumentStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class StudentDetails(
        val studentId: String,
        val email: String,
        val fullName: String,
        val version: Long)


// TODO Add the ability to rebuild the Read Model, re-streaming all events

class StudentDetailsProjection(private val studentDetailsStore: DocumentStore<StudentDetails>) : Handles<Event> {

    override fun handle(event: Event) {
        when (event) {
            is NewStudentRegistered -> {
                val new = event.toStudentDetails()
                log.debug("Save Student {} into the read-model", new)
                studentDetailsStore.save(new.studentId, new)
            }
        }
    }

    private fun NewStudentRegistered.toStudentDetails() =
            StudentDetails(this.studentId, this.email, this.fullName, this.version!!)

    companion object {
        private val log: Logger = LoggerFactory.getLogger(StudentDetailsProjection::class.java)
    }
}

/**
 * External, read-only facade for the read model
 */
class StudentDetailsReadModel(private val studentDetailsStore: DocumentStore<StudentDetails>) {
    fun getStudentById(studentId: String) : Option<StudentDetails> = studentDetailsStore.get(studentId)
}
