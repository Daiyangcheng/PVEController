package cn.locyan.pvecontroller.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audit_logs")
open class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "user_id", nullable = false)
    open var userId: Long? = null

    @Column(name = "action", nullable = false)
    open var action: String? = null

    @Column(name = "resource_type")
    open var resourceType: String? = null

    @Column(name = "resource_id")
    open var resourceId: Long? = null

    @Column(name = "dc_id")
    open var dcId: Long? = null

    @Column(name = "details", columnDefinition = "TEXT")
    open var details: String? = null

    @Column(name = "ip_address")
    open var ipAddress: String? = null

    @Column(name = "status")
    open var status: String? = null

    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null
}
