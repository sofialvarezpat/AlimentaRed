package com.example.ecoabastos.ui.screens.login

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.components.BrandHeader
import com.example.ecoabastos.ui.components.EcoContextMessage
import com.example.ecoabastos.ui.components.EcoPrimaryButton
import com.example.ecoabastos.ui.components.EcoTextField
import com.example.ecoabastos.ui.components.PasswordToggle
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Pantalla de inicio de sesión (solo diseño).
 * No habla con el backend: avisa lo que pasa mediante las funciones que recibe.
 */
@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        BrandHeader(height = 257.dp, logoTop = 63.dp)

        Column(
            modifier = Modifier.padding(start = 46.dp, end = 33.dp, top = 103.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "¡Bienvenido de nuevo!",
                color = EcoColors.VerdeOscuro,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Inicia sesión a tu cuenta",
                color = EcoColors.VerdeClaro,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            EcoTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Correo",
                placeholderSize = 12.sp,
                keyboardType = KeyboardType.Email
            )

            EcoTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                placeholderSize = 12.sp,
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

            // Solo aparece cuando hay un error (estado "Error" del diseño)
            if (errorMessage != null) {
                EcoContextMessage(
                    title = "No pudimos iniciar sesión",
                    message = errorMessage
                )
            }

            Text(
                text = "Olvidó su contraseña",
                fontSize = 11.sp,
                color = EcoColors.Gris,
                modifier = Modifier.clickable(onClick = onForgotPasswordClick)
            )

            EcoPrimaryButton(
                text = "Iniciar Sesión",
                onClick = { onLoginClick(email.trim(), password) },
                enabled = email.isNotBlank() && password.isNotBlank(),
                isLoading = isLoading
            )

            Text(
                text = "No tiene cuenta, crea una",
                fontSize = 11.sp,
                color = EcoColors.Gris,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRegisterClick)
            )
        }
    }
}

// ---------- Vistas previas ----------

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Vacío")
@Composable
private fun LoginVacioPreview() {
    EcoTheme(darkTheme = false) {
        LoginScreen(
            onLoginClick = { _, _ -> },
            onForgotPasswordClick = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Error")
@Composable
private fun LoginErrorPreview() {
    EcoTheme(darkTheme = false) {
        LoginScreen(
            onLoginClick = { _, _ -> },
            onForgotPasswordClick = {},
            onRegisterClick = {},
            errorMessage = "Revisa tu correo o usuario y tu contraseña."
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Oscuro")
@Composable
private fun LoginOscuroPreview() {
    EcoTheme(darkTheme = true) {
        LoginScreen(
            onLoginClick = { _, _ -> },
            onForgotPasswordClick = {},
            onRegisterClick = {}
        )
    }
}