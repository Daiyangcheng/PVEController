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
open class BillingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "user_id", nullable = false)
    open var userId: Long? = null

    @Column(name = "server_id")
    open var serverId: Long? = null

    @Column(name = "dc_id", nullable = false)
    open var dcId: Long? = null

    @Column(name = "billing_type")
    open var billingType: String? = null

    @Column(name = "amount")
    open var amount: java.math.BigDecimal? = null

    @Column(name = "quantity")
    open var quantity: Int? = null

    @Column(name = "unit_price")
    open var unitPrice: java.math.BigDecimal? = null

    @Column(name = "billing_period_start")
    open var billingPeriodStart: LocalDateTime? = null

    @Column(name = "billing_period_end")
    open var billingPeriodEnd: LocalDateTime? = null

    @ColumnDefault("false")
    @Column(name = "is_paid")
    open var isPaid: Boolean? = false

    @Column(name = "paid_time")
    open var paidTime: LocalDateTime? = null

    @Column(name = "remark")
    open var remark: String? = null

    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    open var updatedTime: LocalDateTime? = null
}
