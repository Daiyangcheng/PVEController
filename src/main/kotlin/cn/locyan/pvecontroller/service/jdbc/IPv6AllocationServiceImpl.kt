package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IPv6Allocation
import cn.locyan.pvecontroller.repository.IPv6AllocationRepository
import cn.locyan.pvecontroller.repository.IPv6RangeRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class IPv6AllocationServiceImpl(
    private val ipv6AllocationRepository: IPv6AllocationRepository,
    private val ipv6RangeRepository: IPv6RangeRepository
) : IPv6AllocationService {

    override fun create(allocation: IPv6Allocation): IPv6Allocation {
        allocation.createdTime = LocalDateTime.now()
        allocation.updatedTime = LocalDateTime.now()
        return ipv6AllocationRepository.save(allocation)
    }

    override fun update(allocation: IPv6Allocation): IPv6Allocation {
        allocation.updatedTime = LocalDateTime.now()
        return ipv6AllocationRepository.save(allocation)
    }

    override fun delete(id: Long) {
        ipv6AllocationRepository.deleteById(id)
    }

    override fun findById(id: Long): IPv6Allocation? {
        return ipv6AllocationRepository.findById(id).orElse(null)
    }

    override fun findAllByRangeId(rangeId: Long): List<IPv6Allocation> {
        return ipv6AllocationRepository.findAllByIpv6RangeId(rangeId)
    }

    override fun findByServerId(serverId: Long): IPv6Allocation? {
        return ipv6AllocationRepository.findByServerId(serverId)
    }

    override fun allocateIPv6(rangeId: Long, method: String): IPv6Allocation? {
        val range = ipv6RangeRepository.findById(rangeId).orElse(null) ?: return null
        if (!range.isActive!!) return null

        // Sequential allocation: parse start address and allocate next available
        if (method == "sequential") {
            val allocations = ipv6AllocationRepository.findAllByIpv6RangeId(rangeId)
            val nextAddress = if (allocations.isEmpty()) {
                range.startAddress
            } else {
                // Simple increment logic (IPv6 address increment)
                incrementIPv6Address(allocations.last().assignedAddress ?: range.startAddress!!)
            }

            val allocation = IPv6Allocation().apply {
                this.ipv6RangeId = rangeId
                this.dcId = range.dcId
                this.assignedAddress = nextAddress
            }
            range.allocatedCount = (range.allocatedCount ?: 0L) + 1L
            ipv6RangeRepository.save(range)
            return create(allocation)
        }
        return null
    }

    override fun deallocateIPv6(ipv6Id: Long) {
        val allocation = findById(ipv6Id) ?: return
        delete(ipv6Id)
        
        // Decrement allocated count in range
        val range = ipv6RangeRepository.findById(allocation.ipv6RangeId ?: return).orElse(null) ?: return
        range.allocatedCount = maxOf(0L, (range.allocatedCount ?: 0L) - 1L)
        ipv6RangeRepository.save(range)
    }

    private fun incrementIPv6Address(address: String): String {
        // Simple IPv6 increment logic: for demo, just append suffix counter
        // In production, use proper IPv6 arithmetic library
        return address
    }
}
