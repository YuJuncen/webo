package net.csust.webo.services.user

import net.csust.webo.domain.User
import net.csust.webo.domain.repositories.UserRepository
import org.springframework.data.repository.findByIdOrNull
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
        return user
    }

    fun getUserInfo(id: Int) : User? = repo.findById(id).orElse(null)
    fun getUserInfo(username: String) : User? = repo.findUserByUsername(username)

    fun getViewOfUser(username: String) : UserNameView? = getUserInfo(username)?.toNameView()
    fun getViewOfUserId(id: Int) = getUserInfo(id)?.toNameView()

    fun modifyUser(userId: Int, modifier: UserInfoModifier) : UserNameView {
        val user = repo.findByIdOrNull(userId)!!
        modifier.modify(user)
        return repo.save(user).toNameView()
    }
}