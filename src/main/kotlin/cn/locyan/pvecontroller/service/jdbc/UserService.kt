package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.User
import org.springframework.stereotype.Service

@Service
interface UserService {
    fun findById(userId: Long): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun update(user: User): User
    fun delete(user: User)
}