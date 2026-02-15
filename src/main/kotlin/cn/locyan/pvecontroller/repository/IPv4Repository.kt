package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.IPv4
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IPv4Repository : JpaRepository<IPv4, Long> {
    fun findAllByNodeId(dcId: Long): List<IPv4>
    fun findByNodeIdAndIsAllocatedFalse(dcId: Long): List<IPv4>
}
