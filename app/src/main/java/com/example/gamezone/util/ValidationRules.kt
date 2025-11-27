package com.example.gamezone.util

fun validateFullName(fullName: String): String? {
    val trimmed = fullName.trim()
    if (trimmed.isEmpty()) return "El nombre es obligatorio"
    if (trimmed.length > 100) return "El nombre no puede superar 100 caracteres"
    if (!trimmed.matches(NAME_REGEX)) return "Solo se permiten letras y espacios"
    return null
}

fun validateRegistrationEmail(email: String): String? {
    val trimmed = email.trim()
    if (trimmed.isEmpty()) return "El correo es obligatorio"
    if (trimmed.length > 60) return "El correo no puede superar 60 caracteres"
    if (!trimmed.matches(EMAIL_DUOC_REGEX)) return "Debe ser un correo @duoc.cl válido"
    return null
}

fun validatePassword(password: String): String? {
    if (password.length < 10) return "La contraseña debe tener al menos 10 caracteres"
    if (!password.any { it.isUpperCase() }) return "Debe incluir al menos una mayúscula"
    if (!password.any { it.isLowerCase() }) return "Debe incluir al menos una minúscula"
    if (!password.any { it.isDigit() }) return "Debe incluir al menos un número"
    if (!password.any { !it.isLetterOrDigit() }) return "Debe incluir al menos un carácter especial"
    return null
}

fun validateConfirmPassword(password: String, confirmPassword: String): String? {
    if (confirmPassword.isBlank()) return "Debe confirmar la contraseña"
    if (password != confirmPassword) return "Las contraseñas no coinciden"
    return null
}

fun validatePhone(phone: String): String? {
    val trimmed = phone.trim()
    if (trimmed.isEmpty()) return null
    if (!trimmed.all { it.isDigit() }) return "Solo se permiten números"
    return null
}

fun validateGenres(selectedGenres: Set<String>): String? {
    if (selectedGenres.isEmpty()) return "Selecciona al menos un género"
    return null
}
