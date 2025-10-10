package cl.duoc.appmovil2.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.appmovil2.R // Asegúrate de importar tu clase R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenCompact() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi App Adaptable") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Bienvenido a la vista Compacta",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            //Image(
            //    painter = painterResource(id = R.drawable.tu_imagen), // Reemplaza con tu imagen
            //    contentDescription = "Logo de la App",
            //    modifier = Modifier.fillMaxWidth().height(150.dp)
            //)
            Button(onClick = { /* Acción futura */ }) {
                Text("Presióname")
            }
        }
    }
}

@Preview(name = "Compact", widthDp = 360, heightDp = 800)
@Composable
fun PreviewCompact() {
    HomeScreenCompact()
}