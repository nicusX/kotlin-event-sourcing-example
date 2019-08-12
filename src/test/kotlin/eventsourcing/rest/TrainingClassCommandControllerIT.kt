package eventsourcing.rest

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
    lateinit var handler: TrainingClassCommandHandler

    @BeforeEach
    fun setup() {
        handler = mock<TrainingClassCommandHandler>()
        mvc = MockMvcBuilders.standaloneSetup(TrainingClassCommandController(handler)).build()
    }

    private val classId = "CLASS001"

    @Test
    fun `given a Schedule New Class API request, when ScheduleNewClass command is successfully handled, then it returns 202 ACCEPTED with class Location header`() {
        whenever(handler.handle(any<ScheduleNewClass>()))
                .thenReturn(ScheduleNewClassSuccess(classId))

        val request = ScheduleNewClassRequest("Class Title", LocalDate.now(), 10)
        mvc.perform(post("/classes/schedule_new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.asJsonString()))
                .andExpect(status().isAccepted)
                .andExpect(header().string("Location", endsWith("/classes/$classId")))

        verify(handler).handle( check<ScheduleNewClass> {
            assertThat(it.title).isEqualTo( request.title )
            assertThat(it.date).isEqualTo( request.date)
            assertThat(it.size).isEqualTo( request.size)
        })
        verifyNoMoreInteractions(handler)
    }



    private val studentId = "STUDENT-42"
    private val enrollStudentRequest = EnrollStudentRequest(studentId, 0L)

    @Test
    fun `given an Enroll Student request, when EnrollStudent command is successfully handled, then it returns 202 ACCEPTED with class Location header`() {
        whenever(handler.handle(any<EnrollStudent>()))
                .thenReturn(EnrollStudentSuccess)

        mvc.perform(post("/classes/$classId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isAccepted)
                .andExpect(header().string("Location", endsWith("/classes/$classId")))

        verify(handler).handle(check<EnrollStudent>{
            assertThat(it.classId).isEqualTo(classId)
            assertThat(it.studentId).isEqualTo(enrollStudentRequest.studentId)
            assertThat(it.originalVersion).isEqualTo(enrollStudentRequest.classVersion)
        })
        verifyNoMoreInteractions(handler)
    }

    @Test
    fun `given an Enroll Student request, when processing the command fails because the Class does not exist, then it returns 404 NOT FOUND`(){
        whenever(handler.handle(any<EnrollStudent>()))
                .thenAnswer{ throw AggregateNotFoundException(TrainingClassAggregateType, classId) }

        mvc.perform(post("/classes/$classId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `given an Enroll Student request, when processing the command fails because of insufficient spots in the class, then it returns 422 UNPROCESSABLE ENTITY and a JSON body containing the error`() {
        whenever(handler.handle(any<EnrollStudent>()))
                .thenAnswer{ throw NoAvailableSpotsException(classId) }

        mvc.perform(post("/classes/$classId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("No available spots"))
    }

    @Test
    fun `given an Enroll Student request, when processing the command fails because the student is already enrolled, returns 422 UNPROCESSABLE ENTITY and a JSON body containing the error`(){
        whenever(handler.handle(any<EnrollStudent>()))
                .thenAnswer{ throw StudentAlreadyEnrolledException(studentId, classId) }

        mvc.perform(post("/classes/$classId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("Student already enrolled"))
    }

    @Test
    fun `given an Enroll Student request, when processing the command fails for a concurrency issue, then it returns 409 CONFLICT and a JSON body containing the error`(){
        whenever(handler.handle(any<EnrollStudent>()))
                .thenAnswer{ throw ConcurrencyException(TrainingClassAggregateType, classId, 0, 1) }

        mvc.perform(post("/classes/$classId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("Concurrent change detected"))
    }


    val unenrollStudentRequest = UnenrollStudentRequest(studentId, 1L)

    @Test
    fun `given and Unenroll Student request, when the command is successfully processed, then it returns 202 ACCEPTED with class Location header`() {
        whenever(handler.handle(any<UnenrollStudent>()))
                .thenReturn(UnenrollStudentSuccess)

        mvc.perform(post("/classes/$classId/unenroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unenrollStudentRequest.asJsonString()))
                .andExpect(status().isAccepted)
                .andExpect(header().string("Location", endsWith("/classes/$classId")))

        verify(handler).handle(check<UnenrollStudent>{
            assertThat(it.classId).isEqualTo(classId)
            assertThat(it.studentId).isEqualTo(unenrollStudentRequest.studentId)
            assertThat(it.originalVersion).isEqualTo(unenrollStudentRequest.classVersion)
        })
        verifyNoMoreInteractions(handler)

    }

    @Test
    fun `given an Unenroll Student request, when processing the command fails because the Class does not exist, then it returns 404 NOT FOUND`(){
        whenever(handler.handle(any<UnenrollStudent>()))
                .thenAnswer{ throw AggregateNotFoundException(TrainingClassAggregateType, classId) }

        mvc.perform(post("/classes/$classId/unenroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unenrollStudentRequest.asJsonString()))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `given an Unenroll Student request, when processing the command fails because the student is not enrolled, then it returns 422 UNPROCESSABLE ENTITY and a JSON body containing the error`(){
        whenever(handler.handle(any<UnenrollStudent>()))
                .thenAnswer{ throw StudentNotEnrolledException(studentId, classId) }

        mvc.perform(post("/classes/$classId/unenroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unenrollStudentRequest.asJsonString()))
                .andExpect(status().isUnprocessableEntity)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("Student not enrolled"))
    }

    @Test
    fun `given an Unenroll Student request, when processing the command fails for a concurrency issue, then it returns 409 CONFLICT and a JSON body containing the error`(){
        whenever(handler.handle(any<UnenrollStudent>()))
                .thenAnswer{ throw ConcurrencyException(TrainingClassAggregateType, classId, 0, 1) }

        mvc.perform(post("/classes/$classId/unenroll_student")
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
