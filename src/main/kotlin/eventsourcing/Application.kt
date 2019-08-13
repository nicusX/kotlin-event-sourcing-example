package eventsourcing

import eventsourcing.domain.*
import eventsourcing.eventstore.InMemoryEventStore
import eventsourcing.messagebus.AsyncInMemoryBus
import eventsourcing.readmodel.InMemoryDatastore
import eventsourcing.readmodel.TrainingClassView
import kotlinx.coroutines.GlobalScope
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class KotlinBootApplication {

    val trainingClassView = TrainingClassView(InMemoryDatastore())
    val eventBus : EventPublisher<Event> = AsyncInMemoryBus(GlobalScope).register(trainingClassView)
    val eventStore : EventStore = InMemoryEventStore(eventBus)
    val trainingClassCommandHandler : TrainingClassCommandHandler = TrainingClassCommandHandler(TrainingClassRepository(eventStore))


    @Bean fun trainingClassView() = trainingClassView
    @Bean fun trainingClassCommandHandler() = trainingClassCommandHandler

}

fun main(args: Array<String>) {
    runApplication<KotlinBootApplication>(*args)
}
