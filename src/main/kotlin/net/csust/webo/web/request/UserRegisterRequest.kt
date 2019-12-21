package net.csust.webo.web.request

import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class UserRegisterRequest(
        @get:Email
        @get:NotNull
        @get:Size(min = 1, max = 128)
        val username: String,

        @get:Size(min = 1, max = 32)
        val nickname: String = username,

        @get:Size(min = 1, max = 32)
        @get:NotNull
        val password: String
)