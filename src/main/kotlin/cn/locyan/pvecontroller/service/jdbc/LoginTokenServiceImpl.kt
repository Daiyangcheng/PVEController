package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.LoginToken
import cn.locyan.pvecontroller.repository.LoginTokenRepository
import org.springframework.stereotype.Service

@Service
class LoginTokenServiceImpl(
    private val loginTokenRepository: LoginTokenRepository,
) : LoginTokenService {
    override fun findById(id: Long): LoginToken? {
        return loginTokenRepository.findById(id).orElse(null)
    }

    override fun update(loginToken: LoginToken) {
        loginTokenRepository.save(loginToken)
    }

    override fun delete(loginToken: LoginToken) {
        loginTokenRepository.delete(loginToken)
    }
}