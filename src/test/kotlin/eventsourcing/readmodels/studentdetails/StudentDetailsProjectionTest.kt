package eventsourcing.readmodels.studentdetails

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import eventsourcing.domain.NewStudentRegistered
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import com.nhaarman.mockitokotlin2.check
import eventsourcing.readmodels.DocumentStore

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
}

private fun givenProjectionAndStore(): Pair<StudentDetailsProjection, DocumentStore<StudentDetails>> {
    val datastore = mock<DocumentStore<StudentDetails>>()
    return Pair(StudentDetailsProjection(datastore), datastore)
}