package net.csust.webo.services.user

import net.csust.webo.domain.User
import net.csust.webo.domain.UserRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
        private val repo: UserRepository
) {
    @Transactional
    fun register(username: String, password: String, nickname: String) : User {
        val user = repo.save(User.register(username, password))
        user.nickname = nickname
        user.follow(user)
        return user
    }

    fun getUserInfo(id: Int) : User? = repo.findById(id).orElse(null)
    fun getUserInfo(username: String) : User? = repo.findUserByUsername(username)

    @Transactional
    fun addFollow(followerId: Int, followeeId: Int) {
        val follower = repo.findById(followerId).orElse(null)
        val followee = repo.findById(followeeId).orElse(null)
        follower.follow(followee)
        repo.save(followee)
        repo.save(follower)
    }

    fun getFollowings(userId: Int) : Set<*> {
        val user = repo.findById(userId).orElse(null)
        return user.following
    }
}