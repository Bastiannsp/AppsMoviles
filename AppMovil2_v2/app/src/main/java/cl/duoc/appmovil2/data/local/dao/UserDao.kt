package cl.duoc.appmovil2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.appmovil2.data.local.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
}
