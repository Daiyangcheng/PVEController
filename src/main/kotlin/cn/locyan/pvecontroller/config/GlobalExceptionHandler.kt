package cn.locyan.pvecontroller.config

import cn.locyan.pvecontroller.exception.UnauthorizedException
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val builder: ResponseBuilder
) {

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(e: UnauthorizedException): ResponseEntity<Response> {
        return builder.unauthorized().message(e.toString()).build()
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<Response> {
        return builder.exception().message(e.toString()).build()
    }
}