package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.RouterOSRules
import org.springframework.data.jpa.repository.JpaRepository

interface RouterOSRulesRepository : JpaRepository<RouterOSRules, Long> {
    fun findByTemplateId(templateId: Long): List<RouterOSRules>
    fun findByApiId(apiId: Long): List<RouterOSRules>
}