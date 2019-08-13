package eventsourcing.messagebus

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import eventsourcing.domain.Event
import eventsourcing.domain.Handles
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

/**
 * This test uses kotlinx.coroutines.test.runBlockingTest to test the asynchronous behaviours in AsyncInMemoryBus
 */
internal class AsyncInMemoryBusTest {
    @Test
    fun `given a message bus with registered handlers, when I publish multiple events, then all handlers get notified for each event`() = runBlockingTest {
        val sut = AsyncInMemoryBus(this)
        val handlers = listOf( mock<Handles<Event>>(), mock<Handles<Event>>() )
        for(h in handlers) sut.register(h)

        for(i in 1..10L)
            sut.publish(object : Event(i) {
                override fun copyWithVersion(version: Long): Event = this
            })

        for (h in handlers)
            verify(h, times(10)).handle(any<Event>())

        sut.shutdown()
    }
}

