package com.rocha.campusgps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rocha.campusgps.model.TipoError
import com.rocha.campusgps.model.UbicacionUi

@Composable
fun MensajeError(
    error: UbicacionUi.Error,
    onReintentar: () -> Unit,
    onAbrirAjustes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error.mensaje,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        when (error.tipo) {
            TipoError.SIN_PERMISO -> {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onAbrirAjustes
                ) {
                    Text("Abrir ajustes")
                }
            }

            TipoError.GPS_APAGADO, TipoError.OTRO -> {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onReintentar
                ) {
                    Text("Reintentar")
                }
            }

            TipoError.SIN_PLAY_SERVICES -> {
                // Sin botón de acción
            }
        }
    }
}
