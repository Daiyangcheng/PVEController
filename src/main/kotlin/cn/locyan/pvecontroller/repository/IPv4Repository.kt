package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.IPv4
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface IPv4Repository : JpaRepository<IPv4, Long> {
    fun findAllByNodeId(dcId: Long): List<IPv4>
    fun findByNodeIdAndIsAllocatedFalse(dcId: Long): List<IPv4>

    @Query(
        value = """
            select * from ipv4
            where node_id = :nodeId and is_allocated = false
            order by id asc
            limit 1
            for update skip locked
        """,
        nativeQuery = true
    )
    fun lockFirstAvailableByNodeId(@Param("nodeId") nodeId: Long): IPv4?
    fun findByServerId(serverId: Long): IPv4?

    @Query(
        value = """
            select * from ipv4
            where ip_group_id = :ipGroupId and is_allocated = false
            order by id asc
            limit 1
            for update skip locked
        """,
        nativeQuery = true
    )
    fun lockFirstAvailableByIpGroupId(@Param("ipGroupId") ipGroupId: Long): IPv4?

    fun findAllByIpGroupId(ipGroupId: Long): List<IPv4>
}
