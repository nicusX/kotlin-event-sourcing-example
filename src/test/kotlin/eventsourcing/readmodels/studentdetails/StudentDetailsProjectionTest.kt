package eventsourcing.readmodels.studentdetails

import com.nhaarman.mockitokotlin2.*
import eventsourcing.domain.NewStudentRegistered
import eventsourcing.readmodels.DocumentStore
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class StudentDetailsProjectionTest {

    @Test
    fun `given a StudentDetails projection, when it handles a NewStudentRegistered event, then it saves the new StudentDetails in the datastore`(){
        val (sut, store) = givenProjectionAndStore()

        val event = NewStudentRegistered("STUDENT001", "test@ema.il", "Full Name", 42L)
        sut.handle(event)

        verify(store).save(eq(event.studentId), check {
            Assertions.assertThat(it.studentId).isEqualTo(event.studentId)
            Assertions.assertThat(it.email).isEqualTo(event.email)
            Assertions.assertThat(it.fullName).isEqualTo(event.fullName)
            Assertions.assertThat(it.version).isEqualTo(event.version)
        })
        verifyNoMoreInteractions(store)
    }


    private fun givenProjectionAndStore(): Pair<StudentDetailsProjection, DocumentStore<StudentDetails>> {
        val datastore = mock<DocumentStore<StudentDetails>>()
        return Pair(StudentDetailsProjection(datastore), datastore)
    }
}
