package eventsourcing.domain

import java.time.LocalDate

// FIXME Events should contain a version, but the version cannot be assigned until the event is stored into its EventStore stream
// FIXME Events should all contain a target aggregate type and ID
abstract class Event() : Message()

data class NewClassScheduled(val classId: ClassID, val title: String, val date: LocalDate, val classSize: Int) : Event()

data class StudentEnrolled(val classId: ClassID, val studentId: StudentID) : Event()

// TODO add "reason"
data class StudentUnenrolled(val classId: ClassID, val studentId: StudentID) : Event()

// FIXME Identify "new" and "final" events?