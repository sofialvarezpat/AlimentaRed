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
import com.example.ecoabastos.ui.components.CategoriasLote
import com.example.ecoabastos.ui.components.EcoContextMessage
import com.example.ecoabastos.ui.components.EcoDateField
import com.example.ecoabastos.ui.components.EcoDropdown
import com.example.ecoabastos.ui.components.EcoPhotoUpload
import com.example.ecoabastos.ui.components.EcoPrimaryButton
import com.example.ecoabastos.ui.components.EcoTextField
import com.example.ecoabastos.ui.components.EcoTimeField
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme
import android.net.Uri

private val UnidadesMedida = listOf("kg", "g", "L", "ml", "unidades")
private val OpcionesPerecedero = listOf("Sí", "No")

/** Datos de un lote ya publicado, para precargar el formulario de edición. */
data class LoteEditable(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val cantidad: String,
    val unidadMedida: String,
    val fecha: String,
    val hora: String,
    val ubicacion: String,
    val observaciones: String
)

/**
 * Pantalla "Editar lote" (solo diseño).
 * ⚠️ El backend aún no tiene endpoint para actualizar un lote (PATCH /api/lotes/{id}/cancelar
 * solo cancela). Por eso onUpdateClick todavía no se conecta a nada real.
 */
@Composable
fun EditLoteScreen(
    lote: LoteEditable,
    onUpdateClick: (LoteEditable) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var nombre by remember { mutableStateOf(lote.nombre) }
    var categoria by remember { mutableStateOf(lote.categoria) }
    var cantidad by remember { mutableStateOf(lote.cantidad) }
    var unidadMedida by remember { mutableStateOf(lote.unidadMedida) }
    var perecedero by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(lote.fecha) }
    var hora by remember { mutableStateOf(lote.hora) }
    var ubicacion by remember { mutableStateOf(lote.ubicacion) }
    var observaciones by remember { mutableStateOf(lote.observaciones) }
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
        Text(text = "Editar lote", color = EcoColors.Texto, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Completa la información del ecoabasto",
            color = EcoColors.Gris,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Campo("Nombre del alimento") {
                EcoTextField(value = nombre, onValueChange = { nombre = it }, placeholder = "Ej: Arroz integral")
            }
            Campo("Categoría") {
                EcoDropdown(value = categoria, onValueChange = { categoria = it }, options = CategoriasLote, placeholder = "Selecciona una categoría")
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
            Campo("Ubicación de recogida") {
                EcoTextField(value = ubicacion, onValueChange = { ubicacion = it }, placeholder = "Ej: Calle 123, Local 4")
            }
            Campo("Observaciones de conservación") {
                EcoTextField(value = observaciones, onValueChange = { observaciones = it }, placeholder = "Ej: Envasado al vacío")
            }
            EcoPhotoUpload(fotoUri = fotoUri, onFotoSelected = { fotoUri = it })

            if (errorMessage != null) {
                EcoContextMessage(title = "No pudimos actualizar el lote", message = errorMessage)
            }

            EcoPrimaryButton(
                text = "Actualizar lote",
                onClick = {
                    onUpdateClick(
                        lote.copy(
                            nombre = nombre.trim(),
                            categoria = categoria,
                            cantidad = cantidad,
                            unidadMedida = unidadMedida,
                            fecha = fecha,
                            hora = hora,
                            ubicacion = ubicacion.trim(),
                            observaciones = observaciones.trim()
                        )
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
    Text(texto, color = EcoColors.Texto, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun Campo(etiqueta: String, campo: @Composable () -> Unit) {
    Column { Etiqueta(etiqueta); campo() }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 900, name = "Vacío")
@Composable
private fun EditLoteVacioPreview() {
    EcoTheme(darkTheme = false) {
        EditLoteScreen(
            lote = LoteEditable(1, "Manzanas", "Frutas", "15", "kg", "2026-11-05", "06:00 PM", "Calle 123, Local 4", ""),
            onUpdateClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 900, name = "Error")
@Composable
private fun EditLoteErrorPreview() {
    EcoTheme(darkTheme = false) {
        EditLoteScreen(
            lote = LoteEditable(1, "Manzanas", "Frutas", "15", "kg", "2026-11-05", "06:00 PM", "Calle 123, Local 4", ""),
            onUpdateClick = {},
            errorMessage = "Revisa los datos e inténtalo de nuevo."
        )
    }
}