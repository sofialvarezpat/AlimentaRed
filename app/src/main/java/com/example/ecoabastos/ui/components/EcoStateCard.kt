package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Recuadro grande para cuando no hay datos o algo falló.
 * isError = false -> símbolo "—" (vacío). isError = true -> símbolo "!" (error).
 */
@Composable
fun EcoStateCard(
    title: String,
    message: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EcoColors.MensajeFondo, shape)
            .border(1.dp, if (isError) EcoColors.MensajeBorde else EcoColors.Borde, shape)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(EcoColors.Superficie, CircleShape)
                    .border(1.dp, EcoColors.MensajeBorde, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isError) "!" else "—",
                    color = EcoColors.VerdeOscuro,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = title,
                color = EcoColors.Texto,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = message,
            color = EcoColors.Gris,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        EcoActionButton(
            text = actionText,
            onClick = onActionClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PreviewContent() {
    Column(
        modifier = Modifier
            .background(EcoColors.Fondo)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        EcoStateCard(
            title = "Aún no tienes publicaciones",
            message = "Publica tu primer lote para compartir los alimentos disponibles.",
            actionText = "Publicar lote",
            onActionClick = {}
        )
        EcoStateCard(
            title = "No se cargaron tus publicaciones",
            message = "Comprueba tu conexión. Tus publicaciones no se han eliminado.",
            actionText = "Reintentar",
            onActionClick = {},
            isError = true
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoStateCardPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoStateCardDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}