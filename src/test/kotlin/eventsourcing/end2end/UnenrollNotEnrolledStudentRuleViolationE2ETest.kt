package eventsourcing.end2end

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

internal class UnenrollNotEnrolledStudentRuleViolationE2ETest(@Autowired template : TestRestTemplate) : BaseE2EJourneyTest(template) {

    @Test
    fun `UNAHPPY | Register Student + Schedule Class + Enroll a Student + Unenroll a different Student and get rejected`()  {
        val aStudentURI = registerStudent_withEmailAndFullName_isAccepted("student1@ema.il", "First Student")
        val aStudent = getStudent_isOk_withVersion(aStudentURI, 0L)

        val classURI = scheduleNewClass_withSize_isAccepted(2)
        var expectedClassVersion = 0L

        enrollStudent_isAccepted(classURI, aStudent.studentId, expectedClassVersion)
        expectedClassVersion++

        unenrollStudent_isRejectedWith422(classURI, "ANOTHER-STUDENT-ID", expectedClassVersion)
    }
}