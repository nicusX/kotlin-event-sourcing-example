package eventsourcing.readmodels

/**
 * Simple implementation of Document Store, thread-safe, keeping everything in memory
 */
class InMemoryDocumentStore<D> : DocumentStore<D> {
    private val store: MutableMap<String, D> = mutableMapOf()

    @Synchronized override fun get(key: String): D = store[key] ?: throw DocumentNotFound(key)

    @Synchronized override fun list(): List<D> = store.values.toList()

    @Synchronized override fun save(key: String, document: D) {
        store[key] = document
    }

    fun clear() {
        store.clear()
    }
}

class InMemorySingleDocumentStore<D> : SingleDocumentStore<D> {
    private var document: D? = null

    @Synchronized override fun get() = document

    @Synchronized
    override fun save(document: D) {
        this.document = document
    }

    fun clear() {
        document = null
    }
}