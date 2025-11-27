package com.example.gamezone.ui.screens.registration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gamezone.AppViewModelProvider
import com.example.gamezone.ui.screens.GENRE_OPTIONS
import com.example.gamezone.ui.screens.components.GenreSelection

@Composable
fun RegistrationScreen(
    navController: NavController,
    viewModel: RegistrationViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            val email = state.lastRegisteredEmail
            if (!email.isNullOrBlank()) {
                val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
                savedStateHandle?.set("registeredEmail", email)
                savedStateHandle?.set("registrationSuccess", true)
                if (savedStateHandle == null) {
                    val loginBackStackEntry = runCatching { navController.getBackStackEntry("login") }.getOrNull()
                    loginBackStackEntry?.savedStateHandle?.apply {
                        set("registeredEmail", email)
                        set("registrationSuccess", true)
                    }
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    navController.popBackStack()
                }
            } else {
                navController.popBackStack()
            }
            viewModel.consumeSuccess()
            viewModel.clearLastRegisteredEmail()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Registro de usuario", fontSize = 24.sp)

        CardSection(title = "Información básica") {
            RegistrationTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Nombre Completo",
                leadingIcon = Icons.Outlined.VerifiedUser,
                error = state.errors.fullName
            )
            RegistrationTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "Correo",
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                error = state.errors.email,
                helperText = state.emailAvailabilityMessage
            )
            RegistrationTextField(
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Teléfono (opcional)",
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                error = state.errors.phone
            )
        }

        CardSection(title = "Seguridad") {
            RegistrationTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                error = state.errors.password
            )
            RegistrationTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirmar contraseña",
                leadingIcon = Icons.Outlined.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                error = state.errors.confirmPassword
            )
        }

        CardSection(title = "Preferencias") {
            Text(text = "Géneros favoritos", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            GenreSelection(
                options = GENRE_OPTIONS,
                selectedGenres = state.selectedGenres,
                onSelectionChange = viewModel::onGenreToggle
            )
            AnimatedFieldMessages(error = state.errors.genres, helper = null)
        }

        AnimatedVisibility(visible = state.message != null, enter = fadeIn(), exit = fadeOut()) {
            state.message?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Button(
            onClick = viewModel::submitRegistration,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else {
                Text(text = "Registrar")
            }
        }

        TextButton(onClick = { navController.navigateUp() }) {
            Text(text = "¿Ya tienes cuenta? Inicia sesión")
        }
    }
}

@Composable
private fun CardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun RegistrationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    error: String?,
    helperText: String? = null
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
    AnimatedFieldMessages(error = error, helper = helperText)
}

@Composable
private fun AnimatedFieldMessages(error: String?, helper: String?) {
    val message = error ?: helper
    AnimatedVisibility(visible = message != null, enter = fadeIn(), exit = fadeOut()) {
        if (message != null) {
            val color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                text = message,
                color = color,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}

