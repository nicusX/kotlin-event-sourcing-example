package eventsourcing.messagebus

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import eventsourcing.domain.Event
import eventsourcing.domain.Handles
import org.junit.jupiter.api.Test

internal class InMemoryBusTest {
    @Test
    fun `given a message bus with registered handlers, when I publish an event, then all handlers get notified`() {
        val sut = InMemoryBus()
        val handlers = listOf( mock<Handles<Event>>(), mock<Handles<Event>>() )
        for(h in handlers) sut.register(h)

        val event = DummyEvent
        sut.publish(event)

        for(h in handlers)
            verify(h).handle(eq(event))
    }
}

private object DummyEvent : Event(0L) {
    override fun copyWithVersion(version: Long): Event  = this
}