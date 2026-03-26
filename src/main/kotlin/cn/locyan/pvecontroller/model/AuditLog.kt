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
class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "user_id", nullable = false)
    var userId: Long? = null

    @Column(name = "action", nullable = false)
    var action: String? = null

    @Column(name = "resource_type")
    var resourceType: String? = null

    @Column(name = "resource_id")
    var resourceId: Long? = null

    @Column(name = "dc_id")
    var dcId: Long? = null

    @Column(name = "details", columnDefinition = "TEXT")
    var details: String? = null

    @Column(name = "ip_address")
    var ipAddress: String? = null

    @Column(name = "status")
    var status: String? = null

    @Column(name = "created_time", nullable = false)
    var createdTime: LocalDateTime? = null
}
