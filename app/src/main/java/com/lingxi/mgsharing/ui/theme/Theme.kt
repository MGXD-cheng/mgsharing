package com.lingxi.mgsharing.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** 浅色配色 —— Teal 主色 + Amber 辅色 */
private val LightColorScheme = lightColorScheme(
    primary = Teal500,
    onPrimary = NeutralSurface,
    primaryContainer = Teal50,
    onPrimaryContainer = Teal800,
    secondary = Amber500,
    onSecondary = NeutralSurface,
    secondaryContainer = Amber300,
    onSecondaryContainer = Teal800,
    background = NeutralBg,
    onBackground = TextOnLight,
    surface = NeutralSurface,
    onSurface = TextOnLight,
    surfaceVariant = Teal50,
    onSurfaceVariant = Teal700,
    outline = Teal300,
    error = ErrorRed
)

/** 深色配色 */
private val DarkColorScheme = darkColorScheme(
    primary = TealDarkPrimary,
    onPrimary = TealDarkOnPrimary,
    primaryContainer = TealDarkContainer,
    onPrimaryContainer = TealDarkOnContainer,
    secondary = Amber300,
    onSecondary = Teal800,
    secondaryContainer = Amber700,
    onSecondaryContainer = Amber300,
    background = NeutralBgDark,
    onBackground = TextOnDark,
    surface = NeutralSurfaceDark,
    onSurface = TextOnDark,
    surfaceVariant = TealDarkContainer,
    onSurfaceVariant = Teal100,
    outline = Teal300,
    error = ErrorRedDark
)

/**
 * MGsharing 主题 —— 支持 Material You 动态取色与深浅色切换
 */
@Composable
fun MGsharingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}