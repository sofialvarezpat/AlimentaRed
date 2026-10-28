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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Tarjeta de un lote disponible en el catálogo para organizaciones receptoras.
 */
@Composable
fun CatalogoLoteCard(
    nombre: String,
    donante: String,
    cantidadDisponible: String,
    categoria: String,
    distancia: String = "1.5 km",
    onSolicitarClick: () -> Unit,
    modifier: Modifier = Modifier,
    foto: @Composable () -> Unit = { LoteFotoPlaceholder() }
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 76.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                foto()
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = nombre,
                    color = EcoColors.Texto,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Donado por: $donante",
                    color = EcoColors.Gris,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EcoTag(
                        text = categoria,
                        backgroundColor = EcoColors.Suave,
                        textColor = EcoColors.VerdeOscuro
                    )
                    EcoTag(
                        text = distancia,
                        backgroundColor = EcoColors.VerdeFondo,
                        textColor = EcoColors.VerdeOscuro
                    )
                }
                Text(
                    text = "$cantidadDisponible disponibles",
                    color = EcoColors.VerdeOscuro,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        EcoActionButton(
            text = "Solicitar Donación",
            onClick = onSolicitarClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = EcoColors.Accion,
            height = 36.dp
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun CatalogoLoteCardPreview() {
    EcoTheme(darkTheme = false) {
        Box(modifier = Modifier.background(EcoColors.Fondo).padding(16.dp)) {
            CatalogoLoteCard(
                nombre = "Tomates Frescos",
                donante = "Supermercado Central",
                cantidadDisponible = "25 Kg",
                categoria = "Verduras",
                distancia = "1.2 km",
                onSolicitarClick = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun CatalogoLoteCardDarkPreview() {
    EcoTheme(darkTheme = true) {
        Box(modifier = Modifier.background(EcoColors.Fondo).padding(16.dp)) {
            CatalogoLoteCard(
                nombre = "Tomates Frescos",
                donante = "Supermercado Central",
                cantidadDisponible = "25 Kg",
                categoria = "Verduras",
                distancia = "1.2 km",
                onSolicitarClick = {}
            )
        }
    }
}
