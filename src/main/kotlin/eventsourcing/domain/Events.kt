package eventsourcing.domain

import java.time.Instant
import java.time.LocalDate

abstract class Event(private val version: Long?) : Message() {
    val eventTime =  Instant.now() // It would be better to inject a clock, but we have no logic to test around eventTime
    fun version(): Long? = version  // The version is assigned only then the Event is stored in the EventStore
                                    // There are effectively two types of Events: before and after they are stored in the Event Store.
    abstract fun copyWithVersion(version: Long): Event
}

data class NewClassScheduled (
        val classId: ClassID,
        val title: String,
        val date: LocalDate,
        val classSize: Int,
        val version: Long? = null) : Event(version) {

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

data class StudentUnenrolled (val classId: ClassID,
                             val studentId: StudentID,
                             val reason: String,
                             val version: Long? = null) : Event(version) {
    override fun copyWithVersion(version: Long): StudentUnenrolled =
            this.copy(version = version)
}

data class NewStudentRegistered (
        val studentId: StudentID,
        val email : EMail,
        val fullName: String,
        val version: Long? = null) : Event(version) {
    override fun copyWithVersion(version: Long): NewStudentRegistered =
            this.copy(version = version)
}
