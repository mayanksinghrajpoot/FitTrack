package com.example.fittrack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CrimsonRed,
    onPrimary = PureWhite,
    primaryContainer = CrimsonRedLight,
    onPrimaryContainer = CrimsonRedDark,
    secondary = CrimsonRedDark,
    onSecondary = PureWhite,
    background = SurfaceWhite,
    onBackground = CharcoalDark,
    surface = PureWhite,
    onSurface = CharcoalDark,
    surfaceVariant = SurfaceWhite,
    onSurfaceVariant = MutedSlate,
    outline = CardBorderColor
)

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonRedAccent,
    onPrimary = PureWhite,
    primaryContainer = CrimsonRedDark,
    onPrimaryContainer = PureWhite,
    secondary = CrimsonRed,
    onSecondary = PureWhite,
    background = CharcoalDark,
    onBackground = PureWhite,
    surface = CharcoalMedium,
    onSurface = PureWhite,
    surfaceVariant = CharcoalDark,
    onSurfaceVariant = LightSlate,
    outline = CharcoalMedium
)

@Composable
fun FitTrackTheme(
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
        typography = Typography,
        content = content
    )
}
