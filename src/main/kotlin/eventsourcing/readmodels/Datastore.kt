package eventsourcing.readmodels

import java.lang.Exception

interface Datastore<V> {
    fun save(key: String, entity: V)
    fun get(key: String) : V
    fun list() : List<V>
}

class RecordNotFound(id: String) :
        Exception("Record id: $id not found")