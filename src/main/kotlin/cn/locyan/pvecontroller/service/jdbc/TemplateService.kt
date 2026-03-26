package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Template

interface TemplateService {
    fun create(template: Template): Template
    fun update(template: Template): Template
    fun delete(id: Long)
    fun findById(id: Long): Template?
    fun findByTemplateGroupId(groupId: Long): List<Template>
}
