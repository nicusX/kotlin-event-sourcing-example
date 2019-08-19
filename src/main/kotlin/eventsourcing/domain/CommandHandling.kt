package eventsourcing.domain

// FIXME remove this exception
@Deprecated("To be removed")
class AggregateNotFoundException() : Exception()

object AggregateNotFound : Problem