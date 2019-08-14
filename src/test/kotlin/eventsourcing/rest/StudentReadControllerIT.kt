package eventsourcing.rest

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import eventsourcing.readmodels.RecordNotFound
import eventsourcing.readmodels.StudentDTO
import eventsourcing.readmodels.StudentView
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
internal class StudentReadControllerIT {
    lateinit var mvc : MockMvc
    lateinit var studentView: StudentView

    @BeforeEach
    fun setup() {
        studentView = mock<StudentView>()
        mvc = MockMvcBuilders.standaloneSetup(StudentReadController(studentView))
                .build()
    }

    @Test
    fun `when I hit the GET Student endpoint with the Student ID, then it returns the Student representation in JSON`() {
        whenever(studentView.getStudentById(eq("STUDENT001"))).thenReturn(aStudentDTO)

        mvc.perform(MockMvcRequestBuilders.get("/students/STUDENT001").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("""$.studentId""").value(aStudentDTO.studentId))
                .andExpect(MockMvcResultMatchers.jsonPath("""$.version""").value(aStudentDTO.version))
    }

    @Test
    fun `when I hit the GET Student endpoint with a non-existing Student ID, then it returns 404`() {
        whenever(studentView.getStudentById(eq("DO-NOT-EXISTS")))
                .thenAnswer{ throw RecordNotFound("DO-NOT-EXISTS") } // .thenThrows does not work as RecordNotFound is a checked exception


        mvc.perform(MockMvcRequestBuilders.get("/classes/001").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `when I hit the GET all Students, then it returns a JSON representation of a list containing all Students`() {
        whenever(studentView.listStudents()).thenReturn( listOf(aStudentDTO, anotherStudentDTO))

        mvc.perform(MockMvcRequestBuilders.get("/students").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("""$.[0].studentId""").value(aStudentDTO.studentId))
                .andExpect(MockMvcResultMatchers.jsonPath("""$.[1].studentId""").value(anotherStudentDTO.studentId))
    }
}

private val aStudentDTO = StudentDTO(
        studentId = "STUDENT001",
        email = "test@ema.il",
        fullName = "Full Name",
        version = 0L
)

private val anotherStudentDTO = StudentDTO(
        studentId = "STUDENT002",
        email = "test2@ema.il",
        fullName = "Another Name",
        version = 2L
)
