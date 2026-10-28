package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Un destino de la barra: su nombre, su ícono normal y su ícono cuando está seleccionado. */
data class BottomBarItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

/** Barra inferior con el destino seleccionado resaltado en una "píldora" verde clara. */
@Composable
fun EcoBottomBar(
    items: List<BottomBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(EcoColors.Fondo)
            .navigationBarsPadding()
            .height(72.dp)
            .padding(horizontal = 36.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        items.forEachIndexed { index, item ->
            BottomBarDestination(
                item = item,
                selected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun BottomBarDestination(
    item: BottomBarItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(top = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 44.dp)
                .background(
                    color = if (selected) EcoColors.Suave else Color.Transparent,
                    shape = RoundedCornerShape(44.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.icon,
                contentDescription = item.label,
                tint = if (selected) EcoColors.Accion else EcoColors.Gris,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(2.5.dp))
        // Puntito debajo del destino seleccionado
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(
                    color = if (selected) EcoColors.Accion else Color.Transparent,
                    shape = CircleShape
                )
        )
    }
}

// ---------- Vistas previas ----------

private val ejemploDestinos = listOf(
    BottomBarItem("Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    BottomBarItem("Buscar", Icons.Outlined.Search, Icons.Filled.Search),
    BottomBarItem("Perfil", Icons.Outlined.Person, Icons.Filled.Person)
)

@Composable
private fun PreviewContent() {
    var seleccionado by remember { mutableIntStateOf(0) }
    EcoBottomBar(
        items = ejemploDestinos,
        selectedIndex = seleccionado,
        onSelect = { seleccionado = it }
    )
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoBottomBarPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoBottomBarDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}