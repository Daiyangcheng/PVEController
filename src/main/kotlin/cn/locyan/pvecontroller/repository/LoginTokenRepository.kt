package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.LoginToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoginTokenRepository : JpaRepository<LoginToken, Long>