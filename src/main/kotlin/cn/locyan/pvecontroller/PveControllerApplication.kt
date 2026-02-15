package cn.locyan.pvecontroller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class, ErrorMvcAutoConfiguration::class, UserDetailsServiceAutoConfiguration::class])
@EnableAspectJAutoProxy
class PveControllerApplication

fun main(args: Array<String>) {
    runApplication<PveControllerApplication>(*args)
}
