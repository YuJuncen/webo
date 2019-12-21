package net.csust.webo.services.user

import net.csust.webo.domain.User

fun (User).toNameView() = UserNameView(
        username = this.username,
        nickname = this.nickname,
        email = this.email
)

data class UserNameView(
        val username: String,
        val nickname: String,
        val email: String?
)