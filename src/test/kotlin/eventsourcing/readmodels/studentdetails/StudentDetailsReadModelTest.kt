package eventsourcing.readmodels.studentdetails

import arrow.core.None
import arrow.core.Some
import com.nhaarman.mockitokotlin2.*
import eventsourcing.readmodels.DocumentStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StudentDetailsReadModelTest {

    @Test
    fun `given a Student Details read model, when I get an existing studentId, then it returns the student`() {
        val (sut, store) = givenReadModelAndStore()
        val aStudent = StudentDetails("STUDENT001", "test@ema.il", "Full Name", 2L)
        whenever(store.get(any())).thenReturn( Some(aStudent) )

        val result = sut.getStudentById("STUDENT001")
        assertThat(result).isEqualTo(aStudent)

        verify(store).get(eq("STUDENT001"))
        verifyNoMoreInteractions(store)
    }

    @Test
    fun `given a Student Details read model, when I get a non existing studentId, then it returns null`(){
        val (sut, store) = givenReadModelAndStore()
        whenever(store.get(any())).thenReturn( None )

        val result = sut.getStudentById("NOT-EXISTS")
        assertThat(result).isNull()
    }


    private fun givenReadModelAndStore(): Pair<StudentDetailsReadModel, DocumentStore<StudentDetails>> {
        val datastore = mock<DocumentStore<StudentDetails>>()
        return Pair(StudentDetailsReadModel(datastore), datastore)
    }
}
