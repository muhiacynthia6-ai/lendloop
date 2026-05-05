package com.example.lendloop.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary              = Amethyst500,
    onPrimary            = PureWhite,
    primaryContainer     = Amethyst100,
    onPrimaryContainer   = Amethyst800,

    secondary            = Violet500,
    onSecondary          = PureWhite,
    secondaryContainer   = Violet100,
    onSecondaryContainer = Violet700,

    tertiary             = Gold500,
    onTertiary           = Color(0xFF3A2800),
    tertiaryContainer    = Gold100,
    onTertiaryContainer  = Gold600,

    error                = ErrorRed,
    onError              = PureWhite,
    errorContainer       = ErrorLight,
    onErrorContainer     = Color(0xFF7A1B12),

    background           = Ivory100,
    onBackground         = PurpleGray800,
    surface              = Ivory200,
    onSurface            = PurpleGray800,
    surfaceVariant       = Ivory300,
    onSurfaceVariant     = PurpleGray600,

    outline              = PurpleGray300,
    outlineVariant       = PurpleGray200,

    inverseSurface       = PurpleGray800,
    inverseOnSurface     = Ivory200,
    inversePrimary       = Amethyst300,

    surfaceTint          = Amethyst500,
    scrim                = Color(0x991A0533),
)

private val DarkColorScheme = darkColorScheme(
    primary              = Amethyst300,
    onPrimary            = Amethyst900,
    primaryContainer     = Amethyst700,
    onPrimaryContainer   = Amethyst100,

    secondary            = Violet300,
    onSecondary          = Violet700,
    secondaryContainer   = Color(0xFF280D5C),
    onSecondaryContainer = Violet100,

    tertiary             = Gold400,
    onTertiary           = Color(0xFF1F1200),
    tertiaryContainer    = Color(0xFF3B2500),
    onTertiaryContainer  = Gold200,

    error                = Color(0xFFE88A83),
    onError              = Color(0xFF5C1410),
    errorContainer       = Color(0xFF7A1B12),
    onErrorContainer     = Color(0xFFFCBBB6),

    background           = Color(0xFF120B1E),
    onBackground         = Amethyst100,
    surface              = Color(0xFF1C1228),
    onSurface            = PurpleGray100,
    surfaceVariant       = Color(0xFF2A1F3D),
    onSurfaceVariant     = PurpleGray300,

    outline              = PurpleGray600,
    outlineVariant       = Color(0xFF3D3352),

    inverseSurface       = Amethyst100,
    inverseOnSurface     = Amethyst800,
    inversePrimary       = Amethyst500,

    surfaceTint          = Amethyst300,
    scrim                = Color(0xCC000000),
)

@Composable
fun LendLoopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = LendLoopTypography,
        shapes      = LendLoopShapes,
        content     = content,
    )
}