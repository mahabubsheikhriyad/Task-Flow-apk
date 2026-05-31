package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = IndigoPrimary,
    secondary = IndigoSecondary,
    tertiary = VioletTertiary,
    background = ImmersiveBg,
    surface = ImmersiveBg,
    onBackground = Slate100,
    onSurface = Slate100
  )

private val LightColorScheme = DarkColorScheme // Standard immersive dark look for both

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for the immersive vibe
  dynamicColor: Boolean = false, // Always use our custom visual theme
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
