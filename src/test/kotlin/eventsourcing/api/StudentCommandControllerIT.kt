package eventsourcing.api

import com.nhaarman.mockitokotlin2.*
import eventsourcing.api.TrainingClassCommandControllerIT.Companion.asJsonString
import eventsourcing.domain.RegisterNewStudent
import eventsourcing.domain.RegisterNewStudentSuccess
import org.assertj.core.api.Assertions
import org.hamcrest.core.StringEndsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(SpringExtension::class)
internal class StudentCommandControllerIT() {
    lateinit var mvc : MockMvc
    lateinit var dispatcher: CommandDispatcher

    @BeforeEach
    fun setup() {
        dispatcher = mock<CommandDispatcher>()
        mvc = MockMvcBuilders.standaloneSetup(StudentCommandController(dispatcher)).build()
    }

    private val aStudentId = "STUDENT-42"

    @Test
    fun `given a POST to Register new Student endpoint, when command processing succeeds, then it returns 202 ACCEPTED with Student Location header`() {
        whenever(dispatcher.handle(any<RegisterNewStudent>()))
                .thenReturn(RegisterNewStudentSuccess(aStudentId))

        val request = RegisterNewStudentRequest("test@ema.il", "Full Name")
        mvc.perform(MockMvcRequestBuilders.post("/students/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.asJsonString()))
                .andExpect(MockMvcResultMatchers.status().isAccepted)
                .andExpect(MockMvcResultMatchers.header().string("Location", StringEndsWith.endsWith("/students/$aStudentId")))

        verify(dispatcher).handle( check<RegisterNewStudent> {
            Assertions.assertThat(it.email).isEqualTo( request.email )
            Assertions.assertThat(it.fullName).isEqualTo( request.fullName)
        })
        verifyNoMoreInteractions(dispatcher)
    }
}
