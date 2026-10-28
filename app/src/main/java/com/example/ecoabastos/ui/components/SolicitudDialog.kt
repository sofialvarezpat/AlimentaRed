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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ecoabastos.ui.theme.EcoColors

@Composable
fun SolicitudDialog(
    nombreLote: String,
    cantidadDisponible: String,
    onDismiss: () -> Unit,
    onConfirm: (cantidad: Double) -> Unit
) {
    var cantidadText by remember { mutableStateOf("") }
    val cantidadDouble = cantidadText.replace(",", ".").toDoubleOrNull()
    val esValido = cantidadDouble != null && cantidadDouble > 0

    Dialog(onDismissRequest = onDismiss) {
        val shape = RoundedCornerShape(20.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoColors.Superficie, shape)
                .border(1.dp, EcoColors.Borde, shape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Solicitar Donación",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Texto
            )

            Text(
                text = "Indica la cantidad que necesitas de \"$nombreLote\" (Disponible: $cantidadDisponible):",
                fontSize = 13.sp,
                color = EcoColors.Gris
            )

            EcoTextField(
                value = cantidadText,
                onValueChange = { cantidadText = it },
                placeholder = "Cantidad a solicitar",
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EcoActionButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    containerColor = EcoColors.Secundario,
                    contentColor = EcoColors.Texto,
                    height = 40.dp
                )

                EcoActionButton(
                    text = "Confirmar",
                    onClick = {
                        if (cantidadDouble != null) {
                            onConfirm(cantidadDouble)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    containerColor = EcoColors.Accion,
                    height = 40.dp
                )
            }
        }
    }
}
