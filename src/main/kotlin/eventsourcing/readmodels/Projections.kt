package eventsourcing.readmodels

object InconsistentReadModelException : Exception("The read model is in an inconsistent state")
