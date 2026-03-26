package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.TemplateGroup
import org.springframework.stereotype.Service

interface TemplateGroupService {
    fun create(group: TemplateGroup): TemplateGroup
    fun update(group: TemplateGroup): TemplateGroup
    fun delete(id: Long)
    fun findById(id: Long): TemplateGroup?
    fun findAllByDcId(dcId: Long): List<TemplateGroup>
    fun findByNameAndDcId(name: String, dcId: Long): TemplateGroup?
}
