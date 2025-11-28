package com.example.gamezone

import android.app.Application
import androidx.room.Room
import com.example.gamezone.data.local.GamezoneDatabase
import com.example.gamezone.data.location.LocationTracker
import com.example.gamezone.data.preferences.UserPreferences
import com.example.gamezone.data.storage.ProfilePhotoStorage
import com.example.gamezone.network.RetrofitClient
import com.example.gamezone.network.external.GameDealsApi
import com.example.gamezone.repository.UserRepository
import com.example.gamezone.repository.UserRepositoryImpl
import com.example.gamezone.repository.GameDealsRepository
import com.example.gamezone.repository.GameDealsRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    private val gameDealsApi: GameDealsApi = Retrofit.Builder()
        .baseUrl("https://www.cheapshark.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GameDealsApi::class.java)
    val locationTracker = LocationTracker(application)
    val profilePhotoStorage = ProfilePhotoStorage(application)

    val userRepository: UserRepository =
        UserRepositoryImpl(database.userDao(), userPreferences, apiService)
    val gameDealsRepository: GameDealsRepository =
        GameDealsRepositoryImpl(gameDealsApi)
}
