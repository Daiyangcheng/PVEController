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
open class IPv6Range {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "dc_id", nullable = false)
    open var dcId: Long? = null

    @Column(name = "start_address", nullable = false)
    open var startAddress: String? = null

    @Column(name = "end_address", nullable = false)
    open var endAddress: String? = null

    @Column(name = "gateway", nullable = false)
    open var gateway: String? = null

    @Column(name = "prefix_length")
    open var prefixLength: Int? = null

    @ColumnDefault("true")
    @Column(name = "is_active", nullable = false)
    open var isActive: Boolean? = true

    @ColumnDefault("0")
    @Column(name = "allocated_count")
    open var allocatedCount: Long? = 0

    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    open var updatedTime: LocalDateTime? = null
}
