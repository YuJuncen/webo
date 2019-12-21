package net.csust.webo.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.sun.istack.NotNull
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table( indexes = [
        Index(name= "publish_time_idx", columnList = "publishTime", unique = false),
        Index(name= "publisher_idx", columnList = "publishedBy", unique = false)
])
data class Webo(
        @Id
        @GeneratedValue
        var id: UUID? = null,

        @NotNull
        @Column(name = "publishTime")
        var publishTime: Instant = Instant.now(),

        @NotNull
        @Column(name = "publishedBy")
        var publishedBy: Int,

        @NotNull
        var content: String,

        @NotNull
        @ElementCollection
        @JsonIgnore
        var likedBy: MutableSet<Int> = mutableSetOf(),

        var forwarding: UUID? = null,

        @JsonIgnore
        @ElementCollection
        var forwardedBy: MutableSet<UUID> = mutableSetOf()
) {
        fun commentTo(by: Int, content: String) = Comment(publisher = by, content= content, commentTo = this.id!!)
        fun forward(by: Int, addionalMessage : String = "💬转发一条 Webo～"): Webo {
                val newID = UUID.randomUUID()
                this.forwardedBy.add(newID)
                return Webo(
                        id = newID,
                        publishedBy = by,
                        content = addionalMessage,
                        forwarding = this.id!!
                )
        }
}