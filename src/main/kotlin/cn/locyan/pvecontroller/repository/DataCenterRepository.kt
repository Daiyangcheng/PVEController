package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.DataCenter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DataCenterRepository : JpaRepository<DataCenter, Long> {
    fun findByName(name: String): DataCenter?
}
