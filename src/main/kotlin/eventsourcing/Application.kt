package eventsourcing

import eventsourcing.api.CommandDispatcher
import eventsourcing.domain.*
import eventsourcing.eventstore.InMemoryEventStore
import eventsourcing.messagebus.AsyncInMemoryBus
import eventsourcing.readmodels.InMemoryDocumentStore
import eventsourcing.readmodels.InMemorySingleDocumentStore
import eventsourcing.readmodels.studentdetails.StudentDetails
import eventsourcing.readmodels.studentdetails.StudentDetailsProjection
import eventsourcing.readmodels.studentdetails.StudentDetailsReadModel
import eventsourcing.readmodels.studentlist.StudentList
import eventsourcing.readmodels.studentlist.StudentListProjection
import eventsourcing.readmodels.studentlist.StudentListReadModel
import eventsourcing.readmodels.trainingclasses.*
import kotlinx.coroutines.GlobalScope
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class EventSourcingApplication {

    // Manually wiring up all dependencies

    // Student Details Read Model
    private val studentDetailsDatastore = InMemoryDocumentStore<StudentDetails>()
    private val studentDetailsProjection = StudentDetailsProjection(studentDetailsDatastore)
    private val studentDetailsReadModelFacade = StudentDetailsReadModel(studentDetailsDatastore)

    // Student List Read Model
    private val studentListDatastore = InMemorySingleDocumentStore<StudentList>(emptyList())
    private val studentListProjection = StudentListProjection(studentListDatastore)
    private val studentListReadModelFacade = StudentListReadModel(studentListDatastore)

    // Training Class Read Model
    private val trainingClassDetailsStore = InMemoryDocumentStore<TrainingClassDetails>()
    private val trainingClassListStore = InMemorySingleDocumentStore<TrainingClassList>(emptyList())
    private val studentsContactsStore = InMemoryDocumentStore<StudentContacts>()
    private val trainingClassProjection = TrainingClassProjection(trainingClassDetailsStore, trainingClassListStore, studentsContactsStore)
    private val trainingClassReadModel = TrainingClassReadModel(trainingClassDetailsStore, trainingClassListStore)

    private val registeredEmailIndex = RegisteredEmailsIndex(InMemoryIndex<EMail>())

    // Event Bus
    private val eventBus : EventPublisher<Event> = AsyncInMemoryBus(GlobalScope)
            .register(studentDetailsProjection)
            .register(studentListProjection)
            .register(trainingClassProjection)
            .register(registeredEmailIndex)

    // Event Store and Event-Sourced Repositories
    private val eventStore : EventStore = InMemoryEventStore(eventBus)
    private val classRepository = TrainingClassRepository(eventStore)
    private val studentRepository = StudentRepository(eventStore)
    private val commandDispatcher : CommandDispatcher = CommandDispatcher(classRepository, studentRepository, registeredEmailIndex)


    // The only Spring Beans are read models and the command handler dispatcher, to be injected in Controllers

    // These Beans are injected in the MVC Controllers
    @Bean fun studentDetailsReadModelFacade() = studentDetailsReadModelFacade
    @Bean fun studentListReadModelFacade() = studentListReadModelFacade
    @Bean fun trainingClassReadModel() = trainingClassReadModel
    @Bean fun trainingClassCommandHandler() = commandDispatcher
}

fun main(args: Array<String>) {
    runApplication<EventSourcingApplication>(*args)
}
