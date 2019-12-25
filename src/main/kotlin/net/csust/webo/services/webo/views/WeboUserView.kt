package net.csust.webo.services.webo.views

import net.csust.webo.services.user.UserNameView

data class WeboUserView(
        val personal: UserNameView,
        val followingCount: Int,
        val followedByCount: Int
)