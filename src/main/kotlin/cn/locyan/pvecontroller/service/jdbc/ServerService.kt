package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Server

interface ServerService {
    fun create(server: Server): Server
    fun update(server: Server): Server
    fun delete(server: Server)
    fun findById(id: Long): Server?
    fun findByUserId(userId: Long): List<Server>
    fun findByTemplateId(templateId: Long): List<Server>
    fun findByIpId(ipId: Long): Server?
    fun findAll(): List<Server>
}
