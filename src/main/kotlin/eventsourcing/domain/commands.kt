package eventsourcing.domain

import java.time.LocalDate

abstract class Command : Message()

data class ScheduleNewClass(val title: String, val date: LocalDate, val size: Int) : Command()

data class EnrollStudent(val classId: ClassID, val studentId: StudentID, val originalVersion: Long) : Command()

// TODO Add a "reason"
data class UnenrollStudent(val classId: ClassID, val studentId: StudentID, val originalVersion: Long) : Command()

// TODO Add support for CancelTrainingClass, notifying all enrolled Students