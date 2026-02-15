package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IPv4
import cn.locyan.pvecontroller.repository.IPv4Repository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class IPv4ServiceImpl(
    private val ipv4Repository: IPv4Repository
) : IPv4Service {
    
    override fun create(ipv4: IPv4): IPv4 {
        ipv4.createdTime = LocalDateTime.now()
        ipv4.updatedTime = LocalDateTime.now()
        return ipv4Repository.save(ipv4)
    }

    override fun update(ipv4: IPv4): IPv4 {
        ipv4.updatedTime = LocalDateTime.now()
        return ipv4Repository.save(ipv4)
    }

    override fun delete(id: Long) {
        ipv4Repository.deleteById(id)
    }

    override fun findById(id: Long): IPv4? {
        return ipv4Repository.findById(id).orElse(null)
    }

    override fun findAllByNodeId(nodeId: Long): List<IPv4> {
        return ipv4Repository.findAllByNodeId(nodeId)
    }

    override fun findAvailableByNodeId(nodeId: Long): List<IPv4> {
        return ipv4Repository.findByNodeIdAndIsAllocatedFalse(nodeId)
    }

    override fun allocateIP(nodeId: Long, vmId: Long): IPv4? {
        val availableIPs = findAvailableByNodeId(nodeId)
        return if (availableIPs.isNotEmpty()) {
            val ip = availableIPs.first()
            ip.isAllocated = true
            ip.vmId = vmId
            update(ip)
        } else {
            null
        }
    }

    // IP 地址已经由 DC 和 Node id 固定，因此 VMID 不需要判定
    override fun deallocateIP(ipId: Long) {
        val ip = findById(ipId)
        if (ip != null) {
            ip.isAllocated = false
            ip.vmId = null
            update(ip)
        }
    }
}
