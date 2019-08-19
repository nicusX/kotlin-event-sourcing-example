package eventsourcing.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Auxiliary read model, to identify whether an email is already in use.
 * It is both a Projection (handling NewStudentRegistered events) and a Read Model.
 */
class RegisteredEmailsIndex(private val emailIndex: Index<EMail>) : Handles<Event> {

    // TODO Support rebuilding index reconsuming all events

    override fun handle(event: Event) {
        when (event) {
            is NewStudentRegistered -> {
                log.debug("Add '{}' to the index", event.email)
                emailIndex.add(event.email)
            }
        }
    }

    fun isEmailAlreadyInUse(email: EMail): Boolean = emailIndex.contains(email)

    companion object {
        private val log: Logger = LoggerFactory.getLogger(RegisteredEmailsIndex::class.java)
    }
}