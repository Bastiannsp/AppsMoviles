package com.example.gamezone

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.gamezone.ui.screens.home.HomeViewModel
import com.example.gamezone.ui.screens.login.LoginViewModel
import com.example.gamezone.ui.screens.profile.ProfileViewModel
import com.example.gamezone.ui.screens.registration.RegistrationViewModel
import com.example.gamezone.ui.session.SessionViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            RegistrationViewModel(gamezoneApplication().container.userRepository)
        }
        initializer {
            LoginViewModel(gamezoneApplication().container.userRepository)
        }
        initializer {
            SessionViewModel(gamezoneApplication().container.userRepository)
        }
        initializer {
            ProfileViewModel(
                gamezoneApplication().container.userRepository,
                gamezoneApplication().container.profilePhotoStorage
            )
        }
        initializer {
            HomeViewModel(
                gamezoneApplication().container.locationTracker,
                gamezoneApplication().container.gameDealsRepository
            )
        }
    }
}

fun CreationExtras.gamezoneApplication(): GamezoneApp =
    (this[APPLICATION_KEY] as? GamezoneApp)
        ?: throw IllegalStateException("GamezoneApp no disponible en CreationExtras")
