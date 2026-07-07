package br.com.isa.rotinaestudos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = IsaG2,
    onPrimary = Color.White,
    primaryContainer = IsaGbg,
    onPrimaryContainer = IsaG1,
    secondary = IsaG1,
    onSecondary = Color.White,
    tertiary = IsaCoin,
    onTertiary = Color.White,
    background = IsaBg,
    onBackground = IsaText,
    surface = IsaCard,
    onSurface = IsaText,
    surfaceVariant = IsaGbg,
    onSurfaceVariant = IsaSub,
    outline = IsaBorder,
    error = IsaRed
)

private val DarkColors = darkColorScheme(
    primary = IsaDarkAccent,
    onPrimary = Color.Black,
    primaryContainer = IsaDarkGbg,
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color.Black,
    tertiary = IsaCoin,
    onTertiary = Color.Black,
    background = IsaDarkBg,
    onBackground = IsaDarkText,
    surface = IsaDarkCard,
    onSurface = IsaDarkText,
    surfaceVariant = IsaDarkGbg,
    onSurfaceVariant = IsaDarkSub,
    outline = IsaDarkBorder,
    error = IsaRed
)

@Composable
fun IsaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    shopThemeId: String? = null,
    content: @Composable () -> Unit
) {
    val colors = resolveColorScheme(darkTheme, shopThemeId)
    MaterialTheme(
        colorScheme = colors,
        typography = IsaTypography,
        shapes = IsaShapes,
        content = content
    )
}

private fun resolveColorScheme(dark: Boolean, themeId: String?) = when (themeId) {
    "theme_ocean" -> if (dark) oceanDark() else oceanLight()
    "theme_sunset" -> if (dark) sunsetDark() else sunsetLight()
    "theme_galaxy" -> if (dark) galaxyDark() else galaxyLight()
    "theme_rose" -> if (dark) roseDark() else roseLight()
    "theme_midnight" -> darkColorScheme(
        primary = Color(0xFF5C6BC0),
        onPrimary = Color.White,
        background = Color(0xFF0A0A12),
        surface = Color(0xFF14141F),
        onBackground = Color(0xFFE0E0E0),
        onSurface = Color(0xFFE0E0E0)
    )
    else -> if (dark) DarkColors else LightColors
}

private fun oceanLight() = lightColorScheme(primary = Color(0xFF1565C0), secondary = Color(0xFF0277BD), background = Color(0xFFE3F2FD), surface = Color.White)
private fun oceanDark() = darkColorScheme(primary = Color(0xFF42A5F5), secondary = Color(0xFF29B6F6), background = Color(0xFF0D1B2A), surface = Color(0xFF1B2838))
private fun sunsetLight() = lightColorScheme(primary = Color(0xFFE65100), secondary = Color(0xFFFF7043), background = Color(0xFFFFF3E0), surface = Color.White)
private fun sunsetDark() = darkColorScheme(primary = Color(0xFFFF7043), secondary = Color(0xFFFFAB40), background = Color(0xFF1A0F00), surface = Color(0xFF2A1810))
private fun galaxyLight() = lightColorScheme(primary = Color(0xFF6A1B9A), secondary = Color(0xFF8E24AA), background = Color(0xFFF3E5F5), surface = Color.White)
private fun galaxyDark() = darkColorScheme(primary = Color(0xFFAB47BC), secondary = Color(0xFFCE93D8), background = Color(0xFF120818), surface = Color(0xFF1E0F28))
private fun roseLight() = lightColorScheme(primary = Color(0xFFC2185B), secondary = Color(0xFFF06292), background = Color(0xFFFCE4EC), surface = Color.White)
private fun roseDark() = darkColorScheme(primary = Color(0xFFF06292), secondary = Color(0xFFF48FB1), background = Color(0xFF1A0810), surface = Color(0xFF2A1020))
