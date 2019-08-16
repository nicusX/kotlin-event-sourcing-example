package eventsourcing.end2end

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

internal class UnenrollNotEnrolledStudentRuleViolationE2ETest(@Autowired template : TestRestTemplate) : BaseE2EJourneyTest(template) {

    @Test
    fun `UNAHPPY | Schedule Class + Enroll a Student + Unenroll a different Student and get rejected`()  {
        val classURI = scheduleNewClass_withSize_isAccepted(2)
        var expectedClassVersion = 0L

        enrollStudent_isAccepted(classURI, "STUDENT001", expectedClassVersion)
        expectedClassVersion++

        unenrollStudent_isRejectedWith422(classURI, "STUDENT999", expectedClassVersion)
    }
}