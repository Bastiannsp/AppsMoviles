package gamezone.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
    @field:NotBlank(message = "El nombre completo es obligatorio")
    val fullName: String,

    @field:Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    val phone: String? = null,

    @field:NotEmpty(message = "Debes mantener al menos un género favorito")
    val favoriteGenres: List<@NotBlank(message = "El género no puede estar vacío") String>,

    @field:Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
    val password: String? = null,

    @field:Size(max = 512, message = "El enlace del avatar no debe exceder 512 caracteres")
    val avatarUrl: String? = null
)
