package gamezone.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo debe tener un formato válido")
    val email: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
    val password: String
)
