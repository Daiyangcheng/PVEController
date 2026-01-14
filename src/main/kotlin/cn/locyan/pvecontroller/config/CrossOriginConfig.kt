package cn.locyan.pvecontroller.config

import jakarta.servlet.*
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import java.io.IOException
/**
 * 跨域配置
 */
@WebFilter(filterName = "CorsFilter")
@Configuration
class CrossOriginConfig : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest?, res: ServletResponse?, chain: FilterChain) {
        val request = req as HttpServletRequest
        val response = res as HttpServletResponse

        val origin = request.getHeader("Origin")

        if (origin == null) {
            chain.doFilter(req, res)
            return
        }
        response.setHeader("Access-Control-Allow-Origin", "*")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.setHeader("Access-Control-Max-Age", "3600")
        response.setHeader("Access-Control-Allow-Headers", "*")


        if (request.method.equals("OPTIONS", ignoreCase = true)) {
            response.status = HttpServletResponse.SC_OK
            return
        }

        chain.doFilter(req, res)
    }
}