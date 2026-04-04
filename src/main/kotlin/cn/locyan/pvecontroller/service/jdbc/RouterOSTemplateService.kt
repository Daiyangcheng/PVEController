package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.RouterOSTemplate

interface RouterOSTemplateService {
    fun update(routerOSTemplate: RouterOSTemplate)
    fun delete(routerOSTemplate: RouterOSTemplate)
    fun findAll(): List<RouterOSTemplate>
    fun findById(id: Long): RouterOSTemplate?
    fun findByNodeId(nodeId: Long): List<RouterOSTemplate>
    fun findByApiId(apiId: Long): List<RouterOSTemplate>
}
