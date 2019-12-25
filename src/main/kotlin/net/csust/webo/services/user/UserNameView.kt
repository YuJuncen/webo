package net.csust.webo.services.user

import net.csust.webo.domain.User

fun (User).toNameView() = UserNameView(
        id = this.id!!,
        username = this.username,
        nickname = this.nickname,
        email = this.email,
        bio = this.bio
)

data class UserNameView(
        val id: Int,
        val username: String,
        val nickname: String,
        val email: String?,
        val bio: String?
)