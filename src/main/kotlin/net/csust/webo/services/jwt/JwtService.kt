package net.csust.webo.services.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import net.csust.webo.config.JwtConfig
import net.csust.webo.domain.User
import net.csust.webo.services.kv.ConcurrentHashMapKvService
import net.csust.webo.services.user.UserExceptions
import net.csust.webo.services.user.UserService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet



@Service
class JwtService(private val config: JwtConfig,
                 private val kvs: ConcurrentHashMapKvService,
                 private val user: UserService) {
    init {
        kvs.put(Fields.AVAILABLE_REFRESH, ConcurrentSkipListSet<String>())
    }

    val availableTokens: MutableSet<String>
        get() = kvs.get(Fields.AVAILABLE_REFRESH)

    fun sign(u: User, now: Instant = Instant.now()) : TokenPair = signForUserId(u.id!!, now)


    fun login(username: String, password: String) : TokenPair {
        val user = user.getUserInfo(username) ?: throw UserExceptions.UserNameNotValid()
        if (!user.testPassword(password)) throw UserExceptions.PasswordNotValid()
        return sign(user)
    }

    private fun signForUserId(id: Int, now: Instant = Instant.now()) : TokenPair {
        fun (Instant).toDate() = Date.from(this)
        val token = JWT.create()
                .withClaim(Fields.USER_ID, id)
                .withClaim(Fields.CREATE_TIME, now.toDate())
                .withExpiresAt(now.plus(config.tokenValidTime()).toDate())
                .sign(config.algorithm)
        val refreshTokenId = UUID.randomUUID()
        val refreshToken = JWT.create()
                .withClaim(Fields.USER_ID, id)
                .withClaim(Fields.REFRESH_TOKEN_ID, refreshTokenId.toString())
                .withClaim(Fields.CREATE_TIME, now.toDate())
                .withExpiresAt(now.plus(config.refreshTokenValidTime()).toDate())
                .sign(config.algorithm)
        availableTokens.add(refreshTokenId.toString())
        return TokenPair(token, refreshToken)
    }

    fun verify(token: String): User {
        val (_, user) = ensureTokenIsValid(token)
        return user
    }

    fun refresh(refreshToken: String) : TokenPair {
        val (decoded, user) = ensureTokenIsValid(refreshToken)
        val tokenId = decoded.claims[Fields.REFRESH_TOKEN_ID]?.asString() ?: throw JWTVerificationException("刷新令牌无效，因为无法找到刷新令牌 ID。")
        if (!availableTokens.remove(tokenId)) throw JWTVerificationException("刷新令牌无效，因为它看上去已经被使用过。")
        return sign(user)
    }

    private fun ensureTokenIsValid(token: String): Pair<DecodedJWT, User> {
        val decoded = JWT.decode(token)
        config.algorithm.verify(decoded)
        val userId = decoded.getClaim(Fields.USER_ID).asInt()
        val user = user.getUserInfo(userId) ?: throw RuntimeException("令牌中的用户不存在")

        val mustAfter = user.lastPasswordChanged
        val yourTime = decoded.claims[Fields.CREATE_TIME]?.asDate()?.toInstant()
        val valid = yourTime?.isAfter(mustAfter) ?: false
        if (!valid) {
            throw SecurityException("令牌无效，因为验证者要求在 $mustAfter 之后发出的令牌（可能用户在那个时间点改了密码），而您的令牌的创建时间是 $yourTime。")
        }
        return decoded to user
    }
}