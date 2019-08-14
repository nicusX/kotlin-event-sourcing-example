package eventsourcing.readmodels

/**
 * Simple implementation of Datastore, keeping everything in memory
 */
class InMemoryDatastore<E> : Datastore<E> {
    private val store: MutableMap<String, E> = mutableMapOf()

    @Synchronized override fun get(key: String): E =
            store[key] ?: throw RecordNotFound(key)


    @Synchronized override fun list(): List<E> = store.values.toList()

    @Synchronized override fun save(key: String, entity: E) {
        store[key] = entity
    }

    fun clear() {
        store.clear()
    }
}