package eventsourcing.rest

import eventsourcing.readmodel.RecordNotFound
import eventsourcing.readmodel.TrainingClassDTO
import eventsourcing.readmodel.TrainingClassView
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class TrainingClassReadController(private val view: TrainingClassView) {

    @GetMapping("/classes")
    fun listTrainingClasses() : List<TrainingClassDTO>
            = view.list()

    @GetMapping("/classes/{classId}")
    fun getTrainingClass(@PathVariable classId: String) : TrainingClassDTO
        = view.getById(classId)

    @ResponseStatus(value= HttpStatus.NOT_FOUND, reason="Training Class not found")
    @ExceptionHandler(RecordNotFound::class)
    fun notFound() {}
}