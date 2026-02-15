package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.ServerGroup
import org.springframework.data.jpa.repository.JpaRepository

interface ServerGroupRepository : JpaRepository<ServerGroup, Long> {
    fun findAllByDcId(dcId: Long): List<ServerGroup>
    fun findByUserId(userId: Long): List<ServerGroup>
}
