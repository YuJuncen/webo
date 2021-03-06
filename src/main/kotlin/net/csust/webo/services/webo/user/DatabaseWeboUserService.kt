package net.csust.webo.services.webo.user

import net.csust.webo.domain.repositories.UserRepository
import net.csust.webo.services.user.toNameView
import net.csust.webo.services.webo.views.WeboUserView
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DatabaseWeboUserService(
        val userRepository: UserRepository
) : WeboUserService {
    @Transactional
    override fun getUserOf(userId: Int): WeboUserView? {
        val user = userRepository.findByIdOrNull(userId) ?: return null
        val nameView = user.toNameView()
        val followingCount = user.following.size
        val followedByCount = user.followedBy.size
        return WeboUserView(nameView, followingCount, followedByCount)
    }

    override fun isFollowing(userId: Int, toUserId: Int): Boolean {
        return userRepository.findByIdOrNull(userId)?.following?.contains(toUserId) ?: false
    }

    fun countFollowing(userId: Int): Int {
        return userRepository.findByIdOrNull(userId)?.following?.size ?: -1
    }

    fun countFollowers(userId: Int): Int {
        return userRepository.findByIdOrNull(userId)?.followedBy?.size ?: -1
    }

    @Transactional
    override fun getFollowers(userId: Int, page: Pageable): List<Int> {
        return userRepository.findByIdOrNull(userId)?.followedBy
                ?.drop(page.offset.toInt())
                ?.take(page.pageSize)
                ?:listOf()
    }

    @Transactional
    override fun getFollowing(userId: Int, page: Pageable): List<Int> {
        return userRepository.findByIdOrNull(userId)?.following
                ?.drop(page.offset.toInt())
                ?.take(page.pageSize)
                ?:listOf()
    }

    @Transactional
    override fun follow(operatorId: Int, followeeId: Int) {
        val operator = userRepository.findByIdOrNull(operatorId) ?: return
        val followee = userRepository.findByIdOrNull(followeeId) ?: return
        operator.follow(followee)
        userRepository.saveAll(setOf(operator, followee))
    }

    @Transactional
    override fun unfollow(operatorId: Int, followeeId: Int) {
        val operator = userRepository.findByIdOrNull(operatorId) ?: return
        val followee = userRepository.findByIdOrNull(followeeId) ?: return
        operator.following.remove(followeeId)
        followee.followedBy.remove(operatorId)
        userRepository.saveAll(setOf(operator, followee))
    }
}