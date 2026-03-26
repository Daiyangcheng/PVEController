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
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "username", nullable = false)
    var username: String? = null

    @Column(name = "password", nullable = false)
    var password: String? = null

    @Column(name = "email", nullable = false)
    var email: String? = null

    @Column(name = "reg_time", nullable = false)
    var regTime: LocalDateTime? = null

    @Column(name = "update_time", nullable = false)
    var updateTime: LocalDateTime? = null

    @Column(name = "last_login_time")
    var lastLoginTime: LocalDateTime? = null

    @ColumnDefault("true")
    @Column(name = "status", nullable = false)
    var status: Boolean? = null

}