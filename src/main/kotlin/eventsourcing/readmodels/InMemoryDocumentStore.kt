package eventsourcing.readmodels

/**
 * Simple implementation of Document Store, thread-safe, keeping everything in memory
 */
class InMemoryDocumentStore<D> : DocumentStore<D> {
    private val store: MutableMap<String, D> = mutableMapOf()

    @Synchronized override fun get(key: String): D? = store[key]

    @Synchronized override fun save(key: String, document: D) {
        store[key] = document
    }

    fun clear() {
        store.clear()
    }
}

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