package eventsourcing.domain

interface EventPublisher<E : Event> {
    fun publish(event : E)
}

// TODO CommandPublisher

// TODO EventHandler, CommandHandler (by message type and/or aggregate)

interface Handles<in E : Event> {
    fun handle(event : E)
}