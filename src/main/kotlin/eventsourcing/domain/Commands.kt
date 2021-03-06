package eventsourcing.domain

import java.time.Instant
import java.time.LocalDate

abstract class Command(val createdAt: Instant = Instant.now())

data class ScheduleNewClass(
        val title: String,
        val date: LocalDate,
        val size: Int) : Command()

data class EnrollStudent(
        val classId: ClassID,
        val studentId: StudentID,
        val expectedVersion: Long) : Command()

data class UnenrollStudent(
        val classId: ClassID,
        val studentId: StudentID,
        val reason: String,
        val expectedVersion: Long) : Command()

// TODO Add CancelTrainingClass, notifying all enrolled Students (behaviour with side effects)

data class RegisterNewStudent(
        val email: EMail,
        val fullName: String) : Command()

// TODO Add UnregisterStudent, removing the student from all classes (command affecting multiple aggregates)
