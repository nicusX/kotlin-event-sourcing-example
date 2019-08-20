package eventsourcing.readmodels

import arrow.core.Option

// Different datastores backing read models

/**
 * A generic, basic document store
 * containing a single type of document
 */
interface DocumentStore<D> {
    fun save(key: String, document: D)
    fun get(key: String) : Option<D>
}

/**
 * Super-simple document store, containing a single document
 */
interface SingleDocumentStore<D> {
    fun save(document: D)
    fun get() : D
}

/**
 * Implementation of DocumentStore, keeping everything in memory but thread-safe
 */
class InMemoryDocumentStore<D> : DocumentStore<D> {
    private val store: MutableMap<String, D> = mutableMapOf()

    @Synchronized override fun get(key: String): Option<D> = Option.fromNullable(store[key])

    @Synchronized override fun save(key: String, document: D) {
        store[key] = document
    }

    fun clear() {
        store.clear()
    }
}

/**
 * Implementation of SingleDocumentStore, keeping the document in memory, but thread-safe
 */
class InMemorySingleDocumentStore<D>(private val initialValue: D) : SingleDocumentStore<D> {
    private var document: D = initialValue

    @Synchronized override fun get() = document

    @Synchronized
    override fun save(document: D) {
        this.document = document
    }

    fun clear() {
        document = initialValue
    }
}
