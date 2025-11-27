package com.example.gamezone

import android.app.Application
import androidx.room.Room
import com.example.gamezone.data.local.GamezoneDatabase
import com.example.gamezone.data.location.LocationTracker
import com.example.gamezone.data.preferences.UserPreferences
import com.example.gamezone.data.storage.ProfilePhotoStorage
import com.example.gamezone.network.RetrofitClient
import com.example.gamezone.repository.UserRepository
import com.example.gamezone.repository.UserRepositoryImpl

class GamezoneApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val database: GamezoneDatabase = Room.databaseBuilder(
        application,
        GamezoneDatabase::class.java,
        "gamezone.db"
    ).fallbackToDestructiveMigration().build()

    private val userPreferences = UserPreferences(application)
    private val apiService = RetrofitClient.instance
    val locationTracker = LocationTracker(application)
    val profilePhotoStorage = ProfilePhotoStorage(application)

    val userRepository: UserRepository =
        UserRepositoryImpl(database.userDao(), userPreferences, apiService)
}
