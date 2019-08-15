package eventsourcing.readmodels.trainingclasses

import eventsourcing.readmodels.DocumentNotFound
import eventsourcing.readmodels.DocumentStore
import eventsourcing.readmodels.SingleDocumentStore
import java.time.LocalDate
import java.util.*

data class TrainingClassDetails (
        val classId: String,
        val title : String,
        val date: LocalDate,
        val totalSize: Int,
        val availableSpots: Int,
        val students : List<EnrolledStudent>,
        val version: Long )

data class EnrolledStudent (
        val studentId: String,
        val contactType : String,
        val contact: String )

data class StudentContacts (
        val studentId: String,
        val email: String )

data class TrainingClass (
        val classId: String,
        val title : String,
        val date: LocalDate)

typealias TrainingClassList = List<TrainingClass>

// This Read Model maintains an internal view with Student contacts of ALL students (not just those enrolled). This provides
// the information required to add the enrolled student to the class and not included in the StudentEnrolled
// While the TrainingClass state is updated listening to NewClassScheduled, StudentEnrolled, StudentUnenrolled events
// the secondary view is updated when a NewStudentRegistered is received

// Note the projection assumes events are always consistent.
// No need to validate data and any exceptions mean something went wrong.

class TrainingClassReadModel (
        private val trainingClassDetailsStore: DocumentStore<TrainingClassDetails>,
        private val trainingClassListStore: SingleDocumentStore<TrainingClassList>) {

    fun allClasses() : TrainingClassList =
            trainingClassListStore.get() ?: emptyList()

    fun getTrainingClassDetailsById(classId: String) : Optional<TrainingClassDetails> =
            try { Optional.of(trainingClassDetailsStore.get(classId)) }
            catch ( notFound: DocumentNotFound) { Optional.empty()}

    // Note the Student Contacts view is not exposed. It is only used internally
}
