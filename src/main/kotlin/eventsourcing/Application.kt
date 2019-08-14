package eventsourcing

import eventsourcing.api.CommandDispatcher
import eventsourcing.domain.*
import eventsourcing.eventstore.InMemoryEventStore
import eventsourcing.messagebus.AsyncInMemoryBus
import eventsourcing.readmodels.*
import kotlinx.coroutines.GlobalScope
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class KotlinBootApplication {

    private val trainingClassView = TrainingClassView(InMemoryDatastore<TrainingClassDTO>())
    private val studentView = StudentView(InMemoryDatastore<StudentDTO>())
    private val eventBus : EventPublisher<Event> = AsyncInMemoryBus(GlobalScope)
            .register(trainingClassView)
            .register(studentView)
    private val eventStore : EventStore = InMemoryEventStore(eventBus)

    private val classRepository = TrainingClassRepository(eventStore)
    private val studentRepository = StudentRepository(eventStore)
    private val commandDispatcher : CommandDispatcher = CommandDispatcher(classRepository, studentRepository)

    @Bean fun trainingClassView() = trainingClassView
    @Bean fun studentView() = studentView
    @Bean fun trainingClassCommandHandler() = commandDispatcher
}

fun main(args: Array<String>) {
    runApplication<KotlinBootApplication>(*args)
}
