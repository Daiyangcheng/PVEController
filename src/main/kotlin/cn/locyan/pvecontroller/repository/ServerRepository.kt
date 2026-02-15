package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.Server
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServerRepository : JpaRepository<Server, Long> {
    fun findAllByDcId(dcId: Long): List<Server>
    fun findByUserId(userId: Long): List<Server>
    fun findByServerGroupId(groupId: Long): List<Server>
    fun findByTemplateId(templateId: Long): List<Server>
    fun findByIpId(ipId: Long): Server?
}
