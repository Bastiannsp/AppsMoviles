package cl.duoc.appmovil2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.appmovil2.data.local.database.AppDatabase
import cl.duoc.appmovil2.data.repository.UserRepository
import cl.duoc.appmovil2.data.session.SessionPreferencesRepository
import cl.duoc.appmovil2.data.session.sessionDataStore
import cl.duoc.appmovil2.navegation.AppScreens
import cl.duoc.appmovil2.navegation.NavegationEvent
import cl.duoc.appmovil2.ui.screens.HomeScreen
import cl.duoc.appmovil2.ui.screens.LoginScreen
import cl.duoc.appmovil2.ui.screens.RegisterScreen
import cl.duoc.appmovil2.ui.theme.AppMovil2Theme
import cl.duoc.appmovil2.viewmodel.LoginViewModel
import cl.duoc.appmovil2.viewmodel.LoginViewModelFactory
import cl.duoc.appmovil2.viewmodel.MainViewModel
import cl.duoc.appmovil2.viewmodel.RegisterViewModel
import cl.duoc.appmovil2.viewmodel.RegisterViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private val sessionPreferencesRepository by lazy {
        SessionPreferencesRepository(applicationContext.sessionDataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppMovil2Theme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel()
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val rememberSession = sessionPreferencesRepository.rememberSessionFlow.first()
                    startDestination = if (rememberSession) {
                        AppScreens.Home.route
                    } else {
                        AppScreens.Login.route
                    }
                }

                LaunchedEffect(mainViewModel, navController) {
                    mainViewModel.navegationEvents.collectLatest { event ->
                        when (event) {
                            is NavegationEvent.NavegateTo -> {
                                navController.navigate(event.route.route) {
                                    event.popUpToRoute?.let { target ->
                                        popUpTo(target.route) {
                                            inclusive = event.inclusive
                                        }
                                    }
                                    launchSingleTop = event.singleTop
                                }
                            }
                            NavegationEvent.PopBackStack -> {
                                navController.popBackStack()
                            }
                            NavegationEvent.NavegateUp -> {
                                navController.navigateUp()
                            }
                        }
                    }
                }

                if (startDestination == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination!!
                    ) {
                        composable(AppScreens.Login.route) {
                            val loginViewModel: LoginViewModel = viewModel(
                                factory = remember { LoginViewModelFactory(sessionPreferencesRepository) }
                            )
                            LoginScreen(
                                viewModel = loginViewModel,
                                onNavigateToHome = { rememberSession ->
                                    navController.navigate(AppScreens.Home.route) {
                                        if (rememberSession) {
                                            popUpTo(AppScreens.Login.route) {
                                                inclusive = true
                                            }
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate(AppScreens.Register.route)
                                }
                            )
                        }

                        composable(AppScreens.Register.route) {
                            val context = LocalContext.current
                            val userRepository = remember(context) {
                                val database = AppDatabase.getInstance(context)
                                UserRepository(database.userDao())
                            }
                            val registerViewModel: RegisterViewModel = viewModel(
                                factory = remember(userRepository) {
                                    RegisterViewModelFactory(userRepository)
                                }
                            )
                            RegisterScreen(
                                viewModel = registerViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onRegistrationCompleted = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(AppScreens.Home.route) {
                            HomeScreen(navController = navController)
                        }

                        composable(AppScreens.Profile.route) {
                            PlaceholderScreen(title = "Perfil") {
                                navController.popBackStack()
                            }
                        }

                        composable(AppScreens.Settings.route) {
                            PlaceholderScreen(title = "Ajustes") {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateBack) {
            Text("Volver")
        }
    }
}

