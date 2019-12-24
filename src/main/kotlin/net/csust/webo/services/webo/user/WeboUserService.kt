package net.csust.webo.services.webo.user

interface WeboUserService {
    fun getFollowing(userId: Int) : Iterable<Int>
    fun getFollowers(userId: Int) : Iterable<Int>
    fun follow(operatorId: Int, followeeId: Int)
    fun unfollow(operatorId: Int, followeeId: Int)
    fun isFollowing(userId: Int, toUserId: Int) = getFollowing(userId).any { it == toUserId }
    fun countFollowing(userId: Int) : Int = getFollowing(userId).count()
    fun countFollowers(userId: Int) : Int = getFollowers(userId).count()
}