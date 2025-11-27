package gamezone.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "El nombre completo es obligatorio")
    val fullName: String,

    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo debe tener un formato válido")
    val email: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
    val password: String,

    @field:Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    val phone: String? = null,

    @field:NotEmpty(message = "Debes seleccionar al menos un género favorito")
    val favoriteGenres: List<@NotBlank(message = "El género no puede estar vacío") String>
)
