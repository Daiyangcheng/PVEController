package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IndexController {

    private val builder: ResponseBuilder = ResponseBuilder()

    @RequestMapping
    fun index(): ResponseEntity<Response> {
        return builder.ok()
            .message("Welcome to use LoCyan Cloud PVE Controller!")
            .build()
    }
}