package cn.locyan.pvecontroller.aspect

import cn.locyan.pvecontroller.annotation.Authentication
import cn.locyan.pvecontroller.service.jdbc.LoginTokenService
import cn.locyan.pvecontroller.shared.exception.UnauthorizedException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class AuthenticationAspect(
    private val loginTokenService: LoginTokenService
) {

    @Around("@annotation(authentication)")
    fun authentication(joinPoint: ProceedingJoinPoint, authentication: Authentication): Any? {
        val attributes: ServletRequestAttributes =
            RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = attributes.request

        // 请求参数
        val userId: Long = try {
            request.getParameter("user_id")?.toLong() ?: throw UnauthorizedException(null)
        } catch (_: NumberFormatException) {
            throw UnauthorizedException(null)
        }
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: throw UnauthorizedException(null)
        val userAgent = request.getHeader("User-Agent") ?: throw UnauthorizedException(null)

        val lt = loginTokenService.findById(userId) ?: throw UnauthorizedException(null)
        if (lt.loginToken != token) throw UnauthorizedException(null)
        if (lt.ua != userAgent) throw UnauthorizedException(null)
        return joinPoint.proceed()
    }
}