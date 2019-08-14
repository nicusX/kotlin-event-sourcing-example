package eventsourcing.readmodels

import eventsourcing.domain.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

// TODO Make TrainingClassView maintaining a list of StudentDTOs within the class

data class TrainingClassDTO (
        val classId: ClassID,
        val title : String,
        val date: LocalDate,
        val totalSize: Int,
        val availableSpots: Int,
        val students : List<StudentID>, // TODO make list of Students
        val version: Long) {


    companion object {
        fun from(event: NewClassScheduled) =
            TrainingClassDTO(
                    classId = event.classId,
                    title = event.title,
                    date = event.date,
                    totalSize = event.classSize,
                    availableSpots = event.classSize,
                    students = listOf(),
                    version = event.version!!)
    }
}

class TrainingClassView(private val datastore: Datastore<TrainingClassDTO>) : Handles<Event> {

    fun getById(classId: ClassID) : TrainingClassDTO =
            datastore.get(classId)

    fun list() : List<TrainingClassDTO> =
            datastore.list()

    override fun handle(event: Event) {
        // The event handler does not check any business rules when updating the vew

        when(event) {
            is NewClassScheduled -> {
                val new = TrainingClassDTO.from(event)
                log.debug("Save new class {}", new)
                datastore.save(event.classId, new )
            }
            is StudentEnrolled -> {
                val classId = event.classId
                val old = datastore.get(classId)
                val new = old.copy(
                        availableSpots = old.availableSpots - 1,
                        students = old.students + event.studentId,
                        version = event.version!!
                )
                log.debug("Enrolling student. Updating {} -> {}", old, new)
                datastore.save(classId, new)
            }
            is StudentUnenrolled -> {
                val classId = event.classId
                val old = datastore.get(classId)
                val new = old.copy(
                        availableSpots = old.availableSpots + 1,
                        students = old.students - event.studentId,
                        version = event.version!!
                )
                log.debug("Unenrolling student. Updating {} -> {}", old, new)
                datastore.save(classId, new)
            }
            else -> log.debug("{} not handled. Event ignored", event)
        }
    }

    companion object {
        private val log : Logger = LoggerFactory.getLogger(TrainingClassView::class.java)
    }

}
