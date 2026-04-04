package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.RouterOSApi

interface RouterOSApiService {
    fun delete(routerOSApi: RouterOSApi)
    fun update(routerOSApi: RouterOSApi)
    fun findById(id: Long): RouterOSApi?
    fun findAll(): List<RouterOSApi>
    fun execute(id: Long, path: String, action: String, params: List<Map<String, String>>?): List<Map<String, String>>?
    fun execute(id: Long, path: String, action: String, params: String): List<Map<String, String>>?
    fun testConn(host: String, port: Int, ssl: Boolean, user: String, password: String): Boolean
}