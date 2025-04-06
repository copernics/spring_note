package xyz.coderes.note.database.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}") private val jwtSecret: String
) {
    private val secretKey =
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))

    private val accessTokenValidityMs = 15 * 60 * 1000L
    val refreshTokenValidityMs = 30 * 24 * 60 * 1000L

    private fun generateToken(
        userId: String,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun toGenerateAccessToken(userId: String): String =
        generateToken(userId, "access", accessTokenValidityMs)

    fun toGenerateRefreshToken(userId: String): String =
        generateToken(userId, "refresh", refreshTokenValidityMs)

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAccessToken(token) ?: return false
        val type = claims.get("type", String::class.java) ?: return false
        return type == "access"
    }

    fun getUserIdFromAccessToken(token: String): String? {
        val claims = parseAccessToken(token) ?: throw IllegalArgumentException("Invalid token")
        return claims.subject
    }


    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAccessToken(token) ?: return false
        val type = claims.get("type", String::class.java) ?: return false
        return type == "refresh"
    }

    fun parseAccessToken(token: String): Claims? {
        return try {
            val rawToken = getRawToken(token)

            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }

    private fun getRawToken(token: String): String {
        val rawToken = if (token.startsWith(prefix)) {
            token.removePrefix(prefix)
        } else {
            token
        }
        return rawToken
    }
    companion object {
        const val prefix = "Bearer "
    }
}

// wek3269586jkhwjofhsjkcfo32874
// d2VrMzI2OTU4NmpraHdqb2Zoc2prY2ZvMzI4NzQ=