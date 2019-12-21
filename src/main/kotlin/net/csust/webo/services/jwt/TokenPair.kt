package net.csust.webo.services.jwt

data class TokenPair(
        val token: String,
        val refreshToken: String
)