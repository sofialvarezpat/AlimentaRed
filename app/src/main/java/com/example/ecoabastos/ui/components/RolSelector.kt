package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.data.model.Rol
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Selector de tipo de cuenta del registro.
 * Solo permite DONANTE o RECEPTORA (coordinador y administrador no se registran desde la app).
 */
@Composable
fun RolSelector(
    selected: Rol,
    onSelect: (Rol) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "¿Cómo quieres participar?",
            color = EcoColors.Gris,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RolOption(
                text = "Donador",
                selected = selected == Rol.DONANTE,
                onClick = { onSelect(Rol.DONANTE) },
                modifier = Modifier.weight(112f)
            )
            RolOption(
                text = "Organización receptora",
                selected = selected == Rol.RECEPTORA,
                onClick = { onSelect(Rol.RECEPTORA) },
                modifier = Modifier.weight(203f)
            )
        }
    }
}

@Composable
private fun RolOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(if (selected) EcoColors.VerdeFondo else EcoColors.Superficie)
            .border(
                width = 1.dp,
                color = if (selected) EcoColors.VerdeOscuro else EcoColors.Borde,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioDot(selected = selected)
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            maxLines = 1,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) EcoColors.VerdeOscuro else EcoColors.Gris
        )
    }
}

/** El circulito de la opción: vacío si no está elegida, con punto si lo está. */
@Composable
private fun RadioDot(selected: Boolean) {
    val color = if (selected) EcoColors.VerdeOscuro else EcoColors.Gris
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(1.5.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
private fun PreviewContent() {
    var rol by remember { mutableStateOf(Rol.DONANTE) }
    Box(
        modifier = Modifier
            .background(EcoColors.Fondo)
            .padding(24.dp)
    ) {
        RolSelector(selected = rol, onSelect = { rol = it })
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun RolSelectorPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun RolSelectorDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}