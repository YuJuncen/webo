package net.csust.webo.services.user

import net.csust.webo.domain.User

interface UserInfoModifier {
    fun modify(user: User)
}