package eventsourcing.readmodels

import java.lang.Exception
import kotlin.Deprecated

/**
 * Interface to a generic, basic document store
 * containing a single type of document
 */
interface DocumentStore<D> {
    fun save(key: String, document: D)
    fun get(key: String) : D // FIXME change the interface to return an Optional

    // FIXME remove me
    @Deprecated("to be removed")
    fun list() : List<D>
}

/**
 * Super-simple document store, containing a single document
 */
interface SingleDocumentStore<D> {
    fun save(document: D)
    fun get() : D?
}

// FIXME get di
class DocumentNotFound(key: String) : Exception("Document not found (key: $key)")
