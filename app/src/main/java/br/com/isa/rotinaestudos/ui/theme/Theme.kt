package br.com.isa.rotinaestudos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = IsaGreen,
    onPrimary = Color.White,
    primaryContainer = IsaBg,
    secondary = IsaGreenDark,
    background = Color(0xFFF4F6F8),
    surface = IsaCard,
    onBackground = IsaText,
    onSurface = IsaText
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF66BB6A),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0D1F0D),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF0F1117),
    surface = Color(0xFF1A1D27),
    onBackground = Color(0xFFE8EAF0),
    onSurface = Color(0xFFE8EAF0)
)

@Composable
fun IsaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
