package net.csust.webo.services.webo.exceptions

object NoSuchCommentException : NullPointerException() {
    override val message: String?
        get() = "您所寻找的评论不存在！"
}