package eventsourcing.readmodels

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InMemoryDatastoreTest {
    @Test
    fun `given an empty datastore, when I save a new object, then I may retrieve it back`() {
        val sut = givenAnEmptyDatatore()

        val id = "01"
        val obj = Dummy(id, 0)
        sut.save(id, obj)

        val retrieve = sut.get(id)
        assertThat(retrieve).isEqualTo(obj)
    }

    @Test
    fun `given a datastore containing one object, when I update the object, then I may retrieve the new object`() {
        val id = "01"
        val obj = Dummy(id, 0)
        val sut = givenADatastoreContaining(obj)

        val new = Dummy(id, 1)
        sut.save(id, new)

        val retrieve = sut.get(id)
        assertThat(retrieve).isEqualTo(new)
    }

    @Test
    fun `given a datastore continaing multiple objects, when I retrieve the list, then I get all the objects`() {
        val objects = arrayOf( Dummy("001", 0), Dummy("002", 7), Dummy("003", 13))
        val sut = givenADatastoreContaining(*objects)

        val retrieve = sut.list()
        assertThat(retrieve).hasSize(objects.size)
        assertThat(retrieve).containsAll(objects.asIterable())
    }

    @Test
    fun `given a datastore, when I retrieve an object not in the datastore, then I get a RecordNotFound exception`(){
        val sut = givenAnEmptyDatatore()

        assertThrows<RecordNotFound> {
            sut.get("-non-existing-key-")
        }
    }
}

private data class Dummy(val id: String, val version: Int)

private fun InMemoryDatastoreTest.givenAnEmptyDatatore() : InMemoryDatastore<Dummy> = InMemoryDatastore()

private fun InMemoryDatastoreTest.givenADatastoreContaining(vararg entities: Dummy) : InMemoryDatastore<Dummy>  {
    val datastore = InMemoryDatastore<Dummy>()
    for(e in entities) datastore.save(e.id, e)
    return datastore
}
