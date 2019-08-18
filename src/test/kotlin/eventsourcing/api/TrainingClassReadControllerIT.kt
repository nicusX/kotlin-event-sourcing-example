package eventsourcing.api

import arrow.core.None
import arrow.core.Some
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import eventsourcing.readmodels.trainingclasses.EnrolledStudent
import eventsourcing.readmodels.trainingclasses.TrainingClass
import eventsourcing.readmodels.trainingclasses.TrainingClassDetails

import eventsourcing.readmodels.trainingclasses.TrainingClassReadModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate
import java.util.*


@ExtendWith(SpringExtension::class)
internal class TrainingClassReadControllerIT {

    lateinit var mvc : MockMvc
    lateinit var trainingClassReadModel: TrainingClassReadModel

    @BeforeEach
    fun setup() {
        trainingClassReadModel = mock<TrainingClassReadModel>()
        mvc = MockMvcBuilders.standaloneSetup(TrainingClassReadController(trainingClassReadModel))
                .build()
    }

    @Test
    fun `when I hit the GET Class endpoint with the class ID, then it returns the class representation in JSON`() {
        whenever(trainingClassReadModel.getTrainingClassDetailsById(eq("001"))).thenReturn( Some(aClassDetails) )

        mvc.perform(get("/classes/001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.classId""").value(aClassDetails.classId))
                .andExpect(jsonPath("""$.version""").value(aClassDetails.version))
    }

    @Test
    fun `when I hit the GET Class endpoint with a non-existing Class ID, then it returns 404`() {
        whenever(trainingClassReadModel.getTrainingClassDetailsById(eq("001"))).thenReturn(None)


        mvc.perform(get("/classes/001").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound)
    }

    @Test
    fun `when I hit the GET all Classes, then it returns a JSON representation of a list containing all Classes`() {
        whenever(trainingClassReadModel.allClasses()).thenReturn( listOf(aClass, anotherClass))

        mvc.perform(get("/classes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("""$.[0].classId""").value(aClass.classId))
                .andExpect(jsonPath("""$.[1].classId""").value(anotherClass.classId))
    }
}

private val aClassDetails = TrainingClassDetails(
        classId = "001",
        title = "Class title",
        date = LocalDate.now(),
        totalSize = 10,
        availableSpots = 9,
        students = listOf( EnrolledStudent("STUDENT-001", "email","test1@ema.il") ),
        version = 47L)

private val aClass = TrainingClass(
        classId = aClassDetails.classId,
        title = aClassDetails.title,
        date = aClassDetails.date)

private val anotherClassDetails = TrainingClassDetails(
        classId = "002",
        title = "Another class title",
        date = LocalDate.now(),
        totalSize = 15,
        availableSpots = 13,
        students = listOf(
                EnrolledStudent("STUDENT-001", "email","test1@ema.il"),
                EnrolledStudent("STUDENT-002", "email","test2@ema.il")),
        version = 3L)

private val anotherClass = TrainingClass(
        classId = anotherClassDetails.classId,
        title = anotherClassDetails.title,
        date = anotherClassDetails.date)