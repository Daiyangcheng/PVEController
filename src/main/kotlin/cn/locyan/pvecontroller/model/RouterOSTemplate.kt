package cn.locyan.pvecontroller.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "router_os_template")
class RouterOSTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private var id: Long? = null

    @Column(name = "template_name", nullable = false)
    private var templateName: String? = null

    /*
    * 哪个 API 可以用
    */
    @Column(name = "api_id", nullable = false)
    private var apiId: Long? = null

    @Column(name = "node_id", nullable = false)
    private var nodeId: Long? = null

    @Column(name = "path", nullable = false)
    private var path: String? = null

    @Column(name = "action", nullable = false)
    private var action: String? = null

    @Column(name = "template", nullable = false)
    private var template: String? = null

    @Column(name = "values", nullable = false)
    private var values: String? = null
}