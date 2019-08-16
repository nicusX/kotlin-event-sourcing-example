package eventsourcing.readmodels

import java.lang.Exception

class InconsistentReadModelException : Exception("The read model is in an inconsistent state")