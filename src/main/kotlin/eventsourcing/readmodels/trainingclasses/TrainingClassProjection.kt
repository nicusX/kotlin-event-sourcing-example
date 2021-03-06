package eventsourcing.readmodels.trainingclasses

import arrow.core.getOrElse
import eventsourcing.domain.*
import eventsourcing.readmodels.DocumentStore
import eventsourcing.readmodels.InconsistentReadModelException
import eventsourcing.readmodels.SingleDocumentStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO Add the ability to rebuild the Read Model, re-streaming all events. Possibly on InconsistentReadModelException

// Note the projection assumes events are always consistent.
// No need to validate data and any exceptions mean something went wrong.

class TrainingClassProjection(
        private val trainingClassDetailsStore: DocumentStore<TrainingClassDetails>,
        private val trainingClassListStore: SingleDocumentStore<TrainingClassList>,
        private val studentsContactsStore: DocumentStore<StudentContacts>)
    : Handles<Event> {

    override fun handle(event: Event) {
        when (event) {

            is NewClassScheduled -> {
                log.debug("Adding new TrainingClass {} in read model", event.classId)
                trainingClassDetailsStore.addNewClass(event.toTrainingClassDetails())
                trainingClassListStore.addNewClassAndSort(event.toTrainingClass())
            }

            is StudentEnrolled -> {
                val classId = event.classId
                val studentId = event.studentId
                log.debug("Adding Student {} to Class {} in read model", studentId, classId)
                val student: EnrolledStudent = studentsContactsStore.lookupByStudentIdOrFail(studentId).toEnrolledStudent()
                trainingClassDetailsStore.addStudentToClass(classId, student, event.version!!)
                // No need to update the class list
            }

            is StudentUnenrolled -> {
                log.debug("Removing Student {} from Class {} in read model", event.studentId, event.classId)
                val student = studentsContactsStore.lookupByStudentIdOrFail(event.studentId).toEnrolledStudent()
                trainingClassDetailsStore.removeStudentFromClass(event.classId, student, event.version!!)
                // No need to update the class list
            }

            is NewStudentRegistered -> {
                log.debug("New Student {}. Adding to list of Student Contacts in read model", event.studentId)
                studentsContactsStore.addNewStudentContacts(event.toStudentContacts())
            }
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(TrainingClassProjection::class.java)
    }
}

private fun NewClassScheduled.toTrainingClassDetails(): TrainingClassDetails =
        TrainingClassDetails(
                classId = this.classId,
                title = this.title,
                date = this.date,
                totalSize = this.classSize,
                availableSpots = this.classSize,
                students = emptyList(),
                version = this.version!!)

private fun NewClassScheduled.toTrainingClass(): TrainingClass =
        TrainingClass(
                classId = this.classId,
                title = this.title,
                date = this.date)

private fun StudentContacts.toEnrolledStudent() =
        EnrolledStudent(
                studentId = this.studentId,
                contactType = "email",
                contact = this.email) // We only support email contacts ;)

private fun NewStudentRegistered.toStudentContacts() = StudentContacts(this.studentId, this.email)

private fun DocumentStore<TrainingClassDetails>.addNewClass(newClass: TrainingClassDetails) {
    TrainingClassProjection.log.trace("Add Class to TrainingClassDetails view: {}", newClass)
    this.save(newClass.classId, newClass)
}

private fun SingleDocumentStore<TrainingClassList>.addNewClassAndSort(newClass: TrainingClass) {
    TrainingClassProjection.log.trace("Add Class to TrainingClassList view: {}", newClass)
    this.save(((this.get()) + newClass).sortedBy { it.date })
}

private fun DocumentStore<StudentContacts>.lookupByStudentIdOrFail(studentId: String): StudentContacts =
        // If the StudentContacts is not there, the Read Model is stale
        this.get(studentId).getOrElse { throw InconsistentReadModelException }


private fun DocumentStore<TrainingClassDetails>.lookupByClassIdOrFail(classId: ClassID): TrainingClassDetails =
        // If TrainingClassDetails is not there, the Read Model is stale
        this.get(classId).getOrElse { throw InconsistentReadModelException }


private fun DocumentStore<TrainingClassDetails>.addStudentToClass(classId: String, student: EnrolledStudent, newVersion: Long) {
    val old: TrainingClassDetails = this.lookupByClassIdOrFail(classId)
    val new = old.copy(
            availableSpots = old.availableSpots - 1,
            students = old.students + student,
            version = newVersion)
    TrainingClassProjection.log.trace("Adding Student to Class in TrainingClassDetails view. Updating {} -> {}", old, new)
    this.save(classId, new)
}

private fun DocumentStore<TrainingClassDetails>.removeStudentFromClass(classId: String, student: EnrolledStudent, newVersion: Long) {
    val old: TrainingClassDetails = this.lookupByClassIdOrFail(classId)
    val new = old.copy(
            availableSpots = old.availableSpots + 1,
            students = old.students - student,
            version = newVersion)

    TrainingClassProjection.log.trace("Removing Student from Class in TrainingClassDetails view. Updating {} -> {}", old, new)
    this.save(classId, new)
}

private fun DocumentStore<StudentContacts>.addNewStudentContacts(newStudent: StudentContacts) {
    TrainingClassProjection.log.trace("Adding StudentContacts to StudentContacts view: {}", newStudent)
    this.save(newStudent.studentId, newStudent)
}