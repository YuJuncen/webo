package net.csust.webo.services.user

import net.csust.webo.domain.User
import net.csust.webo.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
        private val repo: UserRepository
) {
    @Transactional
    fun register(username: String,
                 password: String,
                 nickname: String,
                 email: String?) : User {
        val user = repo.save(User.register(username, password))
        user.nickname = nickname
        user.email = email
        user.follow(user)
        return user
    }

    fun getUserInfo(id: Int) : User? = repo.findById(id).orElse(null)
    fun getUserInfo(username: String) : User? = repo.findUserByUsername(username)

    fun getViewOfUser(username: String) : UserNameView? = getUserInfo(username)?.toNameView()
    @Deprecated("在用户的限界上下文（控制与访问上下文）之外，您不应该通过 Id 来引用用户，您应该使用 UserName。",
            replaceWith = ReplaceWith("getViewOfUser(username= TODO())"))
    fun getViewOfUserId(id: Int) = getUserInfo(id)?.toNameView()

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