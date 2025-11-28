package gamezone.backend.user

import gamezone.backend.dto.UpdateUserRequest
import gamezone.backend.dto.UserResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@Validated
class UserController(
    private val userService: UserService
) {
    @GetMapping("/{email}")
    fun getByEmail(@PathVariable email: String): ResponseEntity<UserResponse> {
        val response = userService.getByEmail(email)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{email}")
    fun update(
        @PathVariable email: String,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val response = userService.update(email, request)
        return ResponseEntity.ok(response)
    }
}
