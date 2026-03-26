package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IPv6Range
import org.springframework.stereotype.Service

interface IPv6RangeService {
    fun create(range: IPv6Range): IPv6Range
    fun update(range: IPv6Range): IPv6Range
    fun delete(id: Long)
    fun findById(id: Long): IPv6Range?
    fun findAllByDcId(dcId: Long): List<IPv6Range>
    fun findActiveByDcId(dcId: Long): List<IPv6Range>
}
