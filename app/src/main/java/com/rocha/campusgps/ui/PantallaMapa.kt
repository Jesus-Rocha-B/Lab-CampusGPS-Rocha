package com.rocha.campusgps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rocha.campusgps.model.UbicacionUi
import com.rocha.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsState()

    // TODO Paso 2: pedir permiso en tiempo de ejecución antes de llamar a vm.buscar()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CampusGPS") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { vm.buscar() },
                enabled = estado !is UbicacionUi.Buscando
            ) {
                Text("Obtener mi ubicación")
            }

            when (val e = estado) {
                UbicacionUi.Inicial -> {
                    Text(
                        text = "Pulsa el botón para empezar",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                UbicacionUi.Buscando -> {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator()
                }

                is UbicacionUi.Lista -> {
                    Text("Latitud: ${"%.4f".format(e.posicion.latitud)}")
                    Text("Longitud: ${"%.4f".format(e.posicion.longitud)}")
                }

                is UbicacionUi.Error -> {
                    Text(
                        text = e.mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
