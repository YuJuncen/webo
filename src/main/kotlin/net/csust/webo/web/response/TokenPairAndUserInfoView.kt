package net.csust.webo.web.response

import com.fasterxml.jackson.annotation.JsonUnwrapped
import net.csust.webo.services.jwt.TokenPair
import net.csust.webo.services.user.UserNameView

data class TokenPairAndUserInfoView(
        @JsonUnwrapped
        val tokenPair: TokenPair,
        @JsonUnwrapped
        val userView: UserNameView
)