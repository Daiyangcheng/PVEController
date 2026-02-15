package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.Template
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TemplateRepository : JpaRepository<Template, Long> {
    fun findAllByDcId(dcId: Long): List<Template>
    fun findByTemplateIdAndDcId(templateId: Long, dcId: Long): Optional<Template>
    fun findByTemplateGroupId(groupId: Long): List<Template>
}
