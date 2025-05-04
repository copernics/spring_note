package xyz.coderes.note.database.security

import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import xyz.coderes.note.database.model.RefreshToken
import xyz.coderes.note.database.model.User
import xyz.coderes.note.database.repository.RefreshTokenRepository
import xyz.coderes.note.database.repository.UserRepository
import java.security.MessageDigest
import java.time.Instant
import java.util.*

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val hashEncoder: PasswordHashChecker,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    fun registerUser(email: String, password: String): User {
        val user = userRepository.findByEmail(email.trim())
        if(user!=null) {
            throw ResponseStatusException(HttpStatus.CONFLICT,"An user with that email already exists.")
        }
        return userRepository.save(
            User(
                email = email,
                hashedPassword = hashEncoder.encode(password),

                )
        )
    }

    fun login(email: String, password: String): TokenPair {
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid credentials.")

        if (!hashEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Invalid credentials.")
        }
        val newAccessToken = jwtService.toGenerateAccessToken(user.id.toHexString())
        val newRefreshToken = jwtService.toGenerateRefreshToken(user.id.toHexString())

        storeRefreshToken(user.id, newRefreshToken)

        val tokenPair = generateNewTokensAndStoreRefresh(user.id.toHexString())


        return tokenPair
    }

    private fun generateNewTokensAndStoreRefresh(userId: String): TokenPair {
        val pair = TokenPair(
            accessToken = jwtService.toGenerateAccessToken(userId),
            refreshToken = jwtService.toGenerateRefreshToken(userId)
        )

        storeRefreshToken(ObjectId(userId), pair.refreshToken)

        return pair
    }

    @Transactional
    fun refreshToken(refreshToken: String): TokenPair {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")
        }
        val userId = jwtService.getUserIdFromAccessToken(refreshToken)
        val user = userRepository.findById(ObjectId(userId))
            .orElseThrow {
                ResponseStatusException(HttpStatusCode.valueOf(401), "Invalid refresh token.")
            }

        val hashToken = hashToken(refreshToken)

        refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashToken)
            ?: throw ResponseStatusException(
                HttpStatusCode.valueOf(401),
                "Invalid refresh token (maybe it has been expired)."
            )

        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, hashToken)

        return generateNewTokensAndStoreRefresh(user.id.toHexString())
    }

    private fun storeRefreshToken(userId: ObjectId, rawRefreshToken: String) {
        val refreshToken = hashToken(rawRefreshToken)
        val expiry = jwtService.refreshTokenValidityMs
        val expiryDate = Instant.now().plusMillis(expiry)
        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                hashedToken = refreshToken,
                expiryDate = expiryDate,
            )
        )
    }

    private fun hashToken(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(rawToken.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )
}
