package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IPv6Range
import cn.locyan.pvecontroller.repository.IPv6RangeRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class IPv6RangeServiceImpl(
    private val ipv6RangeRepository: IPv6RangeRepository
) : IPv6RangeService {

    override fun create(range: IPv6Range): IPv6Range {
        range.createdTime = LocalDateTime.now()
        range.updatedTime = LocalDateTime.now()
        if (range.isActive == null) {
            range.isActive = true
        }
        if (range.allocatedCount == null) {
            range.allocatedCount = 0L
        }
        return ipv6RangeRepository.save(range)
    }

    override fun update(range: IPv6Range): IPv6Range {
        range.updatedTime = LocalDateTime.now()
        return ipv6RangeRepository.save(range)
    }

    override fun delete(id: Long) {
        ipv6RangeRepository.deleteById(id)
    }

    override fun findById(id: Long): IPv6Range? {
        return ipv6RangeRepository.findById(id).orElse(null)
    }

    override fun findAllByDcId(dcId: Long): List<IPv6Range> {
        return ipv6RangeRepository.findAllByDcId(dcId)
    }

    override fun findActiveByDcId(dcId: Long): List<IPv6Range> {
        return ipv6RangeRepository.findByDcIdAndIsActiveTrue(dcId)
    }
}
