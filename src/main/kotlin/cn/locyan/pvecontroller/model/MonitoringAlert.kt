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
@Table(name = "monitoring_alerts")
class MonitoringAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "dc_id", nullable = false)
    var dcId: Long? = null

    @Column(name = "resource_type")
    var resourceType: String? = null

    @Column(name = "resource_id")
    var resourceId: Long? = null

    @Column(name = "alert_type")
    var alertType: String? = null

    @Column(name = "severity")
    var severity: String? = null

    @Column(name = "message")
    var message: String? = null

    @Column(name = "current_value")
    var currentValue: String? = null

    @Column(name = "threshold_value")
    var thresholdValue: String? = null

    @ColumnDefault("false")
    @Column(name = "is_resolved")
    var isResolved: Boolean? = false

    @Column(name = "resolved_time")
    var resolvedTime: LocalDateTime? = null

    @Column(name = "created_time", nullable = false)
    var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    var updatedTime: LocalDateTime? = null
}
