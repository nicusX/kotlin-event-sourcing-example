package eventsourcing.api

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import eventsourcing.readmodels.studentlist.Student
import eventsourcing.readmodels.studentdetails.StudentDetails
import eventsourcing.readmodels.studentdetails.StudentDetailsReadModel
import eventsourcing.readmodels.studentlist.StudentListReadModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

@ExtendWith(SpringExtension::class)
internal class StudentReadControllerIT {
    lateinit var mvc : MockMvc
    lateinit var studentDetailsReadModel : StudentDetailsReadModel
    lateinit var studentListReadModel : StudentListReadModel

    @BeforeEach
    fun setup() {
        studentDetailsReadModel = mock<StudentDetailsReadModel>()
        studentListReadModel = mock<StudentListReadModel>()
        mvc = MockMvcBuilders.standaloneSetup(StudentReadController(studentDetailsReadModel, studentListReadModel)).build()
    }

    @Test
    fun `when I hit the GET Student endpoint with the Student ID, then it returns the Student representation in JSON`() {
        whenever(studentDetailsReadModel.getStudentById(eq("STUDENT001"))).thenReturn(aStudentDetails)

        mvc.perform(MockMvcRequestBuilders.get("/students/STUDENT001").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("""$.studentId""").value(aStudentDetails.studentId))
                .andExpect(MockMvcResultMatchers.jsonPath("""$.version""").value(aStudentDetails.version))
    }

    @Test
    fun `when I hit the GET Student endpoint with a non-existing Student ID, then it returns 404`() {
        whenever(studentDetailsReadModel.getStudentById(eq("DO-NOT-EXISTS"))).thenReturn( null )

        mvc.perform(MockMvcRequestBuilders.get("/classes/001").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `when I hit the GET all Students, then it returns a JSON representation of a list containing all Students`() {
        whenever(studentListReadModel.allStudents()).thenReturn( listOf(aStudent, anotherStudent))

        mvc.perform(MockMvcRequestBuilders.get("/students").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("""$.[0].studentId""").value(aStudent.studentId))
                .andExpect(MockMvcResultMatchers.jsonPath("""$.[1].studentId""").value(anotherStudent.studentId))
    }
}

private val aStudentDetails = StudentDetails(
        studentId = "STUDENT001",
        email = "test@ema.il",
        fullName = "Full Name",
        version = 0L
)

private val anotherStudentDetails = StudentDetails(
        studentId = "STUDENT002",
        email = "test2@ema.il",
        fullName = "Another Name",
        version = 2L
)

private val aStudent = Student(aStudentDetails.studentId, aStudentDetails.fullName)

private val anotherStudent = Student(anotherStudentDetails.studentId, anotherStudentDetails.fullName)
