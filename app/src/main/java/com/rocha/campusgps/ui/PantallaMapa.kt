package com.rocha.campusgps.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.rocha.campusgps.R
import com.rocha.campusgps.model.UbicacionUi
import com.rocha.campusgps.ui.theme.TecsupBorder
import com.rocha.campusgps.ui.theme.TecsupCyan
import com.rocha.campusgps.ui.theme.TecsupCyanContainer
import com.rocha.campusgps.ui.theme.TecsupCyanDark
import com.rocha.campusgps.ui.theme.TecsupCyanSurface
import com.rocha.campusgps.ui.theme.TecsupTextPrimary
import com.rocha.campusgps.ui.theme.TecsupTextSecondary
import com.rocha.campusgps.util.formatearDistancia
import com.rocha.campusgps.viewmodel.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(vm: MapaViewModel) {
    val estado by vm.estado.collectAsState()
    val lugaresFiltrados by vm.lugaresFiltrados.collectAsState()
    val categoriaSeleccionada by vm.categoriaSeleccionada.collectAsState()
    val radioSeleccionado by vm.radioSeleccionado.collectAsState()
    val seleccionado by vm.seleccionado.collectAsState()
    val masCercano by vm.masCercano.collectAsState()
    val contexto = LocalContext.current

    // Estado interactivo del panel inferior (subir y bajar)
    var expandido by remember { mutableStateOf(true) }

    val alturaSpacer by animateDpAsState(
        targetValue = if (expandido) 360.dp else 165.dp,
        animationSpec = tween(durationMillis = 300),
        label = "alturaSpacer"
    )

    val alturaLista by animateDpAsState(
        targetValue = if (expandido) 200.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "alturaLista"
    )

    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val concedido = resultado.values.any { it }
        if (concedido) {
            vm.buscar()
        } else {
            vm.permisoDenegado()
        }
    }

    fun alPulsar() {
        val fine = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fine || coarse) {
            vm.buscar()
        } else {
            lanzador.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun abrirAjustes() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", contexto.packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        contexto.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_campusgps),
                            contentDescription = "Logo CampusGPS",
                            modifier = Modifier
                                .height(32.dp)
                                .padding(start = 4.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Mapa arriba ocupando el espacio superior con espacio reservado abajo
            Column(modifier = Modifier.fillMaxSize()) {
                MapaUbicacion(
                    estado = estado,
                    lugares = lugaresFiltrados,
                    masCercano = masCercano,
                    seleccionado = seleccionado,
                    radio = radioSeleccionado,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                // Espacio inferior animado reservado para el panel flotante
                Spacer(modifier = Modifier.height(alturaSpacer))
            }

            // 2. Panel blanco inferior estilo BottomSheet con bordes redondeados arriba (como en la imagen)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, TecsupBorder.copy(alpha = 0.8f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    // Tirador / Handle bar superior interactivo (subir y bajar al pulsar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandido = !expandido }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(5.dp)
                                .background(Color(0xFFCBD5E1), RoundedCornerShape(3.dp))
                        )
                    }

                    // Fila horizontal de Chips de filtrado (Celeste y Blanco)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(vm.categorias) { cat ->
                            val esSeleccionado = cat == categoriaSeleccionada
                            FilterChip(
                                selected = esSeleccionado,
                                onClick = { vm.seleccionarCategoria(cat) },
                                label = {
                                    Text(
                                        text = cat,
                                        fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = esSeleccionado,
                                    borderColor = TecsupBorder,
                                    selectedBorderColor = TecsupCyan,
                                    borderWidth = if (esSeleccionado) 1.5.dp else 1.dp
                                ),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    selectedContainerColor = TecsupCyanContainer,
                                    labelColor = TecsupTextSecondary,
                                    selectedLabelColor = TecsupCyanDark
                                )
                            )
                        }
                    }

                    // Selector de radio interactivo cuando la ubicación ya fue obtenida
                    if (estado is UbicacionUi.Lista) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Radio:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TecsupTextSecondary
                            )
                            vm.radiosDisponibles.forEach { r ->
                                val esActivo = r == radioSeleccionado
                                val etiqueta = r?.let { "${it}m" } ?: "Todo"
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (esActivo) TecsupCyan else TecsupCyanSurface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (esActivo) TecsupCyan else TecsupBorder
                                    ),
                                    modifier = Modifier.clickable { vm.seleccionarRadio(r) }
                                ) {
                                    Text(
                                        text = etiqueta,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (esActivo) FontWeight.Bold else FontWeight.Medium,
                                        color = if (esActivo) Color.White else TecsupCyanDark,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Controles centrales (Celeste y Blanco)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Botón principal
                        Button(
                            onClick = { alPulsar() },
                            enabled = estado !is UbicacionUi.Buscando,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TecsupCyan,
                                contentColor = Color.White,
                                disabledContainerColor = TecsupCyan.copy(alpha = 0.5f),
                                disabledContentColor = Color.White.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Obtener mi ubicación",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Botón secundario: Ir al más cercano
                        OutlinedButton(
                            onClick = { masCercano?.let { vm.seleccionar(it.id) } },
                            enabled = masCercano != null,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (masCercano != null) TecsupCyan else TecsupBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TecsupCyanDark,
                                disabledContentColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Ir al más cercano",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Texto: Más cercano estrictamente en una sola línea
                        masCercano?.let { cercano ->
                            val dist = lugaresFiltrados.firstOrNull { it.lugar.id == cercano.id }?.metros
                            val textoDist = dist?.let { " (${formatearDistancia(it)})" }.orEmpty()
                            Text(
                                text = "Más cercano: ${cercano.nombre}$textoDist",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TecsupCyanDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        when (val e = estado) {
                            UbicacionUi.Buscando -> {
                                CircularProgressIndicator(
                                    color = TecsupCyan,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            is UbicacionUi.Error -> {
                                MensajeError(
                                    error = e,
                                    onReintentar = { alPulsar() },
                                    onAbrirAjustes = { abrirAjustes() }
                                )
                            }

                            else -> Unit
                        }
                    }

                    // 4. Lista fija abajo animada (Fondo blanco y tarjetas limpias)
                    ListaLugares(
                        lugares = lugaresFiltrados,
                        seleccionado = seleccionado,
                        onSeleccionar = { vm.seleccionar(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(alturaLista)
                    )
                }
            }
        }
    }
}
