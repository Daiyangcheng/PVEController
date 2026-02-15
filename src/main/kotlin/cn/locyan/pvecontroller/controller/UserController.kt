package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.LoginToken
import cn.locyan.pvecontroller.model.User
import cn.locyan.pvecontroller.service.jdbc.LoginTokenService
import cn.locyan.pvecontroller.service.jdbc.UserService
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import lombok.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController()
class UserController(
    private val userService: UserService,
    private val loginTokenService: LoginTokenService,
    private val passwordEncoder: PasswordEncoder,
    private val builder: ResponseBuilder = ResponseBuilder()
) {
    @PostMapping("/user/login")
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
            lt.apply {
                this.id = user.id
                this.loginToken = token
                this.ua = ua
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

    @PostMapping("/user/register")
    fun register(
        @RequestParam("username") username: String,
        @RequestParam("password") password: String,
        @RequestParam("confirm_password") confirmPassword: String,
        @RequestParam("email") email: String,
        @RequestHeader("User-Agent") ua: String,
    ): ResponseEntity<Response> {
        if (password != confirmPassword) return builder.badRequest()
            .message("两次输入的密码不一致")
            .build()
        var user = userService.findByUsername(username)
        if (user != null) return builder.forbidden()
            .message("该用户已存在")
            .build()
        user = userService.findByEmail(email)
        if (user != null) return builder.forbidden()
            .message("该邮箱已存在")
            .build()
        user = User()
        user.apply {
            this.username = username
            this.email = email
            this.password = password
            this.regTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
            this.lastLoginTime = null
            this.status = true
        }
        userService.update(user)
        return builder.ok().build()
    }
}