package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.IPv6Range
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IPv6RangeRepository : JpaRepository<IPv6Range, Long> {
    fun findAllByDcId(dcId: Long): List<IPv6Range>
    fun findByDcIdAndIsActiveTrue(dcId: Long): List<IPv6Range>
}
