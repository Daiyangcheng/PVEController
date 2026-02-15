package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.ServerGroup

interface ServerGroupService {
    fun create(serverGroup: ServerGroup): ServerGroup
    fun update(serverGroup: ServerGroup): ServerGroup
    fun delete(id: Long)
    fun findById(id: Long): ServerGroup?
    fun findAllByDcId(dcId: Long): List<ServerGroup>
    fun findByUserId(userId: Long): List<ServerGroup>
}
