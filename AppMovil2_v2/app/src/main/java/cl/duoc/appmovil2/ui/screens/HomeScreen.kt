package cl.duoc.appmovil2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.appmovil2.navegation.AppScreens // <-- Importación correcta para la navegación
import cl.duoc.appmovil2.viewmodel.MainViewModel
import kotlinx.coroutines.launch

// Se eliminaron las importaciones y declaraciones innecesarias

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel() // Inyecta el ViewModel
) {
    // 1. Estado para controlar si el menú lateral está abierto o cerrado.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // 2. CoroutineScope para abrir y cerrar el menú de forma asíncrona (sin bloquear la UI).
    val scope = rememberCoroutineScope()

    // 3. Contenedor principal que permite tener un menú lateral.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // 4. El contenido del menú lateral.
            ModalDrawerSheet {
                Text("Menú", modifier = Modifier.padding(16.dp))
                Divider()
                NavigationDrawerItem(
                    label = { Text(text = "Ir a Perfil") },
                    selected = false,
                    onClick = {
                        // Al hacer clic, cerramos el menú y le decimos al ViewModel que navegue.
                        scope.launch { drawerState.close() }
                        // CORRECCIÓN: Usar AppScreens en lugar de Screen
                        viewModel.navegateTo(AppScreens.Profile)
                    }
                )
                // Aquí podrías agregar más items...
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inicio") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            // CORRECIÓN: Se debe usar Icons.Default.Menu
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column (
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Contenido de la pantalla de inicio")
                Spacer(modifier= Modifier.height(16.dp))
                Button(onClick = {
                    // CORRECCIÓN: Usar AppScreens en lugar de Screen
                    viewModel.navegateTo(AppScreens.Settings)
                }){
                    Text("Ir a ajustes")
                }
            }
        } // El paréntesis de cierre de Column estaba mal ubicado, se movió aquí
    }
}
