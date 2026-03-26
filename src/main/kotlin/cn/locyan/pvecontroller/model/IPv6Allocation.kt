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
class IPv6Allocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "dc_id", nullable = false)
    var dcId: Long? = null

    @Column(name = "ipv6_range_id", nullable = false)
    var ipv6RangeId: Long? = null

    @Column(name = "assigned_address", nullable = false)
    var assignedAddress: String? = null

    @Column(name = "vm_id")
    var vmId: Long? = null

    @Column(name = "server_id")
    var serverId: Long? = null

    @ColumnDefault("false")
    @Column(name = "is_allocated", nullable = false)
    var isAllocated: Boolean? = false

    @Column(name = "allocation_method")
    var allocationMethod: String? = null

    @Column(name = "created_time", nullable = false)
    var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    var updatedTime: LocalDateTime? = null
}
