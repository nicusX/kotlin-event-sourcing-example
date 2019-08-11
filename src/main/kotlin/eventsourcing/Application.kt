package eventsourcing

import eventsourcing.domain.*
import eventsourcing.eventstore.InMemoryEventStore
import eventsourcing.messagebus.InMemoryBus
import eventsourcing.readmodel.InMemoryDatastore
import eventsourcing.readmodel.TrainingClassDTO
import eventsourcing.readmodel.TrainingClassView
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.LocalDate

@SpringBootApplication
class KotlinBootApplication {

    val trainingClassView
            = TrainingClassView(InMemoryDatastore<TrainingClassDTO>())
    val eventBus : EventPublisher<Event>
            = InMemoryBus().register(trainingClassView)
    val eventStore : EventStore
            = InMemoryEventStore(eventBus)
    val trainingClassCommandHandler : TrainingClassCommandHandler
        = TrainingClassCommandHandler(TrainingClassRepository(eventStore))


    @Bean fun trainingClassView() = trainingClassView
    @Bean fun trainingClassCommandHandler() = trainingClassCommandHandler

}

fun main(args: Array<String>) {
    runApplication<KotlinBootApplication>(*args)
}
