package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Botón de acción en forma de píldora. Por defecto es el verde fuerte de las pantallas internas. */
@Composable
fun EcoActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = EcoColors.Accion,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    height: Dp = 44.dp,
    cornerRadius: Dp = 999.dp
) {
    val bg = if (enabled) containerColor else containerColor.copy(alpha = 0.5f)
    val fg = if (enabled) contentColor else contentColor.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun PreviewContent() {
    Column(
        modifier = Modifier
            .background(EcoColors.Fondo)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EcoActionButton(text = "Publicar lote", onClick = {}, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EcoActionButton(
                text = "Editar",
                onClick = {},
                modifier = Modifier.weight(1f),
                containerColor = EcoColors.Secundario,
                contentColor = EcoColors.Texto,
                height = 28.dp,
                cornerRadius = 12.dp
            )
            EcoActionButton(
                text = "Ver detalle",
                onClick = {},
                modifier = Modifier.weight(1f),
                height = 28.dp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoActionButtonPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoActionButtonDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}