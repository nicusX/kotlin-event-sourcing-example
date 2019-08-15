package eventsourcing.domain

import java.util.*

class StudentRepository(eventStore: EventStore): EventSourcedRepository<Student>(eventStore) {
    override fun new(id: AggregateID): Student = Student(id)

    // FIXME move to a Service using a Read Model
    fun getByEmail(email: String) : Optional<Student> {
        // FIXME implement (requires maintaining a specialised read-model) + Add test
        return Optional.empty()
    }
}
