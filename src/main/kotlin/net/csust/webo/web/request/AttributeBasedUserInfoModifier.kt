package net.csust.webo.web.request

import net.csust.webo.domain.User
import net.csust.webo.services.user.UserInfoModifier
import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class AttributeBasedUserInfoModifier(
        @get:Email
        val email: String?,

        @get:Size(max= 32)
        val nickname: String?,

        @get:Size(max= 32)
        val bio: String?
) : UserInfoModifier {
    override fun modify(user: User) {
        email ?. let { user.email = it }
        nickname ?. let { user.nickname = it }
        bio ?. let { user.bio = it }
    }
}