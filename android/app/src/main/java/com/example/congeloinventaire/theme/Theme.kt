package com.example.congeloinventaire.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF82D3EE),
    onPrimary = Color(0xFF003542),
    primaryContainer = Color(0xFF004E62),
    onPrimaryContainer = Color(0xFFBCEBFA),
    secondary = Color(0xFF89D5C4),
    onSecondary = Color(0xFF06372E),
    secondaryContainer = Color(0xFF255044),
    onSecondaryContainer = Color(0xFFA5F2DF),
    tertiary = Color(0xFFFFB1BE),
    onTertiary = Color(0xFF650022),
    tertiaryContainer = Color(0xFF8C1539),
    onTertiaryContainer = Color(0xFFFFD9DF),
    background = Color(0xFF0D1416),
    onBackground = Color(0xFFDEE4E6),
    surface = Color(0xFF11191B),
    onSurface = Color(0xFFDEE4E6),
    surfaceVariant = Color(0xFF263236),
    onSurfaceVariant = Color(0xFFBAC8CC),
    outline = Color(0xFF84969B),
    outlineVariant = Color(0xFF3B494D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = GlacierBlue,
    onPrimary = Color.White,
    primaryContainer = GlacierBlueLight,
    onPrimaryContainer = Color(0xFF0C3542),
    secondary = FreezerTeal,
    onSecondary = Color.White,
    secondaryContainer = FreezerTealLight,
    onSecondaryContainer = Color(0xFF143B33),
    tertiary = RecipeCoral,
    onTertiary = Color.White,
    tertiaryContainer = RecipeCoralLight,
    onTertiaryContainer = Color(0xFF571321),
    background = IceBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = IceSurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = Color(0xFF70858B),
    outlineVariant = IceOutline,
    error = OldRed,
    onError = Color.White,
    errorContainer = OldRedContainer,
    onErrorContainer = Color(0xFF410002)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun CongeloInventaireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
