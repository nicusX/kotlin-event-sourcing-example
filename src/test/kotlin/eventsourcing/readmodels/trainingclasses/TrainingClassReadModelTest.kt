package eventsourcing.readmodels.trainingclasses

import com.nhaarman.mockitokotlin2.*
import eventsourcing.readmodels.DocumentStore
import eventsourcing.readmodels.SingleDocumentStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class TrainingClassReadModelTest {
    lateinit var trainingClassDetailsStore: DocumentStore<TrainingClassDetails>
    lateinit var trainingClassListStore: SingleDocumentStore<TrainingClassList>

    @BeforeEach
    fun before() {
        trainingClassDetailsStore = mock<DocumentStore<TrainingClassDetails>>()
        trainingClassListStore = mock<SingleDocumentStore<TrainingClassList>>()
    }

    @Test
    fun `given a Training Class read model, when I get an existing Training Class Details by Id, then it returns the class`() {
        val sut = givenTrainingClassReadModel()

        whenever(trainingClassDetailsStore.get(any())).thenReturn(aTrainingClassDetails)

        val result = sut.getTrainingClassDetailsById(aTrainingClassDetails.classId)

        assertThat(result).isEqualTo(aTrainingClassDetails)

        verify(trainingClassDetailsStore).get(eq(aTrainingClassDetails.classId))

        verifyNoMoreInteractions(trainingClassDetailsStore)
        verifyNoMoreInteractions(trainingClassListStore)
    }

    @Test
    fun `given a Training Class read model, when I get a non existing Training Class Details, then it returns an empty result`() {
        val sut = givenTrainingClassReadModel()

        whenever(trainingClassDetailsStore.get(any())).thenReturn(null)

        val result = sut.getTrainingClassDetailsById("NON-EXISTING-CLASS")

        assertThat(result).isNull()

        verify(trainingClassDetailsStore).get(eq("NON-EXISTING-CLASS"))

        verifyNoMoreInteractions(trainingClassDetailsStore)
        verifyNoMoreInteractions(trainingClassListStore)
    }

    @Test
    fun `given a Training Class read model containing no Class, when I retrieve all Classes, then I get an empty list`() {
        val sut = givenTrainingClassReadModel()
        whenever(trainingClassListStore.get()).thenReturn(emptyList())

        val result = sut.allClasses()
        assertThat(result).isEmpty()

        verify(trainingClassListStore).get()

        verifyNoMoreInteractions(trainingClassDetailsStore)
        verifyNoMoreInteractions(trainingClassListStore)
    }

    @Test
    fun `given a Training Class read model containing some Classes, when I retrieve all Classes, then I get a list with all Classes`() {
        val sut = givenTrainingClassReadModel()

        val aClass = TrainingClass("CLASS001", "First Class", LocalDate.now())
        val anotherClass = TrainingClass("CLASS002", "Second Class", LocalDate.now())
        whenever(trainingClassListStore.get()).thenReturn(listOf(aClass, anotherClass))


        val result = sut.allClasses()
        assertThat(result).hasSize(2)
        assertThat(result.first()).isEqualTo(aClass)
        assertThat(result.last()).isEqualTo(anotherClass)
    }

    private fun givenTrainingClassReadModel() =
            TrainingClassReadModel(trainingClassDetailsStore, trainingClassListStore)
}

val aTrainingClassDetails = TrainingClassDetails(
        classId = "CLASS001",
        title = "Class Title",
        date = LocalDate.now(),
        totalSize = 10,
        availableSpots = 0,
        students = listOf(EnrolledStudent("STUDENT042", "email", "test@ema.il")),
        version = 1L)
