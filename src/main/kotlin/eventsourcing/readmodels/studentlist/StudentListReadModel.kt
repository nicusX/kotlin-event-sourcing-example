package eventsourcing.readmodels.studentlist

import eventsourcing.domain.Event
import eventsourcing.domain.Handles
import eventsourcing.domain.NewStudentRegistered
import eventsourcing.readmodels.SingleDocumentStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class Student(val studentId: String, val fullName: String)

typealias StudentList = Iterable<Student>

// Separating the list of Student from a read model containing each Student is clearly an overkill in this case
// but it is meant to demonstrate read models may be very specialised and decoupled


class StudentListReadModel(private val studentListStore: SingleDocumentStore<StudentList>) {
    fun allStudents() : StudentList = studentListStore.get() ?: emptyList()
}

class StudentListProjection(private val studentListStore : SingleDocumentStore<StudentList>) : Handles<Event> {
    override fun handle(event: Event) {
        when (event) {
            is NewStudentRegistered -> {
                val new = event.toStudent()
                log.debug("Add new Student {} to the read-model and sort the list alphabetically by fullName", new)
                val newSortedList = ((studentListStore.get() ?: emptyList()) + new).sortedBy { it.fullName }
                studentListStore.save(newSortedList)
            }
        }
    }

    private fun NewStudentRegistered.toStudent() = Student(this.studentId, this.fullName)

    companion object {
        private val log: Logger = LoggerFactory.getLogger(StudentListProjection::class.java)
    }

}