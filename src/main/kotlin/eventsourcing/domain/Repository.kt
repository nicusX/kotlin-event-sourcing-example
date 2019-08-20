package eventsourcing.domain

import arrow.core.None
import arrow.core.Option

/**
 * Interface for a simple Repository, supporting version-based concurrency control
 */
interface Repository<A: AggregateRoot> {
    fun getById(id: AggregateID): Option<A>
    fun save(aggregate: A, expectedVersion: Option<Long> = None) : Result<Failure, Success>
    fun new(id: AggregateID) : A // Delegating the creation of a new Aggregate to the Repository, for simplicity
}
