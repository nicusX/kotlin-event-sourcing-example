package eventsourcing.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun serverErrorResponse(errorMessage: String = "Server Error"): ResponseEntity<ErrorResource> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResource(errorMessage))

fun unprocessableEntityResponse(errorMessage: String): ResponseEntity<ErrorResource> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ErrorResource(errorMessage))

fun notFoundResponse(errorMessage: String): ResponseEntity<ErrorResource> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResource(errorMessage))

fun conflictResponse(errorMessage: String) : ResponseEntity<ErrorResource> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResource(errorMessage))