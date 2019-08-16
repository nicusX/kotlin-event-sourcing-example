package eventsourcing.end2end

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate

internal class HappyJourneyE2ETest(@Autowired template : TestRestTemplate) : BaseE2EJourneyTest(template) {

    @Test
    fun `HAPPY | Register a Student + Register another Student + Schedule Class + Enroll a Student + Enroll another Student + Unenroll first Student`() {

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
}
