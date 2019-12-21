package net.csust.webo.config

import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class JwtConfig(
        @Value("\${net.csust.webo.jwtSecret}") val secret : String
) {
    val algorithm: Algorithm
        get() = Algorithm.HMAC256(secret)

    fun tokenValidTime(): Duration {
        return Duration.ofHours(2)
    }

    fun refreshTokenValidTime(): Duration {
        return Duration.ofDays(7)
    }
}
