package net.csust.webo.domain.repositories

import net.csust.webo.domain.User
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Int> {
    fun findUserByUsername(username: String): User?
}
