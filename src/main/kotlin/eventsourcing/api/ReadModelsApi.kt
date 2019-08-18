package eventsourcing.api

import org.springframework.http.ResponseEntity

fun <R> notFoundResponse() : ResponseEntity<R> = ResponseEntity.notFound().build()