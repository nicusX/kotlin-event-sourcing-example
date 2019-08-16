package eventsourcing.end2end

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

internal class AvailableSpotsRuleViolationE2ETest(@Autowired template : TestRestTemplate) : BaseE2EJourneyTest(template) {

    @Test
    fun `UNHAPPY | Schedule Class + Enroll too many Students and get rejected`() {
        val classURI = scheduleNewClass_withSize_isAccepted(2)
        var expectedClassVersion = 0L

        enrollStudent_isAccepted(classURI, "STUDENT001", expectedClassVersion)
        expectedClassVersion++

        enrollStudent_isAccepted(classURI, "STUDENT002", expectedClassVersion)
        expectedClassVersion++

        enrollStudent_isRejectedWith422(classURI, "STUDENT002", expectedClassVersion)
    }
}