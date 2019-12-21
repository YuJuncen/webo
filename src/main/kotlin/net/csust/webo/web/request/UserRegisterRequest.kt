package net.csust.webo.web.request

import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class UserRegisterRequest(
        @get:NotNull
        @get:Size(min = 1, max = 16)
        val username: String,

        @get:Size(min = 1, max = 32)
        val nickname: String = username,

        @get:Email
        @get:Size(min = 1, max = 128)
        val email: String? = null,

        @get:Size(min = 1, max = 32)
        @get:NotNull
        val password: String
)