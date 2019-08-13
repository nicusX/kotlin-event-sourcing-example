package eventsourcing.domain

import java.time.LocalDate

abstract class Event(private val version: Long?) : Message() {
    // TODO find a better way of managing Event.version
    //      The problem is the version is assigned only then the Event is stored in the EventStore
    fun version(): Long? = version
    abstract fun copyWithVersion(version: Long): Event
}


data class NewClassScheduled (
        val classId: ClassID,
        val title: String,
        val date: LocalDate,
        val classSize: Int,
        val version: Long? = null) : Event(version) {

    // TODO any better way than reimplementing in all subclasses?
    override fun copyWithVersion(version: Long): NewClassScheduled =
            this.copy(version = version)
}



data class StudentEnrolled (
        val classId: ClassID,
        val studentId: StudentID,
        val version: Long? = null) : Event(version) {
    override fun copyWithVersion(version: Long): StudentEnrolled =
            this.copy(version = version)
}

// TODO add "reason"
data class StudentUnenrolled(val classId: ClassID,
                             val studentId: StudentID,
                             val version: Long? = null) : Event(version) {
    override fun copyWithVersion(version: Long): StudentUnenrolled =
            this.copy(version = version)
}
