package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.IpGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IpGroupRepository : JpaRepository<IpGroup, Long> {
    fun findAllByNodeId(nodeId: Long): List<IpGroup>
}
