package eventsourcing.readmodels.trainingclasses

import arrow.core.Some
import com.nhaarman.mockitokotlin2.*
import eventsourcing.domain.NewClassScheduled
import eventsourcing.domain.NewStudentRegistered
import eventsourcing.domain.StudentEnrolled
import eventsourcing.domain.StudentUnenrolled
import eventsourcing.readmodels.DocumentStore
import eventsourcing.readmodels.SingleDocumentStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TrainingClassProjectionTest {
    lateinit var trainingClassDetailsStore: DocumentStore<TrainingClassDetails>
    lateinit var trainingClassListStore: SingleDocumentStore<TrainingClassList>
    lateinit var studentsContactsStore: DocumentStore<StudentContacts>

    @BeforeEach
    fun before() {
        trainingClassDetailsStore = mock<DocumentStore<TrainingClassDetails>>()
        trainingClassListStore = mock<SingleDocumentStore<TrainingClassList>>()
        studentsContactsStore = mock<DocumentStore<StudentContacts>>()
    }


    @Test
    fun `given a Training Class projection containing no Class, when it handles a NewClassScheduled, then it adds the new class to the list and Details stores`() {
        val sut = givenTrainingClassProjection()
        whenever(trainingClassListStore.get()).thenReturn(emptyList())

        val event = NewClassScheduled("CLASS001", "Class title", LocalDate.now(), 10, 0L)
        sut.handle(event)

        verify(trainingClassListStore).get()
        verify(trainingClassListStore).save(check {
            assertThat(it).hasSize(1)
            assertThat(it[0].classId).isEqualTo(event.classId)
            assertThat(it[0].title).isEqualTo(event.title)
            assertThat(it[0].date).isEqualTo(event.date)
        })

        verify(trainingClassDetailsStore).save(eq(event.classId), check {
            assertThat(it.classId).isEqualTo(event.classId)
            assertThat(it.title).isEqualTo(event.title)
            assertThat(it.date).isEqualTo(event.date)
            assertThat(it.availableSpots).isEqualTo(event.classSize)
            assertThat(it.totalSize).isEqualTo(event.classSize)
            assertThat(it.students).isEmpty()
            assertThat(it.version).isEqualTo(event.version)
        })

        verifyNoMoreInteractions(trainingClassListStore)
        verifyNoMoreInteractions(trainingClassDetailsStore)
        verifyNoMoreInteractions(studentsContactsStore)
    }

    @Test
    fun `given a Training Class projection, when it handles a StudentEnrolled event, then it retrieves the Class Details and saves it back with the new student, available spots number decreased and version updated`() {
        val sut = givenTrainingClassProjection()

        val classId = "CLASS001"
        val oldTrainingClassDetails = TrainingClassDetails(
                classId = classId,
                title = "Class Title",
                date = LocalDate.now(),
                totalSize = 10,
                availableSpots = 10,
                students = emptyList(),
                version = 0L)
        val studentId = "STUDENT042"
        val studentContacts = StudentContacts(studentId, "test@ema.il")
        val enrolledStudent = EnrolledStudent(studentId, "email", studentContacts.email)

        whenever(trainingClassDetailsStore.get(any())).thenReturn(Some(oldTrainingClassDetails))
        whenever(studentsContactsStore.get(any())).thenReturn(Some(studentContacts))

        val event = StudentEnrolled(classId, studentId, 0L)
        sut.handle(event)

        verify(studentsContactsStore).get(studentId)
        verify(trainingClassDetailsStore).get(eq(event.classId))
        verify(trainingClassDetailsStore).save(eq(event.classId), check {
            assertThat(it.classId).isEqualTo(event.classId)
            assertThat(it.availableSpots).isEqualTo(oldTrainingClassDetails.availableSpots - 1)
            assertThat(it.totalSize).isEqualTo(oldTrainingClassDetails.totalSize)
            assertThat(it.students).hasSize(1)
            assertThat(it.students).contains(enrolledStudent)
            assertThat(it.version).isEqualTo(event.version)
        })

        verifyNoMoreInteractions(trainingClassListStore)
        verifyNoMoreInteractions(trainingClassDetailsStore)
        verifyNoMoreInteractions(studentsContactsStore)
    }

    @Test
    fun `given a TrainingClass projection, when it handles a StudentUnenrolled events, it retrieves the Class Details and saves back without the student, avail spots +1 and new version`() {
        val sut = givenTrainingClassProjection()

        val studentId = "STUDENT042"
        val studentContacts = StudentContacts(studentId, "test@ema.il")
        val enrolledStudent = EnrolledStudent(studentId, "email", studentContacts.email)
        val classId = "CLASS001"
        val oldTrainingClassDetails = TrainingClassDetails(
                classId = classId,
                title = "Class Title",
                date = LocalDate.now(),
                totalSize = 10,
                availableSpots = 0,
                students = listOf(enrolledStudent),
                version = 1L)

        whenever(trainingClassDetailsStore.get(any())).thenReturn(Some(oldTrainingClassDetails))
        whenever(studentsContactsStore.get(any())).thenReturn(Some(studentContacts))

        val event = StudentUnenrolled(classId, studentId, "Some good reasons", 2L)
        sut.handle(event)

        verify(studentsContactsStore).get(studentId)
        verify(trainingClassDetailsStore).get(eq(event.classId))
        verify(trainingClassDetailsStore).save(eq(event.classId), check {
            assertThat(it.classId).isEqualTo(event.classId)
            assertThat(it.availableSpots).isEqualTo(oldTrainingClassDetails.availableSpots + 1)
            assertThat(it.totalSize).isEqualTo(oldTrainingClassDetails.totalSize)
            assertThat(it.students).doesNotContain(enrolledStudent)
            assertThat(it.version).isEqualTo(event.version)
        })

        verifyNoMoreInteractions(trainingClassListStore)
        verifyNoMoreInteractions(trainingClassDetailsStore)
        verifyNoMoreInteractions(studentsContactsStore)
    }

    @Test
    fun `given a TrainingClass projection, when it handles a NewStudentRegistered event, then it adds the new Student's Contact`() {
        val sut = givenTrainingClassProjection()

        val event = NewStudentRegistered(
                studentId = "STUDENT001",
                email = "test@ema.il",
                fullName = "Full Name",
                version = 0L)
        sut.handle(event)

        verify(studentsContactsStore).save( eq(event.studentId), check {
            assertThat(it.studentId).isEqualTo(event.studentId)
            assertThat(it.email).isEqualTo(event.email)
        })

        verifyNoMoreInteractions(trainingClassListStore)
        verifyNoMoreInteractions(trainingClassDetailsStore)
        verifyNoMoreInteractions(studentsContactsStore)
    }

    private fun givenTrainingClassProjection() =
            TrainingClassProjection(trainingClassDetailsStore, trainingClassListStore, studentsContactsStore)
}




