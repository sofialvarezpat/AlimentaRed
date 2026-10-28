package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecoabastos.data.model.EstadoLote
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Tarjeta de un lote publicado.
 * Muestra la imagen real si existe o el icono de relleno, además de su estado reactivo.
 */
@Composable
fun LoteCard(
    nombre: String,
    cantidad: String,
    categoria: String,
    vigencia: String,
    estado: EstadoLote = EstadoLote.DISPONIBLE,
    onEditClick: () -> Unit,
    onDetailClick: () -> Unit,
    imagenUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    val (estadoTexto, estadoBg, estadoColor) = when (estado) {
        EstadoLote.ENTREGADO -> Triple("Recogido", EcoColors.VerdeFondo, EcoColors.VerdeOscuro)
        EstadoLote.RESERVADO -> Triple("En retiro", Color(0xFFE8F5E9), EcoColors.Accion)
        EstadoLote.DISPONIBLE -> Triple("Pendiente", EcoColors.Suave, EcoColors.VerdeOscuro)
        EstadoLote.VENCIDO -> Triple("Vencido", Color(0xFFFFEBEE), Color(0xFFC62828))
        EstadoLote.CANCELADO -> Triple("Cancelado", Color(0xFFFFEBEE), Color(0xFFC62828))
    }

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
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Foto + datos del lote
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(width = 75.dp, height = 72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(EcoColors.Suave)
            ) {
                LoteFoto(imagenUrl = imagenUrl)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = nombre,
                    color = EcoColors.Texto,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cantidad,
                    color = EcoColors.Gris,
                    fontSize = 12.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EcoTag(
                        text = categoria,
                        backgroundColor = EcoColors.Suave,
                        textColor = EcoColors.VerdeOscuro
                    )
                    EcoTag(
                        text = estadoTexto,
                        backgroundColor = estadoBg,
                        textColor = estadoColor
                    )
                    EcoTag(
                        text = vigencia,
                        backgroundColor = EcoColors.Alerta,
                        textColor = Color.White
                    )
                }
            }
        }

        // Botones
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EcoActionButton(
                text = "Editar",
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
                containerColor = EcoColors.Secundario,
                contentColor = EcoColors.Texto,
                height = 28.dp,
                cornerRadius = 12.dp
            )
            EcoActionButton(
                text = "Ver detalle",
                onClick = onDetailClick,
                modifier = Modifier.weight(1f),
                height = 28.dp
            )
        }
    }
}

@Composable
fun LoteFoto(
    imagenUrl: String?,
    modifier: Modifier = Modifier
) {
    if (!imagenUrl.isNullOrBlank()) {
        val imageData: Any = if (imagenUrl.startsWith("data:image")) {
            runCatching {
                val base64Part = imagenUrl.substringAfter("base64,", "")
                android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
            }.getOrDefault(imagenUrl)
        } else {
            imagenUrl
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageData)
                .crossfade(true)
                .build(),
            contentDescription = "Foto del alimento",
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )
    } else {
        LoteFotoPlaceholder(modifier = modifier)
    }
}

@Composable
fun LoteFotoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Suave),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Eco,
            contentDescription = null,
            tint = EcoColors.Accion,
            modifier = Modifier.size(32.dp)
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
        LoteCard(
            nombre = "Manzanas",
            cantidad = "15Kg",
            categoria = "Frutas",
            vigencia = "Vence en 4 días",
            estado = EstadoLote.ENTREGADO,
            onEditClick = {},
            onDetailClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun LoteCardPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}
