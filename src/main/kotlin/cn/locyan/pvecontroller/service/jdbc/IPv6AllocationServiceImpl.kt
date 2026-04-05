package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IPv6Allocation
import cn.locyan.pvecontroller.repository.IPv6AllocationRepository
import cn.locyan.pvecontroller.repository.IPv6RangeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.net.InetAddress
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

    @Transactional
    override fun allocateIPv6(rangeId: Long): IPv6Allocation? {
        val range = ipv6RangeRepository.findById(rangeId).orElse(null) ?: return null
        if (range.isActive != true) {
            return null
        }

        val startNumber = toIpv6Number(range.startAddress ?: return null) ?: return null
        val endNumber = toIpv6Number(range.endAddress ?: return null) ?: return null
        if (startNumber > endNumber) {
            return null
        }

        val allocatedNumbers = buildList {
            for (allocation in ipv6AllocationRepository.findAllByIpv6RangeId(rangeId)) {
                val assignedAddress = allocation.assignedAddress ?: return null
                val parsedAddress = toIpv6Number(assignedAddress) ?: return null
                add(parsedAddress)
            }
        }

        val nextNumber = allocatedNumbers
            .maxOrNull()
            ?.add(BigInteger.ONE)
            ?: startNumber

        if (nextNumber > endNumber) {
            return null
        }

        val nextAddress = toIpv6String(nextNumber) ?: return null
        val allocation = IPv6Allocation().apply {
            this.ipv6RangeId = rangeId
            this.assignedAddress = nextAddress
            this.isAllocated = true
            this.allocationMethod = "auto"
        }

        range.allocatedCount = (range.allocatedCount ?: 0L) + 1L
        ipv6RangeRepository.save(range)
        return create(allocation)
    }

    @Transactional
    override fun deallocateIPv6(ipv6Id: Long) {
        val allocation = findById(ipv6Id) ?: return
        delete(ipv6Id)

        val range = ipv6RangeRepository.findById(allocation.ipv6RangeId ?: return).orElse(null) ?: return
        range.allocatedCount = maxOf(0L, (range.allocatedCount ?: 0L) - 1L)
        ipv6RangeRepository.save(range)
    }

    private fun toIpv6Number(address: String): BigInteger? {
        return try {
            val bytes = InetAddress.getByName(address).address
            if (bytes.size != 16) {
                null
            } else {
                BigInteger(1, bytes)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun toIpv6String(value: BigInteger): String? {
        val raw = value.toByteArray()
        val source = when {
            raw.size == 16 -> raw
            raw.size < 16 -> ByteArray(16 - raw.size) + raw
            raw.size == 17 && raw.first() == 0.toByte() -> raw.copyOfRange(1, 17)
            else -> return null
        }
        val bytes = ByteArray(16)
        System.arraycopy(source, 0, bytes, 0, 16)

        return try {
            InetAddress.getByAddress(bytes).hostAddress
        } catch (_: Exception) {
            null
        }
    }
}
