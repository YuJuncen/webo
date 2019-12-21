package net.csust.webo.domain

import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(indexes = [Index(columnList = "commentTo", unique = false)])
class Comment(
        @Id
        @GeneratedValue
        var id: UUID? = null,

        @NotNull
        val publisher : Int,

        @NotNull
        @Column(name = "commentTo")
        val commentTo : UUID,

        @NotNull
        val content : String,

        @NotNull
        @Column(name = "publishTime")
        val publishTime : Instant = Instant.now(),

        val replyTo : UUID? = null
) {
    fun reply(userId: Int, message: String) = Comment(commentTo= this.commentTo, content= message, publisher= userId, replyTo= this.id)
}