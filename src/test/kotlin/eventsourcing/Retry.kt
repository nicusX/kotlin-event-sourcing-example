package eventsourcing

import org.opentest4j.AssertionFailedError
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Retry {
    val log: Logger = LoggerFactory.getLogger(Retry::class.java)

    /**
     * Non-suspended, retry on AssertionFailedError
     */
    inline fun <T> retryOnAssertionFailure(times: Int, retryDelay: Long = 100L, block: (Int) -> T): T {
        var ex: Throwable? = null
        repeat(times) { i ->
            try {
                return block(i)
            } catch (e: AssertionFailedError) {
                log.trace("Retry #{} failed with {}", i, e)
                Thread.sleep(retryDelay)
                ex = e
            }
        }
        throw ex!! /* rethrow last failure */
    }
}
