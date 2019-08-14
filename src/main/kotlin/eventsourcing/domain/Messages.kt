package eventsourcing.domain

import java.time.Instant
import java.time.Instant.now

abstract class Message(val createdAt : Instant = now())
