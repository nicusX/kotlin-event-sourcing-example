package eventsourcing.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.*
import eventsourcing.domain.*
import eventsourcing.readmodel.RecordNotFound
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import java.time.LocalDate
import org.hamcrest.core.StringEndsWith.endsWith
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

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
    fun `scheduling a new class triggers a ScheduleNewClass command and returns 202 ACCEPTED with class Location header`() {
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
    fun `enrolling a student triggers an EnrollStudent command and, when successful, returns 202 ACCEPTED with class Location header`() {
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
    fun `enrolling a student, when it fails for class not found, returns 404 NOT FOUND`(){
        whenever(handler.handle(any<EnrollStudent>()))
                .thenAnswer{ throw RecordNotFound(classId) }

        mvc.perform(post("/classes/$classId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `enrolling a student, when it fails for insufficient spots in the class, returns 409 CONFLICT and a JSON body containing the error`() {
        whenever(handler.handle(any<EnrollStudent>()))
                .thenAnswer{ throw NoAvailableSpotsException(classId) }

        mvc.perform(post("/classes/$classId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("No available spots"))
    }

    @Test
    fun `enrolling a student, when it fails for the student is already enrolled, returns 409 CONFLICT and a JSON body containing the error`(){
        whenever(handler.handle(any<EnrollStudent>()))
                .thenAnswer{ throw StudentAlreadyEnrolledException(studentId, classId) }

        mvc.perform(post("/classes/$classId/enroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollStudentRequest.asJsonString()))
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("Student already enrolled"))
    }




    val unenrollStudentRequest = UnenrollStudentRequest(studentId, 1L)

    @Test
    fun `unenrolling a student triggers an UnenrollStudent command and, when successful, returns 202 ACCEPTED with class Location header`() {
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
    fun `unenrolling a student, when it fails for class not found, returns 404 NOT FOUND`(){
        whenever(handler.handle(any<UnenrollStudent>()))
                .thenAnswer{ throw RecordNotFound(classId) }

        mvc.perform(post("/classes/$classId/unenroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unenrollStudentRequest.asJsonString()))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `unenrolling a student, when it fails for the student is not enrolled, returns 409 CONFLICT and a JSON body containing the error`(){
        whenever(handler.handle(any<UnenrollStudent>()))
                .thenAnswer{ throw StudentNotEnrolledException(studentId, classId) }

        mvc.perform(post("/classes/$classId/unenroll_student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(unenrollStudentRequest.asJsonString()))
                .andExpect(status().isConflict)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.message""").value("Student not enrolled"))
    }

    companion object {
        private val mapper = ObjectMapper().findAndRegisterModules()
        fun Any.asJsonString(): String = mapper.writeValueAsString(this)
    }
}

