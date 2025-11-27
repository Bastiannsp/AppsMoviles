package com.example.gamezone.ui.screens.profile

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gamezone.AppViewModelProvider
import com.example.gamezone.ui.screens.GENRE_OPTIONS
import com.example.gamezone.ui.screens.components.GenreSelection
import com.example.gamezone.ui.session.SessionViewModel
import java.io.File

@Composable
fun ProfileScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val profileState by viewModel.uiState.collectAsStateWithLifecycle()
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

    if (!profileState.isLoaded) return

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) viewModel.saveProfilePhoto(bitmap)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Mi perfil", style = MaterialTheme.typography.headlineSmall)

        ProfilePhotoSection(
            avatarPath = profileState.avatarPath,
            isSavingPhoto = profileState.isSavingPhoto,
            onCapturePhoto = { cameraLauncher.launch(null) }
        )

        InfoCard(title = "Datos personales") {
            ReadonlyField(value = profileState.email, label = "Correo", leadingIcon = Icons.Outlined.Email)
            Spacer(modifier = Modifier.height(12.dp))
            ValidatedTextField(
                value = profileState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Nombre Completo",
                leadingIcon = Icons.Outlined.VerifiedUser,
                error = profileState.errors.fullName
            )
            Spacer(modifier = Modifier.height(12.dp))
            ValidatedTextField(
                value = profileState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Teléfono (opcional)",
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                error = profileState.errors.phone
            )
        }

        InfoCard(title = "Géneros favoritos") {
            GenreSelection(
                options = GENRE_OPTIONS,
                selectedGenres = profileState.selectedGenres,
                onSelectionChange = viewModel::onGenreToggle
            )
            AnimatedError(profileState.errors.genres)
        }

        InfoCard(title = "Seguridad") {
            ValidatedTextField(
                value = profileState.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                label = "Nueva contraseña",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                error = profileState.errors.newPassword
            )
            Spacer(modifier = Modifier.height(12.dp))
            ValidatedTextField(
                value = profileState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirmar contraseña",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                error = profileState.errors.confirmPassword
            )
        }

        AnimatedVisibility(profileState.message != null, enter = fadeIn(), exit = fadeOut()) {
            profileState.message?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (profileState.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Button(
            onClick = viewModel::submitUpdates,
            modifier = Modifier.fillMaxWidth(),
            enabled = !profileState.isSaving
        ) {
            if (profileState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Outlined.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Guardar cambios")
            }
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text(text = "Volver")
        }
    }
}

@Composable
private fun ProfilePhotoSection(avatarPath: String?, isSavingPhoto: Boolean, onCapturePhoto: () -> Unit) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (avatarPath.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(avatarPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                if (isSavingPhoto) {
                    CircularProgressIndicator()
                }
            }

            OutlinedButton(onClick = onCapturePhoto, enabled = !isSavingPhoto) {
                Icon(imageVector = Icons.Outlined.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isSavingPhoto) "Guardando..." else "Actualizar foto")
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ReadonlyField(value: String, label: String, leadingIcon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        leadingIcon = { Icon(imageVector = leadingIcon, contentDescription = null) },
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    error: String?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        leadingIcon = { Icon(imageVector = leadingIcon, contentDescription = null) },
        trailingIcon = {
            if (error != null) {
                Icon(imageVector = Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        },
        isError = error != null,
        modifier = Modifier.fillMaxWidth()
    )
    AnimatedError(error)
}

@Composable
private fun AnimatedError(error: String?) {
    AnimatedVisibility(visible = error != null, enter = fadeIn(), exit = fadeOut()) {
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
