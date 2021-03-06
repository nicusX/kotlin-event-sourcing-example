package eventsourcing.readmodels.trainingclasses

import arrow.core.Option
import eventsourcing.readmodels.DocumentStore
import eventsourcing.readmodels.SingleDocumentStore
import java.time.LocalDate

// This Read Model keep an internal view with Student contacts of ALL students (not just those enrolled). This provides
// the information required to add the enrolled student to the class and not included in the StudentEnrolled
// While the TrainingClass state is updated listening to NewClassScheduled, StudentEnrolled, StudentUnenrolled events
// the secondary view is updated when a NewStudentRegistered is received

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


class TrainingClassReadModel (
        private val trainingClassDetailsStore: DocumentStore<TrainingClassDetails>,
        private val trainingClassListStore: SingleDocumentStore<TrainingClassList>) {

    fun allClasses() : TrainingClassList =
            trainingClassListStore.get()

    fun getTrainingClassDetailsById(classId: String) : Option<TrainingClassDetails> =
            trainingClassDetailsStore.get(classId)

    // Note the Student Contacts view is not exposed. It is only used internally
}
