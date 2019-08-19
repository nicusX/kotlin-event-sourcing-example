package eventsourcing.domain

class StudentRepository(eventStore: EventStore) : EventSourcedRepository<Student>(eventStore) {
    override fun new(id: AggregateID): Student = Student(id)
}
