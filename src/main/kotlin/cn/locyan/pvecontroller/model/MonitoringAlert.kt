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
open class MonitoringAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "dc_id", nullable = false)
    open var dcId: Long? = null

    @Column(name = "resource_type")
    open var resourceType: String? = null

    @Column(name = "resource_id")
    open var resourceId: Long? = null

    @Column(name = "alert_type")
    open var alertType: String? = null

    @Column(name = "severity")
    open var severity: String? = null

    @Column(name = "message")
    open var message: String? = null

    @Column(name = "current_value")
    open var currentValue: String? = null

    @Column(name = "threshold_value")
    open var thresholdValue: String? = null

    @ColumnDefault("false")
    @Column(name = "is_resolved")
    open var isResolved: Boolean? = false

    @Column(name = "resolved_time")
    open var resolvedTime: LocalDateTime? = null

    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    open var updatedTime: LocalDateTime? = null
}
