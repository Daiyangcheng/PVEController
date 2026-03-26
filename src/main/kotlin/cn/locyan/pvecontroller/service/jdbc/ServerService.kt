package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Server
import org.springframework.stereotype.Service

interface ServerService {
    fun create(server: Server): Server
    fun update(server: Server): Server
    fun delete(server: Server)
    fun findById(id: Long): Server?
    fun findAllByDcId(dcId: Long): List<Server>
    fun findByUserId(userId: Long): List<Server>
    fun findByServerGroupId(groupId: Long): List<Server>
    fun findByTemplateId(templateId: Long): List<Server>
    fun findByIpId(ipId: Long): Server?
}
