package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IPv6Allocation
import cn.locyan.pvecontroller.repository.IPv6AllocationRepository
import cn.locyan.pvecontroller.repository.IPv6RangeRepository
import inet.ipaddr.AddressStringException
import inet.ipaddr.IPAddress
import inet.ipaddr.IPAddressString
import inet.ipaddr.ipv6.IPv6Address
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

    override fun allocateIPv6(rangeId: Long): IPv6Allocation? {
        val range = ipv6RangeRepository.findById(rangeId).orElse(null) ?: return null
        if (!range.isActive!!) return null

        // 顺序分配
        val allocations = ipv6AllocationRepository.findAllByIpv6RangeId(rangeId)
        val nextAddress = if (allocations.isEmpty()) {
            range.startAddress
        } else {
            incrementIPv6Address(allocations.last().assignedAddress ?: range.startAddress!!).toString()
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

    override fun deallocateIPv6(ipv6Id: Long) {
        val allocation = findById(ipv6Id) ?: return
        delete(ipv6Id)
        
        val range = ipv6RangeRepository.findById(allocation.ipv6RangeId ?: return).orElse(null) ?: return
        range.allocatedCount = maxOf(0L, (range.allocatedCount ?: 0L) - 1L)
        ipv6RangeRepository.save(range)
    }

    private fun incrementIPv6Address(address: String): IPv6Address? {
        var ip6Addr: IPAddress
        try {
            ip6Addr = IPAddressString(address).toAddress()
        } catch (e: AddressStringException){
            return null
        }
        val nextIp = ip6Addr.toIPv6().increment(1)
        return nextIp
    }
}
