package gamezone.backend.user

class UserAlreadyExistsException(email: String) : RuntimeException("El correo '$email' ya está registrado")
