package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Lista fija de categorías de lote. Si el backend la expone algún día como endpoint, se reemplaza esto por una llamada a la API. */
val CategoriasLote = listOf(
    "Frutas", "Verduras", "Panadería", "Lácteos", "Carnes", "Enlatados", "Otros"
)

/** Selector desplegable con el mismo look que EcoTextField. */
@Composable
fun EcoDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(20.dp)

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp)
                .shadow(elevation = 3.dp, shape = shape)
                .background(EcoColors.Superficie, shape)
                .border(1.dp, EcoColors.Borde, shape)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.ifEmpty { placeholder },
                fontSize = 14.sp,
                fontStyle = if (value.isEmpty()) FontStyle.Italic else FontStyle.Normal,
                color = if (value.isEmpty()) EcoColors.Gris else EcoColors.Texto
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = EcoColors.Gris
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(EcoColors.Superficie)
        ) {
            options.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, color = EcoColors.Texto) },
                    onClick = {
                        onValueChange(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PreviewContent() {
    var categoria by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .background(EcoColors.Fondo)
            .padding(24.dp)
    ) {
        EcoDropdown(
            value = categoria,
            onValueChange = { categoria = it },
            options = CategoriasLote,
            placeholder = "Selecciona una categoría"
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoDropdownPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoDropdownDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}