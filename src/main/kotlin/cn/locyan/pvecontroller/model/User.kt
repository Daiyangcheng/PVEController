package cn.locyan.pvecontroller.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.time.Instant

@Entity
@Table(name = "users")
open class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "username", nullable = false)
    open var username: String? = null

    @Column(name = "password", nullable = false)
    open var password: String? = null

    @Column(name = "email", nullable = false)
    open var email: String? = null

    @Column(name = "reg_time", nullable = false)
    open var regTime: Instant? = null

    @Column(name = "update_time", nullable = false)
    open var updateTime: Instant? = null

    @Column(name = "last_login_time")
    open var lastLoginTime: Instant? = null

    @ColumnDefault("true")
    @Column(name = "status", nullable = false)
    open var status: Boolean? = null

}