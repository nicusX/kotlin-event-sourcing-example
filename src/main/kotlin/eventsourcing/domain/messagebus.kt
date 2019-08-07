package eventsourcing.domain

interface EventPublisher<E : Event> {
    fun publish(event : E)
}

// TODO CommandPublisher

// TODO EventHandler, CommandHandler (by messsage type and/or aggregate)