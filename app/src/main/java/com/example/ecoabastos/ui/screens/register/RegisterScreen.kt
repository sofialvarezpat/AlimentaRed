package com.example.ecoabastos.ui.screens.register


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.data.model.Rol
import com.example.ecoabastos.ui.components.BrandHeader
import com.example.ecoabastos.ui.components.EcoContextMessage
import com.example.ecoabastos.ui.components.EcoPrimaryButton
import com.example.ecoabastos.ui.components.EcoTextField
import com.example.ecoabastos.ui.components.PasswordToggle
import com.example.ecoabastos.ui.components.RolSelector
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Pantalla de crear cuenta (solo diseño).
 * No habla con el backend: avisa lo que pasa mediante las funciones que recibe.
 */
@Composable
fun RegisterScreen(
    onRegisterClick: (rol: Rol, nombre: String, email: String, direccion: String, password: String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var rol by remember { mutableStateOf(Rol.DONANTE) }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // El backend exige mínimo 8 caracteres en la contraseña
    val formularioValido = nombre.isNotBlank() &&
            email.isNotBlank() &&
            direccion.isNotBlank() &&
            password.length >= 8

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        BrandHeader(height = 218.dp, logoTop = 24.dp)

        Column(
            modifier = Modifier.padding(start = 40.dp, end = 39.dp, top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Título y subtítulo
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Crea tu cuenta",
                    color = EcoColors.VerdeOscuro,
                    fontSize = 36.sp,
                    lineHeight = 44.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Únete a Eco Abasto",
                    color = EcoColors.VerdeClaro,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Donador u Organización receptora
            RolSelector(selected = rol, onSelect = { rol = it })

            // Datos de registro
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                EcoTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = "Nombre completo"
                )
                EcoTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Correo electrónico",
                    keyboardType = KeyboardType.Email
                )
                EcoTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    placeholder = "Dirección"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EcoTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Contraseña",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            PasswordToggle(visible = passwordVisible) {
                                passwordVisible = !passwordVisible
                            }
                        }
                    )
                    Text(
                        text = "Usa al menos 8 caracteres.",
                        color = EcoColors.Gris,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Solo aparece si hay error. (El diseño no trae variante de error para el
            // registro, así que usa el mismo recuadro del login.)
            if (errorMessage != null) {
                EcoContextMessage(
                    title = "No pudimos crear tu cuenta",
                    message = errorMessage
                )
            }

            // Botón y enlace al login
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                EcoPrimaryButton(
                    text = "Crear cuenta",
                    onClick = {
                        onRegisterClick(
                            rol,
                            nombre.trim(),
                            email.trim(),
                            direccion.trim(),
                            password
                        )
                    },
                    enabled = formularioValido,
                    isLoading = isLoading
                )
                Text(
                    text = buildAnnotatedString {
                        append("¿Ya tienes cuenta? ")
                        withStyle(
                            SpanStyle(
                                color = EcoColors.VerdeOscuro,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("Inicia sesión")
                        }
                    },
                    color = EcoColors.Gris,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLoginClick)
                )
            }
        }
    }
}

// ---------- Vistas previas ----------

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Vacío")
@Composable
private fun RegisterVacioPreview() {
    EcoTheme(darkTheme = false) {
        RegisterScreen(
            onRegisterClick = { _, _, _, _, _ -> },
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Error")
@Composable
private fun RegisterErrorPreview() {
    EcoTheme(darkTheme = false) {
        RegisterScreen(
            onRegisterClick = { _, _, _, _, _ -> },
            onLoginClick = {},
            errorMessage = "Revisa los datos e inténtalo de nuevo."
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Oscuro")
@Composable
private fun RegisterOscuroPreview() {
    EcoTheme(darkTheme = true) {
        RegisterScreen(
            onRegisterClick = { _, _, _, _, _ -> },
            onLoginClick = {}
        )
    }
}