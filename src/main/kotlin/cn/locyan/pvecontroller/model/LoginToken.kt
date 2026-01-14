package cn.locyan.pvecontroller.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "login_token")
open class LoginToken {
    @Id
    @Column(name = "user_id", nullable = false)
    open var id: Long? = null

    @Column(name = "login_token", nullable = false)
    open var loginToken: String? = null

    @Column(name = "ua")
    open var ua: String? = null

}