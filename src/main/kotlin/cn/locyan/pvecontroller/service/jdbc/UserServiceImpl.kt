package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.User
import cn.locyan.pvecontroller.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    override fun findById(userId: Long): User? {
        return userRepository.findById(userId).orElse(null)
    }

    override fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username).orElse(null)
    }

    override fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email).orElse(null)
    }

    override fun add(user: User): User {
        return userRepository.save(user)
    }

    override fun update(user: User): User {
        return userRepository.save(user)
    }

    override fun delete(user: User) {
        return userRepository.delete(user)
    }
}