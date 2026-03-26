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
    var name: String? = null

    @Column(name = "host", nullable = false)
    var host: String? = null

    @Column(name = "ssl", nullable = false)
    var ssl: Boolean? = null

    @Column(name = "`user`", nullable = false)
    var user: String? = null

    @Column(name = "password", nullable = false)
    var password: String? = null

    @Column(name = "port", nullable = false)
    var port: Int? = null
}