package net.csust.webo.domain

import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Int> {
    fun findUserByUsername(username: String): User?
}
