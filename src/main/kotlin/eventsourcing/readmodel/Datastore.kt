package eventsourcing.readmodel

import java.lang.Exception

interface Datastore<V> {
    fun save(id: String, entity: V)
    fun getById(id: String) : V
    fun list() : List<V>
}

class RecordNotFound(id: String) :
        Exception("Record id: $id not found")