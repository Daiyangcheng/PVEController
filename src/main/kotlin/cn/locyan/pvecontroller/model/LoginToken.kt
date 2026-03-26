package cn.locyan.pvecontroller.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "login_token")
class LoginToken {
    @Id
    @Column(name = "user_id", nullable = false)
    var id: Long? = null

    @Column(name = "login_token", nullable = false)
    var loginToken: String? = null

    @Column(name = "ua")
    var ua: String? = null

}