package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IPv6Allocation

interface IPv6AllocationService {
    fun create(allocation: IPv6Allocation): IPv6Allocation
    fun update(allocation: IPv6Allocation): IPv6Allocation
    fun delete(id: Long)
    fun findById(id: Long): IPv6Allocation?
    fun findAllByRangeId(rangeId: Long): List<IPv6Allocation>
    fun findByServerId(serverId: Long): IPv6Allocation?
    fun allocateIPv6(rangeId: Long): IPv6Allocation?
    fun deallocateIPv6(ipv6Id: Long)
}
