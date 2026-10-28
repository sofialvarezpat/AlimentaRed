package com.example.ecoabastos.ui.screens.donante

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.ecoabastos.ui.components.CategoriasLote
import com.example.ecoabastos.ui.components.EcoContextMessage
import com.example.ecoabastos.ui.components.EcoDropdown
import com.example.ecoabastos.ui.components.EcoDateField
import com.example.ecoabastos.ui.components.EcoTimeField
import com.example.ecoabastos.ui.components.EcoPhotoUpload
import com.example.ecoabastos.ui.components.EcoPrimaryButton
import com.example.ecoabastos.ui.components.EcoTextField
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

private val UnidadesMedida = listOf("kg", "g", "L", "ml", "unidades")
private val OpcionesPerecedero = listOf("Sí", "No")

/** Combina "yyyy-MM-dd" + "hh:mm AM/PM" en ISO "yyyy-MM-ddTHH:mm:00". Vacío si falta algo. */
private fun construirFechaIso(fecha: String, hora: String): String {
    if (fecha.isBlank() || hora.isBlank()) return ""
    val partes = hora.split(" ")
    val hhmm = partes[0].split(":")
    var h = hhmm[0].toIntOrNull() ?: return ""
    val m = hhmm.getOrElse(1) { "00" }
    val periodo = partes.getOrElse(1) { "AM" }
    if (periodo == "PM" && h != 12) h += 12
    if (periodo == "AM" && h == 12) h = 0
    val hh = h.toString().padStart(2, '0')
    return "${fecha}T$hh:$m:00"
}

/**
 * Pantalla "Formulario de lote" (Publicar lote), solo diseño.
 * No habla con el backend: entrega los datos ya armados mediante onPublishClick.
 * "¿Perecedero?" y "Observaciones de conservación" no se envían: LoteRequest no tiene esos campos.
 */
@Composable
fun PublishLoteScreen(
    onPublishClick: (
        nombre: String,
        categoria: String,
        cantidad: Double,
        unidadMedida: String,
        fechaLimiteIso: String,
        ubicacion: String,
        fotoUri: Uri?
    ) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var unidadMedida by remember { mutableStateOf("") }
    var perecedero by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    val cantidadDouble = cantidad.replace(",", ".").toDoubleOrNull()
    val formularioValido = nombre.isNotBlank() &&
            categoria.isNotBlank() &&
            cantidadDouble != null && cantidadDouble > 0 &&
            unidadMedida.isNotBlank() &&
            fecha.isNotBlank() && hora.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Formulario de lote",
            color = EcoColors.Texto,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Completa la información del ecoabasto",
            color = EcoColors.Gris,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CampoConEtiqueta("Nombre del alimento") {
                EcoTextField(value = nombre, onValueChange = { nombre = it }, placeholder = "Ej: Arroz integral")
            }

            CampoConEtiqueta("Categoría") {
                EcoDropdown(
                    value = categoria,
                    onValueChange = { categoria = it },
                    options = CategoriasLote,
                    placeholder = "Selecciona una categoría"
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Etiqueta("Cantidad")
                    EcoTextField(value = cantidad, onValueChange = { cantidad = it }, placeholder = "Ej: 5")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Etiqueta("Unidad de medida")
                    EcoDropdown(value = unidadMedida, onValueChange = { unidadMedida = it }, options = UnidadesMedida, placeholder = "Ej: kg")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Etiqueta("¿Perecedero?")
                    EcoDropdown(value = perecedero, onValueChange = { perecedero = it }, options = OpcionesPerecedero, placeholder = "Sí / No")
                }
            }

            Column {
                Etiqueta("Fecha y hora límite")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EcoDateField(value = fecha, onValueChange = { fecha = it }, modifier = Modifier.weight(1f))
                    EcoTimeField(value = hora, onValueChange = { hora = it }, modifier = Modifier.weight(1f))
                }
            }

            CampoConEtiqueta("Ubicación de recogida") {
                EcoTextField(value = ubicacion, onValueChange = { ubicacion = it }, placeholder = "Ej: Calle 123, Local 4")
            }

            CampoConEtiqueta("Observaciones de conservación") {
                EcoTextField(value = observaciones, onValueChange = { observaciones = it }, placeholder = "Ej: Envasado al vacío")
            }

            EcoPhotoUpload(fotoUri = fotoUri, onFotoSelected = { fotoUri = it })

            if (errorMessage != null) {
                EcoContextMessage(title = "No pudimos publicar el lote", message = errorMessage)
            }

            EcoPrimaryButton(
                text = "Publicar lote Ahora",
                onClick = {
                    onPublishClick(
                        nombre.trim(),
                        categoria,
                        cantidadDouble ?: 0.0,
                        unidadMedida,
                        construirFechaIso(fecha, hora),
                        ubicacion.trim(),
                        fotoUri
                    )
                },
                enabled = formularioValido,
                isLoading = isLoading
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        text = texto,
        color = EcoColors.Texto,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CampoConEtiqueta(etiqueta: String, campo: @Composable () -> Unit) {
    Column {
        Etiqueta(etiqueta)
        campo()
    }
}

// ---------- Vistas previas ----------
@Preview(showBackground = true, widthDp = 402, heightDp = 900, name = "Vacío")
@Composable
private fun PublishLoteVacioPreview() {
    EcoTheme(darkTheme = false) {
        PublishLoteScreen(onPublishClick = { _, _, _, _, _, _, _ -> })
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 900, name = "Error")
@Composable
private fun PublishLoteErrorPreview() {
    EcoTheme(darkTheme = false) {
        PublishLoteScreen(
            onPublishClick = { _, _, _, _, _, _, _ -> },
            errorMessage = "Revisa los datos e inténtalo de nuevo."
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 900, name = "Oscuro")
@Composable
private fun PublishLoteOscuroPreview() {
    EcoTheme(darkTheme = true) {
        PublishLoteScreen(onPublishClick = { _, _, _, _, _, _, _ -> })
    }
}