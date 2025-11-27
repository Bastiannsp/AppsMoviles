package gamezone.backend.user

class UserNotFoundException(email: String) : RuntimeException("Usuario con email '$email' no encontrado")
