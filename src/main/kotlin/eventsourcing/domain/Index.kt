package eventsourcing.domain

import kotlin.jvm.Synchronized

interface Index <T> {
    fun contains(entry: T): Boolean
    fun add(entry: T): Boolean
    fun remove(entry: T): Boolean
}

/**
 * Nothing more than a synchronised wrapper around a MutableSet
 */
class InMemoryIndex<T> : Index<T> {
    private val set: MutableSet<T> = mutableSetOf()

    @Synchronized override fun add(entry: T): Boolean = set.add(entry)

    @Synchronized override fun contains(entry: T): Boolean = set.contains(entry)

    @Synchronized override fun remove(entry: T): Boolean = set.remove(entry)
}
