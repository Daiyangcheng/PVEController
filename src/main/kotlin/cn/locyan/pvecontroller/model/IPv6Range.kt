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
@Table(name = "ipv6_ranges")
class IPv6Range {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "node_id", nullable = false)
    var nodeId: Long? = null

    @Column(name = "start_address", nullable = false)
    var startAddress: String? = null

    @Column(name = "end_address", nullable = false)
    var endAddress: String? = null

    @Column(name = "gateway", nullable = false)
    var gateway: String? = null

    @Column(name = "prefix_length")
    var prefixLength: Int? = null

    @ColumnDefault("true")
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean? = true

    @ColumnDefault("0")
    @Column(name = "allocated_count")
    var allocatedCount: Long? = 0

    @Column(name = "created_time", nullable = false)
    var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    var updatedTime: LocalDateTime? = null
}
