package eventsourcing.api

import arrow.core.getOrElse
import eventsourcing.readmodels.trainingclasses.TrainingClass
import eventsourcing.readmodels.trainingclasses.TrainingClassDetails
import eventsourcing.readmodels.trainingclasses.TrainingClassReadModel
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.net.URI
import kotlin.reflect.jvm.javaMethod

// TODO rewrite using Routing DSL and coRouter
@RestController
class TrainingClassReadController(private val trainingClassReadModel: TrainingClassReadModel) {

    @GetMapping("/classes")
    fun listTrainingClasses(): ResponseEntity<List<TrainingClass>> = ResponseEntity.ok(trainingClassReadModel.allClasses())

    @GetMapping("/classes/{classId}")
    fun getTrainingClass(@PathVariable classId: String): ResponseEntity<TrainingClassDetails> =
            trainingClassReadModel.getTrainingClassDetailsById(classId)
                    .map { ResponseEntity.ok(it) }
                    .getOrElse{ ResponseEntity.notFound().build() }


    companion object {
        fun classResourceLocation(classId: String): URI =
                MvcUriComponentsBuilder.fromMethod(
                        TrainingClassReadController::class.java,
                        TrainingClassReadController::getTrainingClass.javaMethod!!,
                        classId)
                        .build(classId)
    }
}