package eventsourcing.domain

import java.lang.Exception

interface Repository<A: AggregateRoot> {
    fun getById(id: AggregateID): A // FIXME change to return Option<A>
    fun save(aggregate: A, expectedVersion: Long? = null) // FIXME change expectedVersion: Option<Long>
    fun new(id: AggregateID) : A
}

// FIXME remove this exception
class AggregateNotFoundException(aggregateType: AggregateType, aggregateID: AggregateID)
    : Exception("Aggregate not found. ${aggregateType.toString()}:${aggregateID.toString()}")
