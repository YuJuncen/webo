package net.csust.webo.web.exceptions

object NoToken : IllegalAccessException() {
    override val message: String?
        get() = "没有 Token!"
}