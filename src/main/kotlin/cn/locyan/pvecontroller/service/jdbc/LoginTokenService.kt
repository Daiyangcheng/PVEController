package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.LoginToken

interface LoginTokenService {
    fun findById(id: Long): LoginToken?
    fun update(loginToken: LoginToken)
    fun delete(loginToken: LoginToken)
}