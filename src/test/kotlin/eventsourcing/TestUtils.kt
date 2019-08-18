package eventsourcing

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import eventsourcing.domain.AggregateID
import eventsourcing.domain.AggregateRoot
import eventsourcing.domain.Event
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.ObjectAssert

internal class EventsAssert(actual: Option<Iterable<Event>>) : AbstractAssert<EventsAssert, Option<Iterable<Event>>>(actual, EventsAssert::class.java) {

    private fun Option<Iterable<Event>>.safeExtract() : Iterable<Event> =
            this.getOrElse { emptyList() }

    fun isNotEmpty() : EventsAssert {
        Assertions.assertThat(actual).isInstanceOf(Some::class.java)
        return this
    }

    fun isEmpty() : EventsAssert {
        Assertions.assertThat(actual).isEqualTo(None)
        return this
    }

    fun contains(expectedSize: Int): EventsAssert {
        Assertions.assertThat(actual.safeExtract()).hasSize(expectedSize)
        return this
    }

    fun containsAllInOrder(expected: List<Event>): EventsAssert {
        for ((i, actualEvent) in actual.safeExtract().withIndex()) {
            Assertions.assertThat(actualEvent).isEqualTo(expected[i])
        }
        return this
    }

    fun onlyContainsInOrder(expected: List<Event>): EventsAssert =
            this.contains(expected.size).containsAllInOrder(expected)

    fun onlyContains(expected: Event): EventsAssert =
            this.onlyContainsInOrder(listOf(expected))

    fun containsAllEventTypesInOrder(expected: List<Class<*>>): EventsAssert {
        for ((i, actualEvent) in actual.safeExtract().withIndex()) {
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
                EventsAssert(Some(aggregate.getUncommittedChanges()))

        fun assertThatEvents(actual: Option<Iterable<Event>>): EventsAssert =
                EventsAssert(actual)
    }
}

internal fun <A : AggregateRoot> given(init: () -> A): Pair<A, AggregateID> {
    val agg = init()
    agg.markChangesAsCommitted()
    return Pair(agg, agg.id)
}
