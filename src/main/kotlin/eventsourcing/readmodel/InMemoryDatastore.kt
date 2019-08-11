package eventsourcing.readmodel

class InMemoryDatastore<E> : Datastore<E> {
    private val store : MutableMap<String, E> = mutableMapOf()

    override fun getById(id: String): E =
        store[id] ?: throw RecordNotFound(id)


    override fun list(): List<E> = store.values.toList()

    override fun save(id: String, entity: E) {
        store[id] = entity
    }

    fun clear() {
        store.clear()
    }
}