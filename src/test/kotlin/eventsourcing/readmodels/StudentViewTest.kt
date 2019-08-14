package eventsourcing.readmodels

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import eventsourcing.domain.NewStudentRegistered
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StudentViewTest {

    @Test
    fun `given an empty Student view, when it receives a New Student Registered event, then it saves the new Student in the datastore`(){
        val (datastore, sut) = givenEmptyDatastoreAndView()

        val event = NewStudentRegistered("STUDENT001", "test@ema.il", "Full Name", 42L)
        sut.handle(event)

        verify(datastore).save(eq(event.studentId), com.nhaarman.mockitokotlin2.check {
            assertThat(it.studentId).isEqualTo(event.studentId)
            assertThat(it.email).isEqualTo(event.email)
            assertThat(it.fullName).isEqualTo(event.fullName)
            assertThat(it.version).isEqualTo(event.version)
        })
        verifyNoMoreInteractions(datastore)
    }

}

private fun givenEmptyDatastoreAndView(): Pair<Datastore<StudentDTO>, StudentView> {
    val datastore = mock<Datastore<StudentDTO>>()
    return Pair(datastore, StudentView(datastore))
}
