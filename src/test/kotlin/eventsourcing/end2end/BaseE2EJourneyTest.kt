package eventsourcing.end2end

import eventsourcing.Retry.retryOnAssertionFailure
import eventsourcing.api.EnrollStudentRequest
import eventsourcing.api.RegisterNewStudentRequest
import eventsourcing.api.ScheduleNewClassRequest
import eventsourcing.api.UnenrollStudentRequest
import eventsourcing.readmodels.studentdetails.StudentDetails
import eventsourcing.readmodels.trainingclasses.TrainingClassDetails
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.annotation.DirtiesContext
import java.net.URI
import java.time.LocalDate
import org.apache.commons.lang3.RandomStringUtils

@DirtiesContext // E2E tests change the state of event-store and read-models
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal abstract class BaseE2EJourneyTest(val template: TestRestTemplate) {

    // TODO Configure the application to apply simulated latency in the message bus

    // Because read models are eventually consistent, all GET operations retries if they initially fail
    // You would not probably notice any error when running on a local machine, as the latency is negligible. But if you
    // amplify the latency (set a `simulateLatency` in AsyncInMemoryBus, you will start observing delay, as in a real
    // system, and these E2E tests will start randomly failing, getting a 404 instead of 200 on reads to read models

    // Retries on assertion errors
    val maxRetries = 30
    val retryDelay = 100L

    protected fun listClasses_isOk(): Int = retryOnAssertionFailure(maxRetries, retryDelay) {
        return template.assertThatApiGet<List<Any>>("/classes")
                .returnsStatusCode(HttpStatus.OK)
                .extractBody().size
    }


    protected fun listClasses_isOk_withNofClasses(expectedNofClasses: Int): Int {
        val size = listClasses_isOk()
        assertThat(size).isEqualTo(expectedNofClasses)
        return size
    }

    protected fun listStudents_isOk(): Int = retryOnAssertionFailure(maxRetries, retryDelay) {
        return template.assertThatApiGet<List<Any>>("/students")
                .returnsStatusCode(HttpStatus.OK)
                .extractBody().size
    }

    protected fun listStudents_isOk_withNofStudents(expectedNofStudents: Int): Int {
        val size = listStudents_isOk()
        assertThat(size).isEqualTo(expectedNofStudents)
        return size
    }

    protected fun registerStudent_withEmail_isAccepted(email: String): URI =
            template.assertThatApiPost<Any, RegisterNewStudentRequest>("/students/register",
                    RegisterNewStudentRequest(
                            email = email,
                            fullName = RandomStringUtils.randomAlphabetic(10, 20)))
                    .returnsStatusCode(HttpStatus.ACCEPTED)
                    .extractLocation()

    protected fun registerStudent_withEmail_isRejectedWith422(email: String) {
        template.assertThatApiPost<Any, RegisterNewStudentRequest>("/students/register",
                RegisterNewStudentRequest(
                        email = email,
                        fullName = RandomStringUtils.randomAlphabetic(10, 20)))
                .returnsStatusCode(HttpStatus.UNPROCESSABLE_ENTITY)
    }

    protected fun scheduleNewClass_withSize_isAccepted(size: Int): URI =
            template.assertThatApiPost<Any, ScheduleNewClassRequest>("/classes/schedule_new",
                    ScheduleNewClassRequest(
                            title = "Class title",
                            date = LocalDate.now(),
                            size = size))
                    .returnsStatusCode(HttpStatus.ACCEPTED)
                    .extractLocation()

    protected fun getClass_isOK_withVersion(classUri: URI, expectedVersion: Long): TrainingClassDetails = retryOnAssertionFailure(maxRetries, retryDelay) {
        return template.assertThatApiGet<TrainingClassDetails>(classUri)
                .returnsStatusCode(HttpStatus.OK)
                .returnsClassWithVersion(expectedVersion)
                .extractBody()
    }

    protected fun getClass_isOk_withVersion_andWithStudents(classUri: URI, expectedVersion: Long, vararg studentIds: String): TrainingClassDetails {
        val clazz = getClass_isOK_withVersion(classUri, expectedVersion)
        for (studentId in studentIds)
            Assertions.assertThat(clazz.students)
                    .extracting("studentId")
                    .contains(studentId)
        return clazz
    }

    protected fun getStudent_isOk_withVersion(studentUri: URI, expectedVersion: Long): StudentDetails = retryOnAssertionFailure(maxRetries, retryDelay) {
        return template.assertThatApiGet<StudentDetails>(studentUri)
                .returnsStatusCode(HttpStatus.OK)
                .returnsStudentWithVersion(expectedVersion)
                .extractBody()
    }


    protected fun enrollStudent_isAccepted(classUri: URI, student: String, classVersion: Long) {
        template.assertThatApiPost<Any, EnrollStudentRequest>("$classUri/enroll_student",
                EnrollStudentRequest(
                        studentId = student,
                        classVersion = classVersion))
                .returnsStatusCode(HttpStatus.ACCEPTED)
    }

    protected fun enrollStudent_isRejectedWith409(classUri: URI, student: String, wrongClassVersion: Long) {
        template.assertThatApiPost<Any, EnrollStudentRequest>("$classUri/enroll_student",
                EnrollStudentRequest(
                        studentId = student,
                        classVersion = wrongClassVersion))
                .returnsStatusCode(HttpStatus.CONFLICT)
    }

    protected fun enrollStudent_isRejectedWith422(classUri: URI, student: String, classVersion: Long) {
        template.assertThatApiPost<Any, EnrollStudentRequest>("$classUri/enroll_student",
                EnrollStudentRequest(
                        studentId = student,
                        classVersion = classVersion))
                .returnsStatusCode(HttpStatus.UNPROCESSABLE_ENTITY)
    }

    protected fun unenrollStudent_isAccepted(classUri: URI, student: String, classVersion: Long) {
        template.assertThatApiPost<Any, UnenrollStudentRequest>("$classUri/unenroll_student", UnenrollStudentRequest(
                studentId = student,
                reason = "some reasons",
                classVersion = classVersion))
                .returnsStatusCode(HttpStatus.ACCEPTED)
    }

    protected fun unenrollStudent_isRejectedWith422(classUri: URI, student: String, classVersion: Long) {
        template.assertThatApiPost<Any, UnenrollStudentRequest>("$classUri/unenroll_student", UnenrollStudentRequest(
                studentId = student,
                reason = "some reasons",
                classVersion = classVersion))
                .returnsStatusCode(HttpStatus.UNPROCESSABLE_ENTITY)
    }


    private class ApiAssert<E>(actual: ResponseEntity<E>) : AbstractAssert<ApiAssert<E>, ResponseEntity<E>>(actual, ApiAssert::class.java) {

        fun returnsStatusCode(expectedStatus: HttpStatus): ApiAssert<E> {
            Assertions.assertThat(actual.statusCode).isEqualTo(expectedStatus)
            return this
        }

        fun returnsBodyWithListOfSize(expectedSize: Int): ApiAssert<E> {
            Assertions.assertThat(actual.body as List<*>).hasSize(expectedSize)
            return this
        }

        fun returnsClassWithVersion(expectedVersion: Long): ApiAssert<E> {
            Assertions.assertThat((actual.body as TrainingClassDetails).version).isEqualTo(expectedVersion)
            return this
        }

        fun returnsStudentWithVersion(expectedVersion: Long): ApiAssert<E> {
            Assertions.assertThat((actual.body as StudentDetails).version).isEqualTo(expectedVersion)
            return this
        }

        fun extractLocation(): URI = actual.headers.location!!

        fun extractBody(): E = actual.body!!
    }

    private inline fun <reified E> TestRestTemplate.assertThatApiGet(uri: URI): ApiAssert<E> =
            ApiAssert(this.getForEntity(uri, E::class.java))

    private inline fun <reified E> TestRestTemplate.assertThatApiGet(uri: String): ApiAssert<E> =
            this.assertThatApiGet(URI(uri))

    private inline fun <reified E, R> TestRestTemplate.assertThatApiPost(uri: URI, requestBody: R): ApiAssert<E> =
            ApiAssert(this.postForEntity(uri, requestBody, E::class.java))

    private inline fun <reified E, R> TestRestTemplate.assertThatApiPost(uri: String, requestBody: R): ApiAssert<E> =
            assertThatApiPost(URI(uri), requestBody)
}
