package xyz.coderes.note.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.coderes.note.database.security.AuthService

@RestController
@RequestMapping("/auth")
class AuthController(
    val authService: AuthService,
) {
    data class LoginRequest(
        @field:Email(message = "Invalidate email format.")
        val email: String,
        @field:Pattern(
            regexp =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{9,}\$",
            message = "Password must be at least 9 characters long and contain at least one digit, uppercase and lowercase character."
        )
        val password: String
    )

    data class RegisterRequest(
        @field:Email(message = "Invalidate email format.")
        val email: String,
        @field:Pattern(
            regexp =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{9,}\$",
            message = "Password must be at least 9 characters long and contain at least one digit, uppercase and lowercase character."
        )
        val password: String
    )

    data class RefreshRequest(val refreshToken: String)

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ) {
        authService.registerUser(email = body.email, password = body.password)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody body: LoginRequest
    ) = authService.login(email = body.email, password = body.password)

    @PostMapping("/refreshToken")
    fun refreshToken(
        @RequestBody body: RefreshRequest
    ) = authService.refreshToken(refreshToken = body.refreshToken)


}