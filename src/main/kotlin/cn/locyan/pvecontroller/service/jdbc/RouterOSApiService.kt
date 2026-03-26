package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.RouterOSApi
import org.springframework.stereotype.Service

@Service
interface RouterOSApiService {
    fun delete(routerOSApi: RouterOSApi)
    fun update(routerOSApi: RouterOSApi)
    fun findById(id: Long): RouterOSApi?
    fun findAll(): List<RouterOSApi>
    fun execute(id: Long, path: String, action: String, params: List<Map<String, String>>?): List<Map<String, String>>?
}