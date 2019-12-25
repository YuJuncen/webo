package net.csust.webo.services.user

object UserExceptions {
    class UserNameNotValid : RuntimeException() {
        override val message = "无效用户名～"
    }

    class PasswordNotValid : RuntimeException() {
        override val message = "无效密码！"
    }
}