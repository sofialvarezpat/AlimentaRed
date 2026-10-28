package com.example.ecoabastos.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Todos los colores de la app, en una versión clara y una oscura. */
data class EcoPalette(
    val fondo: Color,          // fondo de pantalla
    val superficie: Color,     // campos de texto, tarjetas, selectores
    val cabecera: Color,       // encabezado verde con el logo
    val boton: Color,          // botón principal
    val verdeOscuro: Color,    // títulos, bordes seleccionados, cursor
    val verdeClaro: Color,     // subtítulos e íconos
    val verdeFondo: Color,     // fondo de la opción seleccionada
    val mensajeFondo: Color,   // recuadro de mensajes (error)
    val mensajeBorde: Color,
    val gris: Color,           // textos secundarios
    val borde: Color,          // bordes de campos
    val texto: Color,          // texto principal
    val accion: Color,         // botones verdes ("Ver detalle", "+", "Publicar lote")
    val suave: Color,          // verde muy claro: curva, menú inferior, etiquetas
    val secundario: Color,     // botones secundarios ("Editar")
    val alerta: Color
)

val LightPalette = EcoPalette(
    fondo = Color(0xFFFFFFFF),
    superficie = Color(0xFFFFFFFF),
    cabecera = Color(0xFF42552C),
    boton = Color(0xFF42552C),
    verdeOscuro = Color(0xFF42552C),
    verdeClaro = Color(0xFF98A973),
    verdeFondo = Color(0xFFF0F3E9),
    mensajeFondo = Color(0xFFF5F6EF),
    mensajeBorde = Color(0xFFBEC69D),
    gris = Color(0xFF6B7280),
    borde = Color(0xFFE5E7EB),
    texto = Color(0xFF111827),
    accion = Color(0xFF02542D),
    suave = Color(0xFFCFF7D3),
    secundario = Color(0xFFBEC69D),
    alerta = Color(0xFFF59E0B)
)

val DarkPalette = EcoPalette(
    fondo = Color(0xFF121410),
    superficie = Color(0xFF1E221A),
    cabecera = Color(0xFF2E3B1F),
    boton = Color(0xFF5C7339),
    verdeOscuro = Color(0xFFB5C98A),
    verdeClaro = Color(0xFFA8BA82),
    verdeFondo = Color(0xFF26301C),
    mensajeFondo = Color(0xFF232A1C),
    mensajeBorde = Color(0xFF4D5C36),
    gris = Color(0xFF9CA3AF),
    borde = Color(0xFF343A2D),
    texto = Color(0xFFECEFE6),
    accion = Color(0xFF2E7D4F),
    suave = Color(0xFF1F3A26),
    secundario = Color(0xFF4D5C36),
    alerta = Color(0xFFB86E00)
)

val LocalEcoPalette = staticCompositionLocalOf { LightPalette }

/** Se usan igual que antes (EcoColors.VerdeOscuro), pero ahora respetan el modo claro/oscuro. */
object EcoColors {
    val Fondo: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.fondo
    val Superficie: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.superficie
    val Cabecera: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.cabecera
    val Boton: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.boton
    val VerdeOscuro: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.verdeOscuro
    val VerdeClaro: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.verdeClaro
    val VerdeFondo: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.verdeFondo
    val MensajeFondo: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.mensajeFondo
    val MensajeBorde: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.mensajeBorde
    val Gris: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.gris
    val Borde: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.borde
    val Texto: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.texto
    val Accion: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.accion
    val Suave: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.suave
    val Secundario: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.secundario
    val Alerta: Color @Composable @ReadOnlyComposable get() = LocalEcoPalette.current.alerta
}