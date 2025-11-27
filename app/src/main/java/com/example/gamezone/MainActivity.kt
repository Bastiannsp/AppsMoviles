package com.example.gamezone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar // CAMBIO: Usamos TopAppBar en lugar de SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamezone.ui.screens.home.HomeScreen
import com.example.gamezone.ui.screens.login.LoginScreen
import com.example.gamezone.ui.screens.profile.ProfileScreen
import com.example.gamezone.ui.screens.registration.RegistrationScreen
import com.example.gamezone.ui.session.SessionViewModel
import com.example.gamezone.ui.theme.GamezoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GamezoneTheme {
                val sessionViewModel: SessionViewModel = viewModel(factory = AppViewModelProvider.Factory)
                val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
                val navController = rememberNavController()
                val startDestinationState = remember { mutableStateOf<String?>(null) }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(sessionState.isLoading, sessionState.activeUser) {
                    if (!sessionState.isLoading && startDestinationState.value == null) {
                        startDestinationState.value = if (sessionState.activeUser != null) {
                            "home"
                        } else {
                            "login"
                        }
                    }
                }

                val showAppChrome = startDestinationState.value != null
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (showAppChrome) {
                            GamezoneTopBar(
                                currentRoute = currentRoute,
                                canNavigateBack = navController.previousBackStackEntry != null && currentRoute !in setOf("login", startDestinationState.value),
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    },
                    bottomBar = {
                        if (showAppChrome && shouldShowBottomBar(currentRoute)) {
                            GamezoneBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    if (route != null && route != currentRoute) {
                                        navController.navigate(route) {
                                            popUpTo("home") { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    val startDestination = startDestinationState.value
                    if (startDestination == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            composable("login") {
                                LoginScreen(navController)
                            }
                            composable("register") {
                                RegistrationScreen(navController)
                            }
                            composable("home") {
                                HomeScreen(navController, sessionViewModel)
                            }
                            composable("profile") {
                                ProfileScreen(navController, sessionViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GamezoneTopBar(
    currentRoute: String?,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit
) {
    val title = when (currentRoute) {
        "login" -> "Inicio de sesión"
        "register" -> "Registro"
        "profile" -> "Mi perfil"
        "home" -> "Inicio"
        else -> "Gamezone"
    }
    // CAMBIO: SmallTopAppBar -> TopAppBar
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (canNavigateBack) {
                androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                    androidx.compose.material3.Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        // CAMBIO: smallTopAppBarColors -> topAppBarColors
        colors = TopAppBarDefaults.topAppBarColors()
    )
}

@Composable
private fun GamezoneBottomBar(
    currentRoute: String?,
    onNavigate: (String?) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { androidx.compose.material3.Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { androidx.compose.material3.Icon(Icons.Outlined.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") }
        )
    }
}

private fun shouldShowBottomBar(currentRoute: String?): Boolean = currentRoute in setOf("home", "profile")