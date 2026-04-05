package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.LoginToken
import cn.locyan.pvecontroller.model.User
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
import java.time.LocalDateTime
import java.util.UUID

@RestController()
class UserController(
    private val userService: UserService,
    private val loginTokenService: LoginTokenService,
    private val passwordEncoder: PasswordEncoder,
    private val builder: ResponseBuilder = ResponseBuilder()
) {
    data class UserSyncResponse(
        val id: Long?,
        val username: String?,
        val email: String?,
        val created: Boolean
    )

    @PostMapping("/user/login")
    fun login(
        @RequestParam("username") username: String,
        @RequestParam("password") password: String,
        @RequestHeader("User-Agent") ua: String,
    ): ResponseEntity<Response> {
        val user = userService.findByUsername(username) ?: return builder.forbidden()
            .message("账号或密码错误")
            .build()
        val passwordMatches = passwordEncoder.matches(password, user.password) || password == user.password
        if (passwordMatches) {
            if (password == user.password) {
                user.password = passwordEncoder.encode(password)
                user.updateTime = LocalDateTime.now()
                userService.update(user)
            }
            val lt = LoginToken()
            val token = UUID.randomUUID().toString()
            lt.apply {
                this.id = user.id
                this.loginToken = token
                this.ua = ua
            }
            loginTokenService.update(lt)

            data class Response(
                val token: String,
                val userId: Long?,
                val username: String?,
                val email: String?,
            )

            val rs = Response(
                token = token,
                userId = user.id,
                username = user.username,
                email = user.email,
            )

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
        val normalizedEmail = email.trim().lowercase()
        if (password != confirmPassword) return builder.badRequest()
            .message("两次输入的密码不一致")
            .build()
        var user = userService.findByUsername(username)
        if (user != null) return builder.forbidden()
            .message("该用户已存在")
            .build()
        user = userService.findByEmailIgnoreCase(normalizedEmail)
        if (user != null) return builder.forbidden()
            .message("该邮箱已存在")
            .build()
        user = User()
        user.apply {
            this.username = username
            this.email = normalizedEmail
            this.password = passwordEncoder.encode(password)
            this.regTime = LocalDateTime.now()
            this.updateTime = LocalDateTime.now()
            this.lastLoginTime = null
            this.status = true
        }
        userService.update(user)
        return builder.ok().build()
    }

    @PostMapping("/user/paymenter-sync")
    fun syncPaymenterUser(
        @RequestParam("email") email: String,
        @RequestParam(value = "username", required = false) username: String? = null,
        @RequestParam(value = "password", required = false) password: String? = null,
    ): ResponseEntity<Response> {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank()) {
            return builder.badRequest().message("Email cannot be empty").build()
        }

        val existingUser = userService.findByEmailIgnoreCase(normalizedEmail)
        if (existingUser != null) {
            return builder.ok()
                .message("User already synced")
                .data(
                    UserSyncResponse(
                        id = existingUser.id,
                        username = existingUser.username,
                        email = existingUser.email,
                        created = false
                    )
                )
                .build()
        }

        val baseUsername = buildBaseUsername(username, normalizedEmail)
        val resolvedUsername = resolveUniqueUsername(baseUsername)
        val rawPassword = password?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val now = LocalDateTime.now()

        val createdUser = userService.update(
            User().apply {
                this.username = resolvedUsername
                this.email = normalizedEmail
                this.password = passwordEncoder.encode(rawPassword)
                this.regTime = now
                this.updateTime = now
                this.lastLoginTime = null
                this.status = true
            }
        )

        return builder.ok()
            .message("User synced successfully")
            .data(
                UserSyncResponse(
                    id = createdUser.id,
                    username = createdUser.username,
                    email = createdUser.email,
                    created = true
                )
            )
            .build()
    }

    private fun buildBaseUsername(username: String?, email: String): String {
        val preferred = username?.trim().orEmpty()
        if (preferred.isNotBlank()) {
            return sanitizeUsername(preferred)
        }

        return sanitizeUsername(email.substringBefore("@"))
    }

    private fun sanitizeUsername(value: String): String {
        val sanitized = value
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .trim('_', '.', '-')
            .take(24)

        return sanitized.ifBlank { "paymenter_user" }
    }

    private fun resolveUniqueUsername(base: String): String {
        if (userService.findByUsername(base) == null) {
            return base
        }

        var index = 1
        while (true) {
            val suffix = "_$index"
            val truncatedBase = base.take((24 - suffix.length).coerceAtLeast(1))
            val candidate = "${truncatedBase}$suffix"
            if (userService.findByUsername(candidate) == null) {
                return candidate
            }
            index++
        }
    }
}
