package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.RouterOSTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RouterOSTemplateRepository : JpaRepository<RouterOSTemplate, Long> {
    fun findByNodeId(nodeId: Long): List<RouterOSTemplate>
    fun findByApiId(apiId: Long): List<RouterOSTemplate>
}