package eventsourcing.readmodels

import com.nhaarman.mockitokotlin2.*
import eventsourcing.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TrainingClassViewTest {

    @Test
    fun `given an empty Training Class view, when it receives a New Class Scheduled event, it saves a new Class into the datastore`() {
        val (datastore, sut) = givenEmptyDatastoreAndView()

        val event = NewClassScheduled("0001", "-title-", LocalDate.now(), 10, 0L)
        sut.handle(event)

        verify(datastore).save(eq(event.classId), check {
            assertThat(it.classId).isEqualTo(event.classId)
            assertThat(it.title).isEqualTo(event.title)
            assertThat(it.date).isEqualTo(event.date)
            assertThat(it.availableSpots).isEqualTo(event.classSize)
            assertThat(it.totalSize).isEqualTo(event.classSize)
            assertThat(it.students).isEmpty()
            assertThat(it.version).isEqualTo(event.version)
        })
        verifyNoMoreInteractions(datastore)
    }

    @Test
    fun `given an empty Training Class view, when it receives a Student Enrolled event, then it retrieves the Class and saves it back with the new student, available spots number decreased and version updated`() {
        val classId = "001"
        val originalSize = 10
        val origAvailSpots = 10
        val origVersion = 0L
        val (datastore, sut) = givenDatastoreAndViewContaining(
                TrainingClassDTO(
                        classId = classId,
                        title = "a title",
                        date = LocalDate.now(),
                        totalSize = originalSize,
                        availableSpots = origAvailSpots,
                        students = emptyList(),
                        version = origVersion))


        val event = StudentEnrolled(classId, "student001", 1L)
        sut.handle(event)

        verify(datastore).get(eq(classId))
        verify(datastore).save(eq(event.classId), check {
            assertThat(it.classId).isEqualTo(event.classId)
            assertThat(it.students).contains(event.studentId)
            assertThat(it.availableSpots).isEqualTo(origAvailSpots - 1)
            assertThat(it.totalSize).isEqualTo(originalSize)
            assertThat(it.version).isEqualTo(event.version)
        })
        verifyNoMoreInteractions(datastore)
    }

    @Test
    fun `given an empty Training Class view, when it receives a Student Unenrolled event, it saves the Class without the Student, available spots number increased and version updated`() {
        val classId = "001"
        val originalSize = 10
        val origAvailSpots = 9
        val origVersion = 2L
        val studentId = "student002"
        val (datastore, sut) = givenDatastoreAndViewContaining(
                TrainingClassDTO(
                        classId = classId,
                        title = "a title",
                        date = LocalDate.now(),
                        totalSize = originalSize,
                        availableSpots = origAvailSpots,
                        students = listOf(studentId),
                        version = origVersion))

        val event = StudentUnenrolled(classId, studentId, "some reasons", 3L)
        sut.handle(event)

        verify(datastore).get(eq(classId))
        verify(datastore).save(eq(event.classId), check {
            assertThat(it.classId).isEqualTo(event.classId)
            assertThat(it.students).doesNotContain(event.studentId)
            assertThat(it.availableSpots).isEqualTo(origAvailSpots + 1)
            assertThat(it.totalSize).isEqualTo(originalSize)
            assertThat(it.version).isEqualTo(event.version)
        })
        verifyNoMoreInteractions(datastore)
    }

}

private fun givenDatastoreAndViewContaining(dto: TrainingClassDTO): Pair<Datastore<TrainingClassDTO>, TrainingClassView> {
    val datastore = mock<Datastore<TrainingClassDTO>> {
        on { get(eq(dto.classId)) }.doReturn(dto)
    }
    return Pair(datastore, TrainingClassView(datastore))
}

private fun givenEmptyDatastoreAndView(): Pair<Datastore<TrainingClassDTO>, TrainingClassView> {
    val datastore = mock<Datastore<TrainingClassDTO>>()
    return Pair(datastore, TrainingClassView(datastore))
}
