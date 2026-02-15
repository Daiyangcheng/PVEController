package cn.locyan.pvecontroller.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ipv6_allocations")
open class IPv6Allocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "dc_id", nullable = false)
    open var dcId: Long? = null

    @Column(name = "ipv6_range_id", nullable = false)
    open var ipv6RangeId: Long? = null

    @Column(name = "assigned_address", nullable = false)
    open var assignedAddress: String? = null

    @Column(name = "vm_id")
    open var vmId: Long? = null

    @Column(name = "server_id")
    open var serverId: Long? = null

    @ColumnDefault("false")
    @Column(name = "is_allocated", nullable = false)
    open var isAllocated: Boolean? = false

    @Column(name = "allocation_method")
    open var allocationMethod: String? = null

    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    open var updatedTime: LocalDateTime? = null
}
