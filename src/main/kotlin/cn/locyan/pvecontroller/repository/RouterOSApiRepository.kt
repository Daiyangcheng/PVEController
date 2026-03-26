package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.RouterOSApi
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RouterOSApiRepository : JpaRepository<RouterOSApi, Long>