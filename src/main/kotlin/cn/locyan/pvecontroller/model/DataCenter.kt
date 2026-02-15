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
@Table(name = "datacenter")
open class DataCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Column(name = "host", nullable = false)
    open var host: String? = null

    @Column(name = "port", nullable = false)
    open var port: Int? = 8006

    @Column(name = "ssl", nullable = false)
    open var ssl: Boolean? = true

    @Column(name = "token_id", nullable = false)
    open var tokenId: String? = null

    @Column(name = "token_secret", nullable = false)
    open var tokenSecret: String? = null

    @ColumnDefault("true")
    @Column(name = "status", nullable = false)
    open var status: Boolean? = null

    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    open var updatedTime: LocalDateTime? = null
}
