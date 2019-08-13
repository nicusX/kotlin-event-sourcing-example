package eventsourcing.domain

import java.util.*

class StudentRepository(eventStore: EventStore): EventSourcedRepository<Student>(eventStore) {
    override fun new(id: AggregateID): Student = Student(id)

    fun getByEmail(email: String) : Optional<Student> {
        // FIXME implement (requires maintaining a specialised read-model) + Add test
        return Optional.empty()
    }
}
