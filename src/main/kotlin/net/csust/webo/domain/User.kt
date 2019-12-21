package net.csust.webo.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.sun.istack.NotNull
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import org.springframework.util.DigestUtils.md5DigestAsHex
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.Email

@Entity
@Table(uniqueConstraints = [UniqueConstraint(name="username-uniq", columnNames = ["username"])],
        indexes = [Index(name="username-index", columnList = "username", unique = true)])
data class User(
        @Id
        @GeneratedValue
        var id: Int?,

        @NotNull
        @Column(unique = true, name = "username")
        var username : String,

        @NotNull
        private var password : String,

        @NotNull
        var nickname : String = username,

        @NotNull
        @ElementCollection
        @LazyCollection(LazyCollectionOption.TRUE)
        @JsonIgnore
        var following : MutableSet<Int> = mutableSetOf(),

        @NotNull
        @ElementCollection
        @LazyCollection(LazyCollectionOption.TRUE)
        @JsonIgnore
        var followedBy : MutableSet<Int> = mutableSetOf(),

        var email : String? = null
) {
    companion object {
        fun register(username: String, password: String) : User {
            val hashedPassword = md5DigestAsHex(password.toByteArray())
            return User(null, username, hashedPassword)
        }
    }

    @JsonIgnore private var _lastPasswordChanged : Instant? = null
    val lastPasswordChanged : Instant
        @JsonIgnore
        get() = _lastPasswordChanged ?: Instant.MIN

    fun testPassword(password: String) = md5DigestAsHex(password.toByteArray()) == this.password
    fun changePassword(oldPassword: String, newPassword: String) {
        if (!testPassword(oldPassword)) throw RuntimeException("密码错误～")
        this.password = md5DigestAsHex(newPassword.toByteArray())
        _lastPasswordChanged = Instant.now()
    }

    fun follow(user: User) {
        this.following.add(user.id!!)
        user.followedBy.add(id!!)
    }

    fun writeWebo(message: String) : Webo {
        return Webo(content= message, publishedBy = id!!)
    }

    fun likeWebo(toLike: Webo) {
        if (this.id!! in toLike.likedBy) {
            throw IllegalStateException("你已经喜欢这个了～没有必要再喜欢一次了～😍")
        }

        toLike.likedBy.add(this.id!!)
    }

    fun unlikeWebo(toUnlike: Webo) {
        if (!(this.id!! in toUnlike.likedBy)) {
            throw IllegalStateException("你从来都没有爱过，何来放弃？🤔")
        }

        toUnlike.likedBy.remove(this.id!!)
    }

    fun toggleLike(toToggle: Webo) : Boolean {
        if (this.id in toToggle.likedBy) {
            this.unlikeWebo(toToggle)
            return false
        }
        this.likeWebo(toToggle)
        return true
    }
}