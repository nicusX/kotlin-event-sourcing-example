package eventsourcing.readmodel

import com.nhaarman.mockitokotlin2.*
import eventsourcing.domain.*
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TrainingClassViewTest {

    @Test
    fun `given a, when it receives a New Class Scheduled event, it saves the new Class into the datastore`() {
        val (datastore, sut) = given()

        val event = NewClassScheduled("0001", "-title-", LocalDate.now(), 10, 0L)
        sut.handle(event)

        verify(datastore).save(eq(event.classId), check{
            assertThatDTO(it)
                    .hasClassId(event.classId)
                    .hasTitle(event.title)
                    .hasTotalSize(event.classSize)
                    .hasAvailableSpots(event.classSize)
                    .containsNoStudent()
                    .hasVersion(event.version!!)
        })
        verifyNoMoreInteractions(datastore)
    }

    @Test
    fun `given a Training Class View, when it receives a Student Enrolled event, then it retrieves the Class and saves the Class with the new student, available spots number decreased and version updated`() {
        val classId = "001"
        val availSpots = 10
        val version = 0L
        val (datastore, sut) = given(TrainingClassDTO(
                classId = classId,
                title = "a title",
                date = LocalDate.now(),
                totalSize = 10,
                availableSpots = availSpots,
                students = emptyList(),
                version = version ))


        val event = StudentEnrolled(classId, "student001", 1L)
        sut.handle(event)

        verify(datastore).getById(eq(classId))
        verify(datastore).save(eq(event.classId), check{
            assertThatDTO(it)
                    .hasClassId(event.classId)
                    .containsStudent(event.studentId)
                    .hasAvailableSpots( availSpots - 1 )
                    .hasVersion(event.version!!)
        })
        verifyNoMoreInteractions(datastore)
    }

    @Test
    fun `given a Training Class View, when it receives a Student Unenrolled event, it saves the Class without the Student, available spots number increased and version updated`() {
        val classId = "001"
        val availSpots = 9
        val version = 2L
        val studentId = "student002"
        val (datastore, sut) = given(TrainingClassDTO(
                classId = classId,
                title = "a title",
                date = LocalDate.now(),
                totalSize = 10,
                availableSpots = availSpots,
                students = listOf(studentId),
                version = version ))

        val event = StudentUnenrolled(classId, studentId, 3L)
        sut.handle(event)

        verify(datastore).getById(eq(classId))
        verify(datastore).save(eq(event.classId), check{
            assertThatDTO(it)
                    .hasClassId(event.classId)
                    .containsNoStudent()
                    .hasAvailableSpots( availSpots + 1 )
                    .hasVersion(event.version!!)
        })
        verifyNoMoreInteractions(datastore)
    }

    companion object {
        fun assertThatDTO(actual : TrainingClassDTO) : TrainingClassDTOAssert =
                TrainingClassDTOAssert(actual)
    }


    private fun given(dto: TrainingClassDTO): Pair<Datastore<TrainingClassDTO>, TrainingClassView> {
        val datastore = mock<Datastore<TrainingClassDTO>> {
            on { getById(eq(dto.classId)) }.doReturn(dto)
        }
        return Pair(datastore, TrainingClassView(datastore))
    }

    private fun given() : Pair<Datastore<TrainingClassDTO>, TrainingClassView> {
        val datastore = mock<Datastore<TrainingClassDTO>>()
        return Pair(datastore, TrainingClassView(datastore))
    }
}

internal class TrainingClassDTOAssert(actual: TrainingClassDTO) : AbstractAssert<TrainingClassDTOAssert, TrainingClassDTO>(actual, TrainingClassDTOAssert::class.java) {
    fun hasClassId(expected: String) : TrainingClassDTOAssert {
        assertThat(actual.classId).isEqualTo(expected)
        return this
    }

    fun hasTitle(expected: String): TrainingClassDTOAssert {
        assertThat(actual.title).isEqualTo(expected)
        return this
    }

    fun hasTotalSize(expected: Int) : TrainingClassDTOAssert {
        assertThat(actual.totalSize).isEqualTo(expected)
        return this
    }

    fun hasAvailableSpots(expected: Int): TrainingClassDTOAssert {
        assertThat(actual.availableSpots).isEqualTo(expected)
        return this
    }

    fun hasVersion(expected: Long) : TrainingClassDTOAssert {
        assertThat(actual.version).isEqualTo(expected)
        return this
    }

    fun containsStudent(expected: StudentID) : TrainingClassDTOAssert {
        assertThat(actual.students).contains(expected)
        return this
    }

    fun containsAllStudents(expected: Iterable<StudentID>) : TrainingClassDTOAssert {
        assertThat(actual.students).containsAll(expected)
        return this
    }

    fun containsNoStudent() : TrainingClassDTOAssert {
        assertThat(actual.students).isEmpty()
        return this
    }
}

