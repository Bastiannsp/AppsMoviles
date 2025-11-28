package gamezone.backend.user

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

@Entity
@Table(name = "usuarios")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var fullName: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var passwordHash: String = "",

    @Column
    var phone: String? = null,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "generos_favoritos",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "genre", nullable = false)
    var favoriteGenres: MutableSet<String> = mutableSetOf(),

    @Column
    var avatarUrl: String? = null
)
