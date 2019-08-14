package eventsourcing.api

import eventsourcing.readmodels.RecordNotFound
import eventsourcing.readmodels.TrainingClassDTO
import eventsourcing.readmodels.TrainingClassView
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.net.URI
import kotlin.reflect.jvm.javaMethod

// TODO rewrite using Routing DSL and coRouter
@RestController
class TrainingClassReadController(private val view: TrainingClassView) {

    // TODO Return a simplified DTO in the list
    @GetMapping("/classes")
    fun listTrainingClasses() :  ResponseEntity<List<TrainingClassDTO>>
            = ResponseEntity.ok(view.listClasses())

    @GetMapping("/classes/{classId}")
    fun getTrainingClass(@PathVariable classId: String) : ResponseEntity<TrainingClassDTO>
        =  try {
            ResponseEntity.ok(view.getClassById(classId))
        } catch (e: RecordNotFound) {
            ResponseEntity.notFound().build()
        }


    companion object {
        fun classResourceLocation(classId: String) : URI =
            MvcUriComponentsBuilder.fromMethod(
                    TrainingClassReadController::class.java,
                    TrainingClassReadController::getTrainingClass.javaMethod!!,
                    classId)
                    .build(classId)
    }
}