package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IPv4

interface IPv4Service {
    fun create(ipv4: IPv4): IPv4
    fun update(ipv4: IPv4): IPv4
    fun delete(id: Long)
    fun findById(id: Long): IPv4?
    fun findAllByNodeId(nodeId: Long): List<IPv4>
    fun findAvailableByNodeId(nodeId: Long): List<IPv4>
    fun allocateIP(nodeId: Long, vmId: Long): IPv4?
    fun deallocateIP(ipId: Long)
    fun findByServerId(serverId: Long): IPv4?
    fun allocateIPByGroup(ipGroupId: Long, vmId: Long): IPv4?
}
