package com.example.ecoabastos.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme
import java.util.Calendar

/**
 * Campo que abre el selector de fecha del sistema.
 * value viene en formato "yyyy-MM-dd" (vacío si no se ha elegido).
 */
@Composable
fun EcoDateField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    EcoPickerField(
        value = value,
        placeholder = "yyyy/mm/dd",
        modifier = modifier,
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val mm = (month + 1).toString().padStart(2, '0')
                    val dd = day.toString().padStart(2, '0')
                    onValueChange("$year-$mm-$dd")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    )
}

/**
 * Campo que abre el selector de hora del sistema.
 * value viene en formato 12h, ej: "06:30 PM" (vacío si no se ha elegido).
 */
@Composable
fun EcoTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    EcoPickerField(
        value = value,
        placeholder = "HH:mm",
        modifier = modifier,
        onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val periodo = if (hour < 12) "AM" else "PM"
                    val hora12 = when {
                        hour == 0 -> 12
                        hour > 12 -> hour - 12
                        else -> hour
                    }
                    val hh = hora12.toString().padStart(2, '0')
                    val mm = minute.toString().padStart(2, '0')
                    onValueChange("$hh:$mm $periodo")
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }
    )
}

@Composable
private fun EcoPickerField(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .height(53.dp)
            .shadow(elevation = 3.dp, shape = shape)
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = value.ifEmpty { placeholder },
            fontSize = 13.sp,
            fontStyle = if (value.isEmpty()) FontStyle.Italic else FontStyle.Normal,
            color = if (value.isEmpty()) EcoColors.Gris else EcoColors.Texto
        )
    }
}

@Composable
private fun PreviewContent() {
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .background(EcoColors.Fondo)
            .padding(24.dp)
    ) {
        Text(
            text = "Fecha y hora límite",
            fontSize = 13.sp,
            color = EcoColors.Gris,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EcoDateField(value = fecha, onValueChange = { fecha = it }, modifier = Modifier.weight(1f))
            EcoTimeField(value = hora, onValueChange = { hora = it }, modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoDateTimePreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoDateTimeDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}