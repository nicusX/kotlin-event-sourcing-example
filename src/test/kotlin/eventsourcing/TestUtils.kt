package eventsourcing

import eventsourcing.domain.AggregateID
import eventsourcing.domain.AggregateRoot
import eventsourcing.domain.Event
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

internal class EventsAssert(actual: Iterable<Event>) : AbstractAssert<EventsAssert, Iterable<Event>>(actual, EventsAssert::class.java) {


    fun contains(expectedSize: Int): EventsAssert {
        Assertions.assertThat(actual).hasSize(expectedSize)
        return this
    }

    fun containsAllInOrder(expected: List<Event>): EventsAssert {
        for ((i, actualEvent) in actual.withIndex()) {
            Assertions.assertThat(actualEvent).isEqualTo(expected[i])
        }
        return this
    }

    fun onlyContainsInOrder(expected: List<Event>): EventsAssert =
            this.contains(expected.size).containsAllInOrder(expected)

    fun onlyContains(expected: Event): EventsAssert =
            this.onlyContainsInOrder(listOf(expected))

    fun containsAllEventTypesInOrder(expected: List<Class<*>>): EventsAssert {
        for ((i, actualEvent) in actual.withIndex()) {
            Assertions.assertThat(actualEvent).isInstanceOf(expected[i])
        }
        return this
    }

    fun onlyContainsEventTypesInOrder(expected: List<Class<*>>): EventsAssert =
            this.contains(expected.size).containsAllEventTypesInOrder(expected)

    fun onlyContainsAnEventOfType(expected: Class<*>): EventsAssert =
            this.onlyContainsEventTypesInOrder(listOf(expected))


    fun containsNoEvents(): EventsAssert = contains(0)


    companion object {
        fun assertThatAggregateUncommitedChanges(aggregate: AggregateRoot): EventsAssert =
                EventsAssert(aggregate.getUncommittedChanges())

        fun assertThatEvents(actual: Iterable<Event>): EventsAssert =
                EventsAssert(actual)
    }
}

internal fun <A : AggregateRoot> given(init: () -> A): Pair<A, AggregateID> {
    val agg = init()
    agg.markChangesAsCommitted()
    return Pair(agg, agg.id)
}