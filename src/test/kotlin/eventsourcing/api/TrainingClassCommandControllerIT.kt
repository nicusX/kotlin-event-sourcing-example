package eventsourcing.api

import arrow.core.Left
import arrow.core.Right
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.*
import eventsourcing.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.core.StringEndsWith.endsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
internal class TrainingClassCommandControllerIT() {

    lateinit var mvc : MockMvc
    lateinit var dispatcher: CommandDispatcher

    @BeforeEach
    fun setup() {
        dispatcher = mock<CommandDispatcher>()
        mvc = MockMvcBuilders.standaloneSetup(TrainingClassCommandController(dispatcher)).build()
    }

    private val aClassId = "CLASS001"

    @Test
    fun `given a POST to Schedule New Class endpoint, when command handling succeeds, then it returns 202 ACCEPTED with Class Location header`() {
        whenever(dispatcher.handle(any<ScheduleNewClass>()))
                .thenReturn(Right(ScheduleNewClassSuccess(aClassId)))

        val request = ScheduleNewClassRequest("Class Title", LocalDate.now(), 10)
        mvc.perform(post("/classes/schedule_new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.asJsonString()))
                .andExpect(status().isAccepted)
                .andExpect(header().string("Location", endsWith("/classes/$aClassId")))

        verify(dispatcher).handle( check<ScheduleNewClass> {
            assertThat(it.title).isEqualTo( request.title )
            assertThat(it.date).isEqualTo( request.date)
            assertThat(it.size).isEqualTo( request.size)
        })
        verifyNoMoreInteractions(dispatcher)
    }



    private val studentId = "STUDENT-42"
    private val enrollStudentRequest = EnrollStudentRequest(studentId, 0L)

    @Test
    fun `given a POST to Enroll Student endpoint, when command handling succeeds, then it returns 202 ACCEPTED with class Location header`() {
        whenever(dispatcher.handle(any<EnrollStudent>()))
                .thenReturn(Right(EnrollStudentSuccess))

        mvc.perform(post("/classes/$aClassId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isAccepted)
                .andExpect(header().string("Location", endsWith("/classes/$aClassId")))

        verify(dispatcher).handle(check<EnrollStudent>{
            assertThat(it.classId).isEqualTo(aClassId)
            assertThat(it.studentId).isEqualTo(enrollStudentRequest.studentId)
            assertThat(it.originalVersion).isEqualTo(enrollStudentRequest.classVersion)
        })
        verifyNoMoreInteractions(dispatcher)
    }

    @Test
    fun `given a POST to Enroll Student endpoint, when command handling fails because the Class does not exist, then it returns 404 NOT FOUND`(){
        whenever(dispatcher.handle(any<EnrollStudent>()))
                .thenReturn(Left(AggregateNotFound))

        mvc.perform(post("/classes/$aClassId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `given a POST to Enroll Student endpoint, when command handling fails because of insufficient spots in the class, then it returns 422 UNPROCESSABLE ENTITY and a JSON body containing the error`() {
        whenever(dispatcher.handle(any<EnrollStudent>()))
                .thenReturn(Left(TrainingClassInvariantViolation.ClassHasNoAvailableSpots))

        mvc.perform(post("/classes/$aClassId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("No available spots"))
    }

    @Test
    fun `given a POST to Enroll Student endpoint, when command handling fails because the student is already enrolled, then it returns 422 UNPROCESSABLE ENTITY and a JSON body containing the error`(){
        whenever(dispatcher.handle(any<EnrollStudent>()))
                .thenReturn(Left(TrainingClassInvariantViolation.StudentAlreadyEnrolled))

        mvc.perform(post("/classes/$aClassId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("Student already enrolled"))
    }

    @Test
    fun `given a POST to Enroll Student endpoint, when command handling fails for a concurrency issue, then it returns 409 CONFLICT and a JSON body containing the error`(){
        whenever(dispatcher.handle(any<EnrollStudent>()))
                .thenReturn(Left(EventStoreProblem.ConcurrentChangeDetected))


        mvc.perform(post("/classes/$aClassId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("Concurrent change detected"))
    }


    val unenrollStudentRequest = UnenrollStudentRequest(studentId, "some reasons", 1L)

    @Test
    fun `given a POST to Unenroll Student endpoint, when command handling succeeds, then it returns 202 ACCEPTED with class Location header`() {
        whenever(dispatcher.handle(any<UnenrollStudent>()))
                .thenReturn(Right(UnenrollStudentSuccess))

        mvc.perform(post("/classes/$aClassId/unenroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unenrollStudentRequest.asJsonString()))
                .andExpect(status().isAccepted)
                .andExpect(header().string("Location", endsWith("/classes/$aClassId")))

        verify(dispatcher).handle(check<UnenrollStudent>{
            assertThat(it.classId).isEqualTo(aClassId)
            assertThat(it.studentId).isEqualTo(unenrollStudentRequest.studentId)
            assertThat(it.originalVersion).isEqualTo(unenrollStudentRequest.classVersion)
        })
        verifyNoMoreInteractions(dispatcher)

    }

    @Test
    fun `given a POST to Unenroll Student endpoint, when command handling fails because the Class does not exist, then it returns 404 NOT FOUND`(){
        whenever(dispatcher.handle(any<UnenrollStudent>()))
                .thenReturn(Left(AggregateNotFound))

        mvc.perform(post("/classes/$aClassId/unenroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unenrollStudentRequest.asJsonString()))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `given a POST to Unenroll Student endpoint, when command handling fails because the student is not enrolled, then it returns 422 UNPROCESSABLE ENTITY and a JSON body containing the error`(){
        whenever(dispatcher.handle(any<UnenrollStudent>()))
                .thenReturn(Left(TrainingClassInvariantViolation.UnenrollingNotEnrolledStudent))

        mvc.perform(post("/classes/$aClassId/unenroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unenrollStudentRequest.asJsonString()))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("Student not enrolled"))
    }

    @Test
    fun `given POST to Unenroll Student endpoint, when command handling fails for a concurrency issue, then it returns 409 CONFLICT and a JSON body containing the error`(){
        whenever(dispatcher.handle(any<UnenrollStudent>()))
                .thenReturn(Left(EventStoreProblem.ConcurrentChangeDetected))

        mvc.perform(post("/classes/$aClassId/unenroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unenrollStudentRequest.asJsonString()))
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("Concurrent change detected"))
    }

    companion object {
        private val mapper = ObjectMapper().findAndRegisterModules()
        fun Any.asJsonString(): String = mapper.writeValueAsString(this)
    }
}
