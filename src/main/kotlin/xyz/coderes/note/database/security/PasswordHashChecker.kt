package xyz.coderes.note.database.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordHashChecker {
    private val passwordEncoder = BCryptPasswordEncoder()
    fun encode(password: String): String =
        passwordEncoder.encode(password)

    fun matches(password: String, hashedPassword: String): Boolean =
          passwordEncoder.matches(password, hashedPassword)
}