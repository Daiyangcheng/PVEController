package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.RouterOSRules

interface RouterOSRulesService {
    fun update(routerOSRules: RouterOSRules)
    fun delete(routerOSRules: RouterOSRules)
    fun findAll(): List<RouterOSRules>
    fun findById(id: Long): RouterOSRules?
    fun findByTemplateId(templateId: Long): List<RouterOSRules>
    fun findByApiId(apiId: Long): List<RouterOSRules>
}