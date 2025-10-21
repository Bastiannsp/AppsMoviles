package cl.duoc.appmovil2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.appmovil2.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onRegistrationCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.showSuccess) {
        if (uiState.showSuccess) {
            focusManager.clearFocus(force = true)
            delay(1600)
            viewModel.onSuccessConsumed()
            onRegistrationCompleted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            AnimatedVisibility(
                visible = uiState.showSuccess,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 })
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "¡Registro exitoso!",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Estamos guardando tus datos...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "Completa tus datos para comenzar",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text("Nombre completo") },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Person, contentDescription = null)
                },
                trailingIcon = {
                    FieldTrailingIcon(isValid = uiState.isNameValid, hasError = uiState.nameError != null)
                },
                isError = uiState.nameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            FieldErrorMessage(uiState.nameError)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Correo institucional") },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Email, contentDescription = null)
                },
                trailingIcon = {
                    when {
                        uiState.isEmailChecking -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        else -> {
                            FieldTrailingIcon(
                                isValid = uiState.isEmailValid && !uiState.isEmailChecking,
                                hasError = uiState.emailError != null
                            )
                        }
                    }
                },
                isError = uiState.emailError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                )
            )
            FieldErrorMessage(uiState.emailError)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
                },
                trailingIcon = {
                    FieldTrailingIcon(isValid = uiState.isPasswordValid, hasError = uiState.passwordError != null)
                },
                supportingText = {
                    Text(
                        text = "Debe incluir mayúsculas, minúsculas, número y símbolo",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                isError = uiState.passwordError != null,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password
                )
            )
            FieldErrorMessage(uiState.passwordError)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
                },
                trailingIcon = {
                    FieldTrailingIcon(
                        isValid = uiState.isConfirmPasswordValid,
                        hasError = uiState.confirmPasswordError != null
                    )
                },
                isError = uiState.confirmPasswordError != null,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password
                )
            )
            FieldErrorMessage(uiState.confirmPasswordError)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Teléfono (opcional)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Phone, contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Phone
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Selecciona tus géneros de interés",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.availableGenders.forEach { gender ->
                    val isSelected = uiState.selectedGenders.contains(gender)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                viewModel.onGenderToggled(gender, checked)
                            }
                        )
                        Text(text = gender)
                    }
                }
            }
            FieldErrorMessage(uiState.genderError)
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.generalError != null) {
                Text(
                    text = uiState.generalError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    focusManager.clearFocus(force = true)
                    viewModel.onSubmit()
                },
                enabled = !uiState.isSubmitting && !uiState.isEmailChecking
            ) {
                if (uiState.isSubmitting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else {
                    Text("Crear cuenta")
                }
            }

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldErrorMessage(errorMessage: String?) {
    if (errorMessage != null) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun FieldTrailingIcon(isValid: Boolean, hasError: Boolean) {
    when {
        hasError -> Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        isValid -> Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
