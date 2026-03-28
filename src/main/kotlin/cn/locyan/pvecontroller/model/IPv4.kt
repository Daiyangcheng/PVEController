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
@Table(name = "ipv4")
class IPv4 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "ip_address", nullable = false)
    var ipAddress: String? = null

    @Column(name = "gateway", nullable = false)
    var gateway: String? = null

    @Column(name = "netmask", nullable = false)
    var netmask: String? = null

    @Column(name = "vm_id")
    var vmId: Long? = null

    @Column(name = "server_id")
    var serverId: Long? = null

    @ColumnDefault("false")
    @Column(name = "is_allocated", nullable = false)
    var isAllocated: Boolean? = false

    @Column(name = "node_id")
    var nodeId: Long? = null

    @Column(name = "created_time", nullable = false)
    var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    var updatedTime: LocalDateTime? = null
}
