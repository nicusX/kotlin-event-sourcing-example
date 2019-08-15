package eventsourcing.readmodels.studentlist

import com.nhaarman.mockitokotlin2.*
import eventsourcing.domain.NewStudentRegistered
import eventsourcing.readmodels.SingleDocumentStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StudentListProjectionTest {

    @Test
    fun `given a Student List projection, when it handles a NewStudentRegistered, then it get the old the stored list and replace it with a new list including the new Student and sorted alphabetically`(){
        val (sut, store) = givenProjectionAndStore()
        val oldList = listOf(Student("STUDENT001", "Zorro"))
        whenever(store.get()).thenReturn(oldList)

        val event = NewStudentRegistered("STUDENT002", "test@ema.il", "Ajeje Brazov", 42L)
        sut.handle(event)

        verify(store).get()
        verify(store).save( check{
            assertThat(it).hasSize(2)
            assertThat(it.first()).isEqualTo(Student("STUDENT002", "Ajeje Brazov"))
            assertThat(it.last()).isEqualTo(Student("STUDENT001", "Zorro"))
        })
        verifyNoMoreInteractions(store)
    }


    private fun givenProjectionAndStore(): Pair<StudentListProjection, SingleDocumentStore<StudentList>> {
        val datastore = mock<SingleDocumentStore<StudentList>>()
        return Pair(StudentListProjection(datastore), datastore)
    }
}
