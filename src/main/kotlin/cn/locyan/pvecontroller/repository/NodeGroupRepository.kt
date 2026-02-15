package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.NodeGroup
import org.springframework.data.jpa.repository.JpaRepository

interface NodeGroupRepository : JpaRepository<NodeGroup, Long> {
    fun findAllByDcId(dcId: Long): List<NodeGroup>
}
