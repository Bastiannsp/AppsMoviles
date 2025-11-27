package com.example.gamezone.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gamezone.AppViewModelProvider
import com.example.gamezone.model.User
import com.example.gamezone.ui.session.SessionViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val homeState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeUser = sessionState.activeUser

    LaunchedEffect(sessionState.isLoading, activeUser) {
        if (!sessionState.isLoading && activeUser == null) {
            navController.navigate("login") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    LaunchedEffect(activeUser) {
        viewModel.setUser(activeUser)
    }

    if (activeUser == null) {
        return
    }

    val context = LocalContext.current
    val permissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    val hasPermission = remember { mutableStateOf(checkLocationPermission(context, permissions)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        hasPermission.value = granted
        if (granted) {
            viewModel.requestLocation()
        }
    }

    LaunchedEffect(hasPermission.value) {
        if (hasPermission.value) {
            viewModel.requestLocation()
        }
    }

    val feedItems = remember(activeUser.favoriteGenres) {
        buildFeedForUser(activeUser)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .animateContentSize()
    ) {
        GreetingHeader(activeUser)

        Spacer(modifier = Modifier.height(16.dp))

        LocationCard(
            hasPermission = hasPermission.value,
            isLoading = homeState.isFetchingLocation,
            locationError = homeState.locationError,
            locationText = homeState.deviceLocation?.let { location ->
                "Lat: %.4f, Long: %.4f (±%.0fm)".format(location.latitude, location.longitude, location.accuracy)
            },
            onRequestPermission = {
                permissionLauncher.launch(permissions)
            },
            onRefreshLocation = viewModel::requestLocation
        )

        Spacer(modifier = Modifier.height(16.dp))

        NavigationShortcuts(
            onProfile = { navController.navigate("profile") },
            onLogout = sessionViewModel::logout
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Novedades", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(feedItems, key = { it.title }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingHeader(user: User) {
    Column {
        Text(text = "Hola, ${user.fullName}", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        val genresText = if (user.favoriteGenres.isEmpty()) {
            "Sin géneros favoritos seleccionados"
        } else {
            user.favoriteGenres.joinToString(separator = ", ")
        }
        Crossfade(targetState = genresText, label = "genresCrossfade") { text ->
            Text(text = "Géneros favoritos: $text", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LocationCard(
    hasPermission: Boolean,
    isLoading: Boolean,
    locationError: String?,
    locationText: String?,
    onRequestPermission: () -> Unit,
    onRefreshLocation: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Ubicación", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            when {
                hasPermission && locationText != null -> {
                    Text(text = locationText, style = MaterialTheme.typography.bodySmall)
                }

                hasPermission && isLoading -> {
                    Text(text = "Obteniendo ubicación...", style = MaterialTheme.typography.bodySmall)
                }

                hasPermission -> {
                    Text(text = "Ubicación no disponible", style = MaterialTheme.typography.bodySmall)
                }

                else -> {
                    Text(text = "Permite el acceso a la ubicación para personalizar tus recomendaciones.", style = MaterialTheme.typography.bodySmall)
                }
            }

            AnimatedVisibility(visible = locationError != null) {
                locationError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val actionLabel = if (hasPermission) "Actualizar" else "Permitir"
            val icon = if (hasPermission) Icons.Outlined.Refresh else Icons.Outlined.LocationOn
            OutlinedButton(onClick = if (hasPermission) onRefreshLocation else onRequestPermission) {
                Icon(imageVector = icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
private fun NavigationShortcuts(onProfile: () -> Unit, onLogout: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Accesos rápidos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            ShortcutButton(icon = Icons.Outlined.Person, label = "Mi perfil", onClick = onProfile)
            Spacer(modifier = Modifier.height(8.dp))
            ShortcutButton(icon = Icons.Outlined.Logout, label = "Cerrar sesión", onClick = onLogout)
        }
    }
}

@Composable
private fun ShortcutButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

private fun buildFeedForUser(user: User): List<FeedItem> {
    val highlightGenre = user.favoriteGenres.firstOrNull()
    val personalized = highlightGenre?.let {
        FeedItem(
            title = "Destacados en $it",
            description = "Explora nuevos contenidos inspirados en tu gusto por $it."
        )
    }
    return listOfNotNull(
        personalized,
        FeedItem("Explora", "Descubre comunidades de jugadores que comparten tus mismos intereses."),
        FeedItem("Eventos", "Participa en torneos y actividades en vivo durante el fin de semana."),
        FeedItem("Noticias", "Mantente al día con las últimas novedades del mundo gamer."),
    )
}

private data class FeedItem(val title: String, val description: String)

private fun checkLocationPermission(context: android.content.Context, permissions: Array<String>): Boolean =
    permissions.any { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
