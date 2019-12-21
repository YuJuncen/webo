package net.csust.webo.services.webo.exceptions

object NoSuchWeboException : NullPointerException() {
    override val message: String?
        get() = "您要找的 Webo 不存在！"
}