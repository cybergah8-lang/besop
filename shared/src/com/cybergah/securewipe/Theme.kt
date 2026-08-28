package com.cybergah.securewipe

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Kullanicinin sectigi tema. */
enum class ThemeMode(val code: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    /** Dongu: sistem -> aydinlik -> karanlik -> sistem */
    fun next(): ThemeMode = when (this) {
        SYSTEM -> LIGHT
        LIGHT -> DARK
        DARK -> SYSTEM
    }

    /** Arayuzde gosterilen isaret. */
    val glyph: String
        get() = when (this) {
            SYSTEM -> "◐"
            LIGHT -> "☀"
            DARK -> "☾"
        }

    companion object {
        fun fromCode(code: String?): ThemeMode =
            entries.firstOrNull { it.code == code } ?: SYSTEM
    }
}

// ---------------------------------------------------------------- paletler

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF4757),          // canli kirmizi - ana vurgu
    onPrimary = Color(0xFF2A0509),
    primaryContainer = Color(0xFF3D1218),
    onPrimaryContainer = Color(0xFFFFD9DC),

    secondary = Color(0xFFFFB020),        // kehribar - ikincil vurgu
    onSecondary = Color(0xFF2A1A00),

    tertiary = Color(0xFF8B93FF),
    onTertiary = Color(0xFF11123A),

    background = Color(0xFF0A0A0C),       // neredeyse siyah, hafif mavi
    onBackground = Color(0xFFECEAEE),

    surface = Color(0xFF141419),
    onSurface = Color(0xFFECEAEE),
    surfaceVariant = Color(0xFF1E1E25),
    onSurfaceVariant = Color(0xFFA8A5AE),

    outline = Color(0xFF2E2E36),
    outlineVariant = Color(0xFF23232B),

    error = Color(0xFFFF4757),
    onError = Color(0xFF2A0509)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFD92D3A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDDDF),
    onPrimaryContainer = Color(0xFF410008),

    secondary = Color(0xFFB26B00),
    onSecondary = Color(0xFFFFFFFF),

    tertiary = Color(0xFF4B52C7),
    onTertiary = Color(0xFFFFFFFF),

    background = Color(0xFFFBFAFB),
    onBackground = Color(0xFF16151A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16151A),
    surfaceVariant = Color(0xFFF2F0F3),
    onSurfaceVariant = Color(0xFF5C5964),

    outline = Color(0xFFE0DDE4),
    outlineVariant = Color(0xFFEDEAF0),

    error = Color(0xFFD92D3A),
    onError = Color(0xFFFFFFFF)
)

// ---------------------------------------------------------------- yazi tipi olcegi

private val AppTypography = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        )
    )
}

@Composable
fun AppTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}

/**
 * Arka plan: duz renk yerine merkezden yayilan yumusak bir isik.
 * Ekrana derinlik katar, logonun cevresinde hafif bir hale birakir.
 */
@Composable
fun GlowBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bg = MaterialTheme.colorScheme.background
    val accent = MaterialTheme.colorScheme.primary
    // Surface yerine duz Box kullaninca LocalContentColor saglanmiyor ve
    // rengi belirtilmemis metinler siyaha dusuyordu - burada acikca veriyoruz.
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Box(
            modifier
                .fillMaxSize()
                .background(bg)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.07f), Color.Transparent),
                        center = Offset.Unspecified,
                        radius = 900f
                    )
                )
        ) { content() }
    }
}
