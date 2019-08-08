package eventsourcing.domain

class TrainingClassRepository(eventStore: EventStore) : EventSourcedRepository<TrainingClass>(eventStore) {
    override fun new(id: AggregateID): TrainingClass = TrainingClass(id)
}
