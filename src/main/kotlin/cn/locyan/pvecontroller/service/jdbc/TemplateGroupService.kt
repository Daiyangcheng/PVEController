package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.TemplateGroup

interface TemplateGroupService {
    fun create(group: TemplateGroup): TemplateGroup
    fun update(group: TemplateGroup): TemplateGroup
    fun delete(id: Long)
    fun findById(id: Long): TemplateGroup?
    fun findAllByNodeId(nodeId: Long): List<TemplateGroup>
    fun findByNameAndNodeId(name: String, nodeId: Long): TemplateGroup?
}
