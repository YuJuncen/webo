package net.csust.webo.services.user

import net.csust.webo.domain.User

data class UserNameView(
        val userId: Int,
        val username: String,
        val nickname: String
) {
    companion object {
        fun (User).toNameView() = UserNameView(
                userId = this.id!!,
                username = this.username,
                nickname = this.nickname
        )
    }
}
