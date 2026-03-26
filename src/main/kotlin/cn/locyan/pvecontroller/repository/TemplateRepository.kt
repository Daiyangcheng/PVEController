package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.Template
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TemplateRepository : JpaRepository<Template, Long> {
    fun findByTemplateGroupId(groupId: Long): List<Template>
}
