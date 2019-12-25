package net.csust.webo.web.request

import org.springframework.validation.annotation.Validated
import javax.validation.constraints.Max

@Validated
data class UserRequest(
        @Max(16, message = "用户名不能超过16位。")
        val username: String,

        @Max(32, message = "密码不能过长。")
        val password: String
)