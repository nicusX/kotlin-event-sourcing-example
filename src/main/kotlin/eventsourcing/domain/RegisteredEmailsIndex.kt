package eventsourcing.domain

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Service using an auxiliary read model, to check whether an email is already in use
 *
 * This is an all-in-one: Service, underlying Read Model and Projection (handling NewStudentRegistered events)

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