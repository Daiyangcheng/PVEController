package cn.locyan.pvecontroller.model

import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "router_os_api")
class RouterOSApi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Column(name = "host", nullable = false)
    open var host: String? = null

    @Column(name = "ssl", nullable = false)
    open var ssl: Boolean? = null

    @Column(name = "`user`", nullable = false)
    open var user: String? = null

    @Column(name = "password", nullable = false)
    open var password: String? = null

    @Column(name = "port", nullable = false)
    open var port: Int? = null
}