package eventsourcing.readmodels.studentlist

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import eventsourcing.readmodels.SingleDocumentStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StudentListReadModelTest {
    @Test
    fun `given a Student List read model containing no Student, when I retrieve all Students, then I get an empty list`(){
        val (sut, store) = givenReadModelAndStore()
        whenever(store.get()).thenReturn(emptyList())

        val result : Iterable<Student> = sut.allStudents()
        assertThat(result).isEmpty()

        verify(store).get()
        verifyNoMoreInteractions(store)
    }

    @Test
    fun `given a Student List read model containing some Students, when I retrieve all Students, then I get a list with all Students`() {
        val (sut, store) = givenReadModelAndStore()
        whenever(store.get()).thenReturn( listOf(
                Student("STUDENT001", "Student One"),
                Student("STUDENT002", "Student Two")))

        val result : Iterable<Student> = sut.allStudents()
        assertThat(result).hasSize(2)
        assertThat(result.first()).isEqualTo(Student("STUDENT001", "Student One"))
        assertThat(result.last()).isEqualTo(Student("STUDENT002", "Student Two"))
    }


    private fun givenReadModelAndStore(): Pair<StudentListReadModel, SingleDocumentStore<StudentList>> {
        val datastore = mock<SingleDocumentStore<StudentList>>()
        return Pair(StudentListReadModel(datastore), datastore)
    }
}
