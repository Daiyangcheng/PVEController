package cn.locyan.pvecontroller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class PveControllerApplication

fun main(args: Array<String>) {
    runApplication<PveControllerApplication>(*args)
}
