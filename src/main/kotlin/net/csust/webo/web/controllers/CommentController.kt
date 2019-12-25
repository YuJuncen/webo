package net.csust.webo.web.controllers

import net.csust.webo.domain.User
import net.csust.webo.services.webo.CommentService
import net.csust.webo.web.annotations.InjectUserInfo
import net.csust.webo.web.request.CommentRequest
import net.csust.webo.web.response.WeboResponse.Companion.Status.response
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/comment")
class CommentController(
        val commentService: CommentService
) {
    @PostMapping("/new")
    @InjectUserInfo
    fun newComment(@RequestAttribute user: User,
                   @RequestBody @Valid comment: CommentRequest) =
        commentService.publishCommentTo(comment.commentTo, user.id!!, comment.text).response()


    @GetMapping("/all")
    fun allComments(id: String, before: Instant?) =
        commentService.getCommentsOf(UUID.fromString(id), before ?: Instant.now()).response()

    @DeleteMapping("")
    @InjectUserInfo
    fun deleteComment(@RequestAttribute userId: Int,
                      id: String) = commentService.removeComment(userId, UUID.fromString(id)).response()
}