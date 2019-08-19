package eventsourcing

import arrow.core.*
import eventsourcing.domain.AggregateRoot
import eventsourcing.domain.Event
import eventsourcing.domain.Result
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import kotlin.reflect.KClass

internal class EventsAssert(actual: Option<Iterable<Event>>) : AbstractAssert<EventsAssert, Option<Iterable<Event>>>(actual, EventsAssert::class.java) {

    private fun Option<Iterable<Event>>.safeExtract() : Iterable<Event> =
            this.getOrElse { emptyList() }

    fun isDefined() : EventsAssert {
        if ( actual is None )
            failWithMessage("Expected Some<Iterable<Event>> but was <None>")
        return this
    }

    fun isNone() : EventsAssert {
        if ( actual is Some<*>)
            failWithMessage("Expected None but was %s", actual)
        return this
    }

    fun containsNoEvent(): EventsAssert {
        Assertions.assertThat(actual.safeExtract()).isEmpty()
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
        fun assertThatAggregateUncommitedChanges(aggregate: AggregateRoot?): EventsAssert =
                EventsAssert(Some(aggregate?.getUncommittedChanges() ?: emptyList()  ))

        fun assertThatEvents(actual: Option<Iterable<Event>>): EventsAssert = EventsAssert(actual)

        fun assertThatEvents(actual: Iterable<Event>?): EventsAssert = EventsAssert(Option.fromNullable(actual))
    }
}

// FIXME rephrase with "success" and "failure"
internal class ResultAssert<A,B>(actual: Result<A, B>) : AbstractAssert<ResultAssert<A,B>, Result<A, B>>(actual, ResultAssert::class.java) {

    fun isFailure() : ResultAssert<A,B> {
        if ( actual.isRight())
            failWithMessage("Expected <Left> but was <%s>", actual)
        return this
    }

    fun isSuccess() : ResultAssert<A,B> {
        if ( actual.isLeft())
            failWithMessage("Expected <Right> but was <%s>", actual)
        return this
    }

    fun failureIsEqualTo(expected: A) : ResultAssert<A,B> {
        if( expected != actual.swap().getOrElse { null } )
            failWithMessage("Expected <Left(%s)> but was <%s>", expected, actual)
        return this
    }

    inline fun <reified T>failureIsA(): ResultAssert<A,B> {
        if ( actual.isRight() || actual.getOrElse { null } is T )
            failWithMessage("Expected <Left<%s>> but was <%s>", T::class.simpleName, typeOfFailure().map { it.simpleName }  )
        return this
    }

    fun successIsEqualTo(expected: B) : ResultAssert<A,B> {
        if ( expected != actual.getOrElse { null })
            failWithMessage("Expected <Right(%s)> but was <%s>", expected, actual)
        return this
    }

    inline fun <reified T>successIsA(): ResultAssert<A,B> {
        if ( actual.isLeft() || actual.swap().getOrElse { null } is T)
            failWithMessage("Expected <Right<%s>> but was <%s>", T::class.simpleName, typeOfSuccess().map { it.simpleName }  )
        return this
    }

    private fun typeOfSuccess(): Option<KClass<*>> {
        val right : Any? = actual.getOrElse { null }
        return when (right) {
            is Any -> Some(right::class)
            else -> None
        }
    }

    private fun typeOfFailure(): Option<KClass<*>> {
        val left : Any? = actual.swap().getOrElse { null }
        return when (left) {
            is Any -> Some(left::class)
            else -> None
        }
    }

    fun extractSuccess(): B? = actual.getOrElse { null }

    fun extractFailure(): A? = actual.swap().getOrElse { null }

    companion object {
        fun <A,B> assertThatResult(actual: Result<A, B>) : ResultAssert<A,B> = ResultAssert(actual)
    }
}

internal class OptionAssert<A>(actual: Option<A>): AbstractAssert<OptionAssert<A>, Option<A>>(actual, OptionAssert::class.java) {

    fun isEmpty(): OptionAssert<A> {
        if ( actual.isDefined()) failWithMessage("Expected <None> but was <%s>", actual)
        return this
    }

    fun isDefined(): OptionAssert<A> {
        if ( actual.isEmpty()) failWithMessage("Expected <Some(*)> but was <%s>", actual)
        return this
    }

    fun extract(): A? = actual.getOrElse { null }

    fun contains(expected: A): OptionAssert<A> {
        if( expected != actual.getOrElse { null }) failWithMessage("Expected <Some(%s)> but was <%s>", expected, actual)
        return this
    }

    companion object {
        fun <A> assertThatOption(actual: Option<A>) : OptionAssert<A> = OptionAssert(actual)
    }
}


