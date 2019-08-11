package eventsourcing.rest

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import eventsourcing.readmodel.InMemoryDatastore
import eventsourcing.readmodel.TrainingClassDTO
import eventsourcing.readmodel.TrainingClassView
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import java.time.LocalDate


@SpringBootTest(webEnvironment  = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class TrainingClassReadControllerIT(@Autowired var restTemplate: TestRestTemplate) {

    // FIXME change the way we test the controller, to be able to test error handlers too

    @MockBean
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

    @Test
    fun `get an existing Class by ID should return the class representation in JSON`() {
        whenever(trainingClassView.getById(eq("001"))).thenReturn(aClassDTO)

        val res = restTemplate.getForEntity("/classes/001", TrainingClassDTO::class.java)
        assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(res.body?.classId).isEqualTo(aClassDTO.classId)
        assertThat(res.body?.version).isEqualTo(aClassDTO.version)
    }

    @Test
    fun `list Classes should return all `() {
        whenever(trainingClassView.list()).thenReturn( listOf(aClassDTO, anotherClassDTO)  )

        val res = restTemplate.getForEntity("/classes", List::class.java)
        assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(res.body).hasSize(2)
    }


}

private fun InMemoryDatastore<TrainingClassDTO>.init(vararg classes : TrainingClassDTO) {
    this.clear()
    for(clazz in classes)
        this.save(clazz.classId, clazz)
}
