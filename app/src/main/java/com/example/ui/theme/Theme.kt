package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Crisp, readable Light Theme (Default)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF), // Blue 50
    onPrimaryContainer = Color(0xFF1D4ED8), // Blue 700
    secondary = Color(0xFF1E293B), // Slate 800
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = SafeGreen,
    onTertiary = Color.White,
    tertiaryContainer = SafeContainer,
    onTertiaryContainer = OnSafeContainer,
    background = Color(0xFFF8FAFC), // Slate 50 - clean light canvas
    onBackground = Color(0xFF0F172A), // Slate 900 - high contrast text
    surface = Color(0xFFFFFFFF), // Pure white cards & app bars
    onSurface = Color(0xFF0F172A), // Slate 900
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100 - soft card container
    onSurfaceVariant = Color(0xFF334155), // Slate 700
    outline = Color(0xFFCBD5E1), // Slate 300
    outlineVariant = Color(0xFFE2E8F0), // Slate 200
    error = EmergencyRed,
    onError = Color.White,
    errorContainer = EmergencyContainer,
    onErrorContainer = OnEmergencyContainer
)

// Dark Theme (Optional)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = Color.White,
    secondary = PrimaryBlue,
    onSecondary = Color.White,
    tertiary = SafeGreenLight,
    onTertiary = Color.White,
    background = NavyDark,
    onBackground = Color.White,
    surface = NavySurface,
    onSurface = Color.White,
    surfaceVariant = NavyElevated,
    onSurfaceVariant = Slate300,
    outline = NavyBorder,
    error = EmergencyRedLight,
    onError = Color.White,
    errorContainer = EmergencyContainer,
    onErrorContainer = OnEmergencyContainer
)

@Composable
fun PocketGuardianTheme(
    darkTheme: Boolean = false, // Defaults strictly to crisp Light Theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
