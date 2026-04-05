package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.IPv6Range
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface IPv6RangeRepository : JpaRepository<IPv6Range, Long> {
    fun findAllByNodeId(nodeId: Long): List<IPv6Range>
    fun findByNodeIdAndIsActiveTrue(nodeId: Long): List<IPv6Range>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from IPv6Range r where r.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): IPv6Range?
}
