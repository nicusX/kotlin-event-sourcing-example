package eventsourcing

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import java.lang.Thread.sleep

internal class ApiEnd2EndTests(@Autowired template : TestRestTemplate) : BaseApiE2ETest(template) {

    @Test
    fun `HAPPY | Register a Student + Register another Student + Schedule Class + Enroll a Student + Enroll another Sudent + Unenroll first Student`(){

        val nOfStudents = listStudents_isOk()

        val aStudentURI = registerStudent_withEmailAndFullName_isAccepted("student1@ema.il", "First Student")
        val aStudent = getStudent_isOk_withVersion(aStudentURI, 0L)

        val anotherStudentURI = registerStudent_withEmailAndFullName_isAccepted("student2@ema.il", "Second Student")
        val anotherStudent = getStudent_isOk_withVersion(anotherStudentURI, 0L)

        listStudents_isOk_withNofStudents(nOfStudents + 2)


        val nOfClasses = listClasses_isOk()

        val classURI = scheduleNewClass_withSize_isAccepted(10)
        var expectedClassVersion = 0L
        getClass_isOK_withVersion(classURI, expectedClassVersion)

        listClasses_isOk_withNofClasses(nOfClasses + 1)

        enrollStudent_isAccepted(classURI, aStudent.studentId, expectedClassVersion)
        expectedClassVersion++

        getClass_isOk_withVersion_andWithStudents(classURI, expectedClassVersion, aStudent.studentId)

        enrollStudent_isAccepted(classURI, anotherStudent.studentId, expectedClassVersion)
        expectedClassVersion++

        getClass_isOk_withVersion_andWithStudents(classURI, expectedClassVersion, aStudent.studentId, anotherStudent.studentId)

        unenrollStudent_isAccepted(classURI,aStudent.studentId, expectedClassVersion)
        expectedClassVersion++

        getClass_isOk_withVersion_andWithStudents(classURI, expectedClassVersion, anotherStudent.studentId)

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

    // FIXME test: `UNHAPPY | Register a Student, Register another Student with the same email and get rejected`
}
