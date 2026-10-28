package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Cabecera de las pantallas de inicio: curva verde clara, saludo,
 * buscador y botón "+" para publicar.
 */
@Composable
fun HomeHeader(
    nombre: String,
    titulo: String,
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Espacio de la barra de estado del teléfono (reloj, batería)
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val colorCurva = EcoColors.Suave

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp + statusBar)
            .drawBehind {
                // La curva es una elipse de 333 x 220, igual que en Figma
                drawOval(
                    color = colorCurva,
                    topLeft = Offset((-31).dp.toPx(), (-72).dp.toPx() + statusBar.toPx()),
                    size = Size(333.dp.toPx(), 220.dp.toPx())
                )
            }
    ) {
        Column(modifier = Modifier.padding(start = 36.dp, top = 53.dp + statusBar)) {
            Text(
                text = "¡Hola, $nombre!",
                color = EcoColors.Texto,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = titulo,
                color = EcoColors.Texto,
                fontSize = 24.sp,
                lineHeight = 30.sp
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 31.dp, end = 33.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchField(
                value = busqueda,
                onValueChange = onBusquedaChange,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(EcoColors.Accion)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Publicar lote",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(42.dp)
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(horizontal = 16.dp),
        singleLine = true,
        textStyle = TextStyle(fontSize = 13.sp, color = EcoColors.Texto),
        cursorBrush = SolidColor(EcoColors.VerdeOscuro),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Ej. Manzanas",
                            fontSize = 11.sp,
                            color = EcoColors.Gris.copy(alpha = 0.7f)
                        )
                    }
                    innerTextField()
                }
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Buscar",
                    tint = EcoColors.Accion,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    )
}

@Composable
private fun PreviewContent() {
    var busqueda by remember { mutableStateOf("") }
    Column(modifier = Modifier.background(EcoColors.Fondo)) {
        HomeHeader(
            nombre = "Juan",
            titulo = "Tus Publicaciones",
            busqueda = busqueda,
            onBusquedaChange = { busqueda = it },
            onAddClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun HomeHeaderPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun HomeHeaderDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}