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
@Table(name = "billing_records")
class BillingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "user_id", nullable = false)
    var userId: Long? = null

    @Column(name = "server_id")
    var serverId: Long? = null

    @Column(name = "dc_id", nullable = false)
    var dcId: Long? = null

    @Column(name = "billing_type")
    var billingType: String? = null

    @Column(name = "amount")
    var amount: java.math.BigDecimal? = null

    @Column(name = "quantity")
    var quantity: Int? = null

    @Column(name = "unit_price")
    var unitPrice: java.math.BigDecimal? = null

    @Column(name = "billing_period_start")
    var billingPeriodStart: LocalDateTime? = null

    @Column(name = "billing_period_end")
    var billingPeriodEnd: LocalDateTime? = null

    @ColumnDefault("false")
    @Column(name = "is_paid")
    var isPaid: Boolean? = false

    @Column(name = "paid_time")
    var paidTime: LocalDateTime? = null

    @Column(name = "remark")
    var remark: String? = null

    @Column(name = "created_time", nullable = false)
    var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    var updatedTime: LocalDateTime? = null
}
