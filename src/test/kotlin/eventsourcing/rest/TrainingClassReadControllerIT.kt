package eventsourcing.rest

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import eventsourcing.readmodel.InMemoryDatastore
import eventsourcing.readmodel.RecordNotFound
import eventsourcing.readmodel.TrainingClassDTO
import eventsourcing.readmodel.TrainingClassView
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@ExtendWith(SpringExtension::class)
internal class TrainingClassReadControllerIT() {

    lateinit var mvc : MockMvc
    lateinit var trainingClassView : TrainingClassView

    private val aClassDTO = TrainingClassDTO(
            classId = "001",
            title = "Class title",
            date = LocalDate.now(),
            totalSize = 10,
            availableSpots = 9,
            students = listOf("STUDENT-001"),
            version = 47L)

    private val anotherClassDTO = TrainingClassDTO(
            classId = "002",
            title = "Another class title",
            date = LocalDate.now(),
            totalSize = 15,
            availableSpots = 13,
            students = listOf("STUDENT-001", "STUDENT-002"),
            version = 3L)

    @BeforeEach
    fun setup() {
        trainingClassView = mock<TrainingClassView>()
        mvc = MockMvcBuilders.standaloneSetup(TrainingClassReadController(trainingClassView))
                .build()
    }

    @Test
    fun `get an existing Class by ID should return the class representation in JSON`() {
        whenever(trainingClassView.getById(eq("001"))).thenReturn(aClassDTO)

        mvc.perform(get("/classes/001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.classId""").value(aClassDTO.classId))
                .andExpect(jsonPath("""$.version""").value(aClassDTO.version))
    }

    @Test
    fun `get a non-existing Class by ID should return 404`() {
        whenever(trainingClassView.getById(eq("001")))
                .thenAnswer{ throw RecordNotFound("001") } // .thenThrows does not work as RecordNotFound is a checked exception


        mvc.perform(get("/classes/001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `list Classes should return all `() {
        whenever(trainingClassView.list()).thenReturn( listOf(aClassDTO, anotherClassDTO))

        mvc.perform(get("/classes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.[0].classId""").value(aClassDTO.classId))
                .andExpect(jsonPath("""$.[1].classId""").value(anotherClassDTO.classId))
    }


}

private fun InMemoryDatastore<TrainingClassDTO>.init(vararg classes : TrainingClassDTO) {
    this.clear()
    for(clazz in classes)
        this.save(clazz.classId, clazz)
}