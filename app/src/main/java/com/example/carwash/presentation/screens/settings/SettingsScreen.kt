package com.example.carwash.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carwash.domain.model.ThemePreference
import com.example.carwash.presentation.viewmodel.SettingsViewModel
import com.example.carwash.ui.theme.BackgroundDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.SurfaceCardDark
import com.example.carwash.ui.theme.OnSurfaceVariantDark

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile header
            ProfileHeader(
                fullName = uiState.userFullName,
                role = uiState.userRole,
                isLoading = uiState.isLoadingProfile
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Theme section
            SectionHeader(title = "Apariencia")
            ThemeSelector(
                currentTheme = uiState.themePreference,
                onThemeSelected = viewModel::setTheme
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign out
            SectionHeader(title = "Cuenta")
            Spacer(modifier = Modifier.height(8.dp))
            SignOutButton(onSignOut = viewModel::signOut)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileHeader(
    fullName: String,
    role: String,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(OrangePrimary.copy(alpha = 0.15f))
                .border(2.dp, OrangePrimary.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = OrangePrimary,
                    strokeWidth = 2.dp
                )
            } else {
                val initials = fullName
                    .split(" ")
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .joinToString("")

                if (initials.isNotEmpty()) {
                    Text(
                        text = initials,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = OrangePrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Text(
                text = "Cargando perfil...",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariantDark
            )
        } else {
            Text(
                text = fullName.ifEmpty { "Usuario" },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            if (role.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangePrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = role,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = OrangePrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            color = OnSurfaceVariantDark,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        ),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun ThemeSelector(
    currentTheme: ThemePreference,
    onThemeSelected: (ThemePreference) -> Unit
) {
    val options = listOf(
        Triple(ThemePreference.Dark, Icons.Default.DarkMode, "Oscuro"),
        Triple(ThemePreference.Light, Icons.Default.LightMode, "Claro"),
        Triple(ThemePreference.System, Icons.Default.PhoneAndroid, "Sistema")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { (preference, icon, label) ->
            val isSelected = currentTheme == preference
            ThemeOption(
                icon = icon,
                label = label,
                isSelected = isSelected,
                onClick = { onThemeSelected(preference) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) OrangePrimary.copy(alpha = 0.12f) else SurfaceCardDark
    val borderColor = if (isSelected) OrangePrimary else Color.Transparent
    val contentColor = if (isSelected) OrangePrimary else OnSurfaceVariantDark

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
private fun SignOutButton(onSignOut: () -> Unit) {
    Button(
        onClick = onSignOut,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2D1A1A),
            contentColor = Color(0xFFE53935)
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = "Cerrar Sesion",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Cerrar Sesion",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
