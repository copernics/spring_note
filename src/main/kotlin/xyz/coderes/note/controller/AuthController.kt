package xyz.coderes.note.controller

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
    data class LoginRequest(val email: String, val password: String)
    data class RegisterRequest(val email: String, val password: String)
    data class RefreshRequest(val refreshToken: String)

    @PostMapping("/register")
    fun register(
        @RequestBody body: RegisterRequest
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