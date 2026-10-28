package com.example.ecoabastos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.R
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

// La curva de abajo es una elipse grande de 377dp de alto,
// con su borde inferior al final del encabezado (igual que en Figma).
private object CurvedHeaderShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val ellipseHeight = 377f * density.density
        val path = Path().apply {
            addOval(
                Rect(
                    left = -size.width * 0.042f,
                    top = size.height - ellipseHeight,
                    right = size.width * 1.035f,
                    bottom = size.height
                )
            )
        }
        return Outline.Generic(path)
    }
}

/**
 * Encabezado verde con el logo.
 * Login: height = 257.dp, logoTop = 63.dp
 * Registro: height = 218.dp, logoTop = 24.dp
 */
@Composable
fun BrandHeader(
    height: Dp,
    logoTop: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(EcoColors.Cabecera, CurvedHeaderShape)
    ) {
        Row(modifier = Modifier.padding(start = 71.dp, top = logoTop)) {
            Image(
                painter = painterResource(R.drawable.logo_eco_abasto),
                contentDescription = "Logo Eco Abasto",
                modifier = Modifier.size(width = 134.dp, height = 133.dp)
            )
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .width(1.dp)
                    .height(98.dp)
                    .background(Color.White)
            )
            Text(
                text = "Eco\nAbasto",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp, top = 28.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun BrandHeaderPreview() {
    EcoTheme(darkTheme = false) {
        Box(modifier = Modifier.background(EcoColors.Fondo)) {
            BrandHeader(height = 257.dp, logoTop = 63.dp)
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun BrandHeaderDarkPreview() {
    EcoTheme(darkTheme = true) {
        Box(modifier = Modifier.background(EcoColors.Fondo)) {
            BrandHeader(height = 257.dp, logoTop = 63.dp)
        }
    }
}