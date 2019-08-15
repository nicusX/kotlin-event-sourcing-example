package eventsourcing.readmodels.trainingclasses

import eventsourcing.domain.*
import eventsourcing.readmodels.DocumentStore
import eventsourcing.readmodels.SingleDocumentStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO Add the ability to rebuild the Read Model, re-streaming all events

class TrainingClassProjection (
        private val trainingClassDetailsStore: DocumentStore<TrainingClassDetails>,
        private val trainingClassListStore: SingleDocumentStore<TrainingClassList>,
        private val studentsContactsStore: DocumentStore<StudentContacts>)
    : Handles<Event> {

    override fun handle(event: Event) {
        when (event) {

            is NewClassScheduled -> {
                log.debug("Adding new TrainingClass {}", event.classId)
                trainingClassDetailsStore.addNewClass(event.toTrainingClassDetails())
                trainingClassListStore.addNewClassAndSort(event.toTrainingClass())
            }

            is StudentEnrolled -> {
                val classId = event.classId
                val studentId = event.studentId
                log.debug("Adding Student {} to Class {}", studentId, classId)
                val student = lookupStudentContacts(studentId).toEnrolledStudent()
                trainingClassDetailsStore.addStudentToClass(classId, student, event.version!!)
                // No need to update the class list
            }

            is StudentUnenrolled -> {
                log.debug("Removing Student {} from Class {}", event.studentId, event.classId)
                val student = lookupStudentContacts(event.studentId).toEnrolledStudent()
                trainingClassDetailsStore.removeStudentFromClass(event.classId, student, event.version!!)
                // No need to update the class list
            }

            is NewStudentRegistered -> {
                log.debug("New Student {}. Adding to list of contacts", event.studentId)
                studentsContactsStore.addNewStudentContacts(event.toStudentContacts())
            }
        }
    }

    private fun lookupStudentContacts(studentId: String) : StudentContacts = studentsContactsStore.get(studentId)

    companion object {
        val log: Logger = LoggerFactory.getLogger(TrainingClassProjection::class.java)
    }
}

private fun NewClassScheduled.toTrainingClassDetails() : TrainingClassDetails =
        TrainingClassDetails(
                classId = this.classId,
                title = this.title,
                date = this.date,
                totalSize = this.classSize,
                availableSpots = this.classSize,
                students = emptyList(),
                version = this.version!!)

private fun NewClassScheduled.toTrainingClass() : TrainingClass =
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
private fun DocumentStore<TrainingClassDetails>.addNewClass(newClass: TrainingClassDetails) =
        this.save(newClass.classId, newClass)

private fun SingleDocumentStore<TrainingClassList>.addNewClassAndSort(newClass: TrainingClass) =
        this.save(((this.get() ?: emptyList()) + newClass).sortedBy { it.date } )

private fun DocumentStore<TrainingClassDetails>.addStudentToClass(classId: String, student: EnrolledStudent, newVersion : Long) {
    val old = this.get(classId) // We assume events are always processed in order, so the class exists
    val new = old.copy(
            availableSpots = old.availableSpots - 1,
            students = old.students + student,
            version = newVersion)
    TrainingClassProjection.log.trace("Adding Student: updating {} -> {}", old, new)
    this.save(classId, new)
}

private fun DocumentStore<TrainingClassDetails>.removeStudentFromClass(classId: String, student: EnrolledStudent, newVersion : Long) {
    val old = this.get(classId)
    val new = old.copy(
            availableSpots = old.availableSpots + 1,
            students = old.students - student,
            version = newVersion)
    TrainingClassProjection.log.trace("Removing Student: updating {} -> {}", old, new)
    this.save(classId, new)
}

private fun DocumentStore<StudentContacts>.addNewStudentContacts(newStudent: StudentContacts) =
        this.save(newStudent.studentId, newStudent)