package eventsourcing

import eventsourcing.rest.BaseE2E
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

internal class TrainingClassApiEnd2EndTests(@Autowired template : TestRestTemplate) : BaseE2E(template) {

    @Test
    fun `HAPPY | Schedule Class + Enroll a Student + Enroll another Sudent + Unenroll first Student`(){
        val nOfClasses = listClasses_isOk()

        val classURI = scheduleNewClass_withSize_isAccepted(10)
        var expectedClassVersion = 0L
        getClass_isOK_withVersion(classURI, expectedClassVersion)

        listClasses_isOk_withNofClasses(nOfClasses + 1)

        enrollStudent_isAccepted(classURI, "STUDENT001", expectedClassVersion)
        expectedClassVersion++

        getClass_isOk_withVersion_andWithStudents(classURI, expectedClassVersion, "STUDENT001")

        enrollStudent_isAccepted(classURI, "STUDENT002", expectedClassVersion)
        expectedClassVersion++

        getClass_isOk_withVersion_andWithStudents(classURI, expectedClassVersion, "STUDENT001", "STUDENT002")

        unenrollStudent_isAccepted(classURI,"STUDENT001", expectedClassVersion)
        expectedClassVersion++

        getClass_isOk_withVersion_andWithStudents(classURI, expectedClassVersion, "STUDENT002")

    }

    @Test
    fun `UNHAPPY | Schedule Class + Enroll Student but with wrong expected version and get rejected`(){
        val classURI = scheduleNewClass_withSize_isAccepted(10)

        var expectedClassVersion = 0L
        getClass_isOK_withVersion(classURI, expectedClassVersion)

        enrollStudent_isRejectedWith409(classURI, "STUDENT001", expectedClassVersion + 1)
    }

    @Test
    fun `UNHAPPY | Schedule Class + Enroll too many Students and get rejected`(){
        val classURI = scheduleNewClass_withSize_isAccepted(2)
        var expectedClassVersion = 0L

        enrollStudent_isAccepted(classURI, "STUDENT001", expectedClassVersion)
        expectedClassVersion++

        enrollStudent_isAccepted(classURI, "STUDENT002", expectedClassVersion)
        expectedClassVersion++

        enrollStudent_isRejectedWith422(classURI, "STUDENT002", expectedClassVersion)
    }

    @Test
    fun `UNAHPPY | Schedule Class + Enroll a Student + Unenroll a different Student and get rejected`(){
        val classURI = scheduleNewClass_withSize_isAccepted(2)
        var expectedClassVersion = 0L

        enrollStudent_isAccepted(classURI, "STUDENT001", expectedClassVersion)
        expectedClassVersion++

        unenrollStudent_isRejectedWith422(classURI, "STUDENT999", expectedClassVersion)
    }
}
