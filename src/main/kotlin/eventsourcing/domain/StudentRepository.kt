package eventsourcing.domain

import arrow.core.None
import arrow.core.Option

class StudentRepository(eventStore: EventStore): EventSourcedRepository<Student>(eventStore) {
    override fun new(id: AggregateID): Student = Student(id)

    fun getByEmail(email: String) : Option<Student> {
        // TODO implement (requires maintaining a specialised read-model) + Add test
        return None
    }
}
