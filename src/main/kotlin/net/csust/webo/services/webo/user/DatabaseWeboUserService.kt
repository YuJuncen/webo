package net.csust.webo.services.webo.user

import net.csust.webo.domain.repositories.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DatabaseWeboUserService(
        val userRepository: UserRepository
) : WeboUserService {

    @Transactional
    override fun getFollowers(userId: Int): Iterable<Int> {
        return userRepository.findByIdOrNull(userId)?.followedBy?.toList() ?: setOf()
    }

    @Transactional
    override fun getFollowing(userId: Int): Iterable<Int> {
        return userRepository.findByIdOrNull(userId)?.following?.toList() ?: setOf()
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