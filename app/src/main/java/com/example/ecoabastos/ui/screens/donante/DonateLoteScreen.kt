package com.example.ecoabastos.ui.screens.donante

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.components.EcoContextMessage
import com.example.ecoabastos.ui.components.EcoDateField
import com.example.ecoabastos.ui.components.EcoDropdown
import com.example.ecoabastos.ui.components.EcoPrimaryButton
import com.example.ecoabastos.ui.components.EcoTextField
import com.example.ecoabastos.ui.components.EcoTimeField
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Lo que de verdad acepta hoy el backend (RetiroRequest). El resto queda visual. */
data class RetiroData(val responsable: String)

/**
 * Pantalla "Donar lote" (registrar retiro), solo diseño.
 * ⚠️ RetiroRequest solo tiene "responsable". Cantidad retirada, organización y
 * observaciones se muestran pero no se envían hasta que el backend los acepte.
 */
@Composable
fun DonateLoteScreen(
    nombreLote: String,
    categoria: String,
    unidadMedida: String,
    onConfirmClick: (RetiroData) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var cantidadRetirada by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var organizacion by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }

    val formularioValido = organizacion.isNotBlank() && fecha.isNotBlank() && hora.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(text = "Donar lote", color = EcoColors.Texto, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Completa la información del ecoabasto",
            color = EcoColors.Gris,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Campo("Nombre del alimento") {
                EcoTextField(value = nombreLote, onValueChange = {}, placeholder = nombreLote)
            }
            Campo("Categoría") {
                EcoDropdown(value = categoria, onValueChange = {}, options = listOf(categoria), placeholder = categoria)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Etiqueta("Cantidad, retirada")
                    EcoTextField(value = cantidadRetirada, onValueChange = { cantidadRetirada = it }, placeholder = "Ej: 5")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Etiqueta("Unidad de medida")
                    EcoTextField(value = unidadMedida, onValueChange = {}, placeholder = unidadMedida)
                }
            }
            Column {
                Etiqueta("Fecha y hora del retiro")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EcoDateField(value = fecha, onValueChange = { fecha = it }, modifier = Modifier.weight(1f))
                    EcoTimeField(value = hora, onValueChange = { hora = it }, modifier = Modifier.weight(1f))
                }
            }
            Campo("Organización que retira") {
                EcoTextField(value = organizacion, onValueChange = { organizacion = it }, placeholder = "Ej: Fundación sonrisas")
            }
            Campo("Observaciones de la entrega") {
                EcoTextField(value = observaciones, onValueChange = { observaciones = it }, placeholder = "Ej: Envasado al vacío")
            }

            if (errorMessage != null) {
                EcoContextMessage(title = "No pudimos confirmar la donación", message = errorMessage)
            }

            EcoPrimaryButton(
                text = "Confirmar donacion",
                onClick = { onConfirmClick(RetiroData(responsable = organizacion.trim())) },
                enabled = formularioValido,
                isLoading = isLoading
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(texto, color = EcoColors.Texto, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun Campo(etiqueta: String, campo: @Composable () -> Unit) {
    Column { Etiqueta(etiqueta); campo() }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 850, name = "Vacío")
@Composable
private fun DonateLoteVacioPreview() {
    EcoTheme(darkTheme = false) {
        DonateLoteScreen(
            nombreLote = "Manzanas", categoria = "Frutas", unidadMedida = "kg",
            onConfirmClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 850, name = "Oscuro")
@Composable
private fun DonateLoteOscuroPreview() {
    EcoTheme(darkTheme = true) {
        DonateLoteScreen(
            nombreLote = "Manzanas", categoria = "Frutas", unidadMedida = "kg",
            onConfirmClick = {}
        )
    }
}