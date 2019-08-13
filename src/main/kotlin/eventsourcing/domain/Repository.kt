package eventsourcing.domain

interface Repository<A: AggregateRoot> {
    fun getById(id: AggregateID): A
    fun save(aggregate: A, expectedVersion: Long? = null)
    fun new(id: AggregateID) : A
}
