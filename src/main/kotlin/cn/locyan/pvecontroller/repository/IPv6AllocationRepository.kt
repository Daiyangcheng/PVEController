package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.IPv6Allocation
import org.springframework.data.jpa.repository.JpaRepository

interface IPv6AllocationRepository : JpaRepository<IPv6Allocation, Long> {
    fun findAllByIpv6RangeId(rangeId: Long): List<IPv6Allocation>
    fun findByServerId(serverId: Long): IPv6Allocation?
}
