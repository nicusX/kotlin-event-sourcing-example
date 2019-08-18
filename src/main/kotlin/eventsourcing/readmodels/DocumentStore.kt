package eventsourcing.readmodels

import arrow.core.Option

/**
 * Interface to a generic, basic document store
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
