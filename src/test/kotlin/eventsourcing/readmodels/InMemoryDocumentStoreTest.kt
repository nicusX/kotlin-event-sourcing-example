package eventsourcing.readmodels

import arrow.core.None
import eventsourcing.OptionAssert.Companion.assertThatOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class InMemoryDocumentStoreTest {
    @Test
    fun `given an empty store, when I save a new document, then I may retrieve it back`() {
        val sut = givenAnEmptyDocumentStore()

        val id = "01"
        val doc = Dummy(id, 0)
        sut.save(id, doc)

        val retrieve = sut.get(id)
        assertThatOption(retrieve).isEqualTo(doc)
    }

    @Test
    fun `given a store containing one document, when I update the document, then I may retrieve the new version of it`() {
        val id = "01"
        val doc = Dummy(id, 0)
        val sut = givenADocumentStoreContaining(doc)

        val new = Dummy(id, 1)
        sut.save(id, new)

        val retrieve = sut.get(id)
        assertThatOption(retrieve).isEqualTo(new)
    }

    @Test
    fun `given a store, when I retrieve a document not in the store, then I get and empty result`(){
        val sut = givenAnEmptyDocumentStore()

        val retrieve = sut.get("-non-existing-key-")
        assertThatOption(retrieve).isEmpty()
    }
}

private data class Dummy(val id: String, val version: Int)

private fun InMemoryDocumentStoreTest.givenAnEmptyDocumentStore() : InMemoryDocumentStore<Dummy> = InMemoryDocumentStore()

private fun InMemoryDocumentStoreTest.givenADocumentStoreContaining(vararg documents: Dummy) : InMemoryDocumentStore<Dummy>  {
    val datastore = InMemoryDocumentStore<Dummy>()
    for(e in documents) datastore.save(e.id, e)
    return datastore
}
