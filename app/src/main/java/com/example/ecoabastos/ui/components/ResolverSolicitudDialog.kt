package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ecoabastos.ui.theme.EcoColors

@Composable
fun ResolverSolicitudDialog(
    nombreLote: String,
    organizacion: String,
    cantidadSolicitada: String,
    onDismiss: () -> Unit,
    onResolver: (aprobada: Boolean, cantidadAsignada: Double?, observaciones: String?) -> Unit
) {
    var cantidadAsignadaText by remember { mutableStateOf(cantidadSolicitada.replace(Regex("[^0-9.]"), "")) }
    var observaciones by remember { mutableStateOf("") }

    val cantidadDouble = cantidadAsignadaText.replace(",", ".").toDoubleOrNull()
    val cantidadValida = cantidadDouble != null && cantidadDouble > 0

    Dialog(onDismissRequest = onDismiss) {
        val shape = RoundedCornerShape(20.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoColors.Superficie, shape)
                .border(1.dp, EcoColors.Borde, shape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Resolver Solicitud",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Texto
            )

            Text(
                text = "$organizacion solicita $cantidadSolicitada de \"$nombreLote\".",
                fontSize = 13.sp,
                color = EcoColors.Gris
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Cantidad a asignar",
                    fontSize = 12.sp,
                    color = EcoColors.Texto,
                    fontWeight = FontWeight.Medium
                )
                EcoTextField(
                    value = cantidadAsignadaText,
                    onValueChange = { cantidadAsignadaText = it },
                    placeholder = "Ej. 10",
                    keyboardType = KeyboardType.Number
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Observaciones (opcional)",
                    fontSize = 12.sp,
                    color = EcoColors.Texto,
                    fontWeight = FontWeight.Medium
                )
                EcoTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    placeholder = "Notas para la entrega o motivo"
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EcoActionButton(
                    text = "Rechazar",
                    onClick = {
                        onResolver(false, null, observaciones.trim().ifEmpty { null })
                    },
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFC62828),
                    height = 42.dp
                )

                EcoActionButton(
                    text = "Aprobar",
                    onClick = {
                        if (cantidadValida) {
                            onResolver(true, cantidadDouble, observaciones.trim().ifEmpty { null })
                        }
                    },
                    modifier = Modifier.weight(1f),
                    containerColor = EcoColors.Accion,
                    height = 42.dp
                )
            }
        }
    }
}
