package eventsourcing.end2end

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

internal class ConcurrentChangeDetectedE2ETest(@Autowired template : TestRestTemplate) : BaseE2EJourneyTest(template) {

    @Test
    fun `UNHAPPY | Schedule Class + Enroll Student but with wrong expected version and get rejected`() {
        val classURI = scheduleNewClass_withSize_isAccepted(10)

        var expectedClassVersion = 0L
        getClass_isOK_withVersion(classURI, expectedClassVersion)

        enrollStudent_isRejectedWith409(classURI, "STUDENT001", expectedClassVersion + 1)
    }
}