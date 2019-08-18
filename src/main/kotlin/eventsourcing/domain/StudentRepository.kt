package eventsourcing.domain

import java.util.*

class StudentRepository(eventStore: EventStore): EventSourcedRepository<Student>(eventStore) {
    override fun new(id: AggregateID): Student = Student(id)

    // TODO move to a Service using a Read Model
    fun getByEmail(email: String) : Student? {
        // TODO implement (requires maintaining a specialised read-model) + Add test
        return null
    }
}
