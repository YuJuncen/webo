package net.csust.webo.services.webo.user

import net.csust.webo.services.webo.views.WeboUserView
import org.springframework.data.domain.Pageable

interface WeboUserService {
    fun getFollowing(userId: Int, page: Pageable) : List<Int>
    fun getFollowers(userId: Int, page: Pageable) : List<Int>
    fun follow(operatorId: Int, followeeId: Int)
    fun unfollow(operatorId: Int, followeeId: Int)
    fun isFollowing(userId: Int, toUserId: Int) : Boolean
    fun getUserOf(userId: Int) : WeboUserView?
}