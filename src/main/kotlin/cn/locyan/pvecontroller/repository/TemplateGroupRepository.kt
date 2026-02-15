package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.TemplateGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TemplateGroupRepository : JpaRepository<TemplateGroup, Long> {
    fun findAllByDcId(dcId: Long): List<TemplateGroup>
    fun findByNameAndDcId(name: String, dcId: Long): TemplateGroup?
}
