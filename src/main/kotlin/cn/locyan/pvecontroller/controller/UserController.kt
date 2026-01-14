package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.annotation.Authentication
import cn.locyan.pvecontroller.model.LoginToken
import cn.locyan.pvecontroller.service.jdbc.LoginTokenService
import cn.locyan.pvecontroller.service.jdbc.UserService
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController("/user")
class UserController(
    private val userService: UserService,
    private val loginTokenService: LoginTokenService,
    private val passwordEncoder: PasswordEncoder,
    private val builder: ResponseBuilder = ResponseBuilder()
) {
    @PostMapping("/login")
    fun login(
        @RequestParam("username") username: String,
        @RequestParam("password") password: String,
        @RequestHeader("User-Agent") ua: String,
    ): ResponseEntity<Response> {
        val user = userService.findByUsername(username) ?: return builder.forbidden()
            .message("账号或密码错误")
            .build()
        if (passwordEncoder.matches(password, user.password)) {
            val lt = LoginToken()
            val token = UUID.randomUUID().toString()
            lt.let {
                it.id = user.id
                it.loginToken = token
                it.ua = ua
            }
            loginTokenService.update(lt)

            data class Response(
                val token: String
            )

            val rs = Response(token = token)

            return builder.ok().data(rs).build()
        } else {
            return builder.forbidden()
                .message("账号或密码错误")
                .build()
        }
    }
}