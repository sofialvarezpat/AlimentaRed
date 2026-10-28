package com.example.ecoabastos.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp) = this.drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )
    )
}

/**
 * Botón de borde punteado para elegir una foto de la galería.
 * Si hay un Uri seleccionado, muestra la miniatura real de la foto.
 */
@Composable
fun EcoPhotoUpload(
    fotoUri: Uri?,
    onFotoSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> onFotoSelected(uri) }
    )
    val shape = RoundedCornerShape(16.dp)

    if (fotoUri != null) {
        // Miniatura real de la foto seleccionada con opción de cambiar
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(shape)
                    .border(1.dp, EcoColors.Borde, shape)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(fotoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto del lote",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(shape)
                    .dashedBorder(EcoColors.Accion, 16.dp)
                    .clickable { launcher.launch("image/*") }
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = EcoColors.Accion,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Cambiar foto",
                    color = EcoColors.Accion,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    } else {
        // Botón vacío para seleccionar foto
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(53.dp)
                .clip(shape)
                .dashedBorder(EcoColors.Borde, 16.dp)
                .clickable { launcher.launch("image/*") }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = EcoColors.Accion,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Subir foto",
                color = EcoColors.Texto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun PreviewContent() {
    Column(
        modifier = Modifier.background(EcoColors.Fondo).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EcoPhotoUpload(fotoUri = null, onFotoSelected = {})
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoPhotoUploadPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoPhotoUploadDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}
