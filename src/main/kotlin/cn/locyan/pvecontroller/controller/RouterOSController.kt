package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.service.jdbc.RouterOSApiService
import org.springframework.web.bind.annotation.RestController

@RestController
class RouterOSController(
    private val routerOSApiService: RouterOSApiService
) {

}