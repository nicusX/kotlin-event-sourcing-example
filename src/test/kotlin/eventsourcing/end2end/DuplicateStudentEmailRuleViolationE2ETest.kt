package eventsourcing.end2end

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

internal class DuplicateStudentEmailRuleViolationE2ETest(@Autowired template : TestRestTemplate) : BaseE2EJourneyTest(template) {

    @Test
    fun `UNHAPPY | Register a Student, Register another Student with the same email and get rejected`(){
        val aStudentURI = registerStudent_withEmail_isAccepted("student1@ema.il")
        getStudent_isOk_withVersion(aStudentURI, 0L)

        registerStudent_withEmail_isRejectedWith422("student1@ema.il")
    }
}