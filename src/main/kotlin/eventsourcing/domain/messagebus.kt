package eventsourcing.domain

interface EventPublisher<E : Event> {
    fun publish(event : E)
    fun register(eventHandler: Handles<E>)
}

interface Handles<in E : Event> {
    fun handle(event : E)
}
