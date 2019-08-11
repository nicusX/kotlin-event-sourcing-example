package eventsourcing.readmodel

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class InMemoryDatastoreTest {
    @Test
    fun `given an empty datastore, when I save a new entity, I should be able to retrieve it back`() {
        val sut = given()

        val id = "01"
        val entity = Dummy(id, 0)
        sut.save(id, entity)

        val retrieve = sut.getById(id)
        assertThat(retrieve).isEqualTo(entity)
    }

    @Test
    fun `given a datastore containing one entity, when I update the entity, I should be able to retrieve the new entity`() {
        val id = "01"
        val entity = Dummy(id, 0)
        val sut = given(entity)

        val new = Dummy(id, 1)
        sut.save(id, new)

        val retrieve = sut.getById(id)
        assertThat(retrieve).isEqualTo(new)
    }

    @Test
    fun `given a datastore continaing multiple entities, when I retrieve the list, I should get all the entities`() {
        val entities = arrayOf( Dummy("001", 0), Dummy("002", 7), Dummy("003", 13))
        val sut = given(*entities)

        val retrieve = sut.list()
        assertThat(retrieve).hasSize(entities.size)
        assertThat(retrieve).containsAll(entities.asIterable())
    }
}

data class Dummy(val id: String, val version: Int)

private fun InMemoryDatastoreTest.given() : InMemoryDatastore<Dummy> = InMemoryDatastore()

private fun InMemoryDatastoreTest.given(vararg entities: Dummy) : InMemoryDatastore<Dummy>  {
    val datastore = InMemoryDatastore<Dummy>()
    for(e in entities) datastore.save(e.id, e)
    return datastore
}
