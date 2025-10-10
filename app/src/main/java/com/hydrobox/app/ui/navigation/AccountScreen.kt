package com.hydrobox.app.ui.navigation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AccountScreen(
    user: AdminUser,
    onSave: (updated: AdminUser, photo: Uri?, username: String, phonePrefix: String, phone: String) -> Unit = { _, _, _, _, _ -> }
) {
    // Foto
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri }

    // Estados de UI
    var name by remember { mutableStateOf("${user.name} ${user.lastName}".trim()) }
    var email by remember { mutableStateOf(user.email) }
    var username by remember { mutableStateOf("") }
    var phonePrefix by remember { mutableStateOf("+52") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Validaciones
    val emailOk = remember(email) { email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val passOk = remember(password) { password.isEmpty() || password.length >= 8 }
    val phoneOk = remember(phone) { phone.isEmpty() || phone.all(Char::isDigit) }

    val hasChanges = remember(name, email, username, phonePrefix, phone, password, photoUri) {
        name.isNotBlank() &&
                (name != "${user.name} ${user.lastName}".trim() ||
                        email != user.email ||
                        username.isNotBlank() ||
                        phonePrefix != "+52" ||
                        phone.isNotBlank() ||
                        password.isNotEmpty() ||
                        photoUri != null)
    }

    val tfColors = TextFieldDefaults.colors(
        focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
        disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
        focusedIndicatorColor   = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor  = Color.Transparent,
        cursorColor             = MaterialTheme.colorScheme.primary
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar + botón cámara
        Box(contentAlignment = Alignment.BottomEnd) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Foto de perfil",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            }
            FilledTonalIconButton(
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.offset(x = 4.dp, y = 4.dp).size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
            ) { Icon(Icons.Outlined.CameraAlt, contentDescription = "Cambiar foto") }
        }

        // Campos
        LabeledField("Name") {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Charlotte King") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )
        }

        LabeledField("E-mail address") {
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("defaultemail@gmail.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = !emailOk,
                supportingText = { if (!emailOk) Text("Email no válido", color = MaterialTheme.colorScheme.error) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )
        }

        LabeledField("User name") {
            TextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("@defaultamerandom") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )
        }

        LabeledField("Password") {
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("********") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (showPassword) "Ocultar" else "Mostrar"
                        )
                    }
                },
                isError = !passOk,
                supportingText = { if (!passOk) Text("Mínimo 8 caracteres", color = MaterialTheme.colorScheme.error) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )
        }

        LabeledField("Phone number") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = phonePrefix,
                    onValueChange = { if (it.length <= 4) phonePrefix = it },
                    singleLine = true,
                    modifier = Modifier.width(90.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = tfColors
                )
                TextField(
                    value = phone,
                    onValueChange = { phone = it.filter(Char::isDigit) },
                    placeholder = { Text("6895312") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = !phoneOk,
                    supportingText = { if (!phoneOk) Text("Solo dígitos", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = tfColors
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val parts = name.trim().split(" ", limit = 2)
                val updated = user.copy(
                    name = parts.getOrNull(0).orEmpty(),
                    lastName = parts.getOrNull(1).orEmpty(),
                    email = email
                )
                onSave(updated, photoUri, username, phonePrefix, phone)
            },
            enabled = hasChanges && emailOk && passOk && phoneOk,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("Guardar cambios") }

        Text("Rol: ${user.role}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        content()
    }
}