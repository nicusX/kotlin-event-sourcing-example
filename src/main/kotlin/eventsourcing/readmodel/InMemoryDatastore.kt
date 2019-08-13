package eventsourcing.readmodel

/**
 * Simple implementation of Datastore, keeping everything in memory
 */
class InMemoryDatastore<E> : Datastore<E> {
    private val store: MutableMap<String, E> = mutableMapOf()

    @Synchronized override fun getById(id: String): E =
            store[id] ?: throw RecordNotFound(id)


    @Synchronized override fun list(): List<E> = store.values.toList()

    @Synchronized override fun save(id: String, entity: E) {
        store[id] = entity
    }

    fun clear() {
        store.clear()
    }
}