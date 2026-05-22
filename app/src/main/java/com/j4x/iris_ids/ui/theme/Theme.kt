package com.j4x.iris_ids.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val IrisColorScheme = lightColorScheme(
    primary = IrisPrimary,
    onPrimary = IrisSurface,
    primaryContainer = IrisPrimarySoft,
    onPrimaryContainer = IrisPrimary,
    secondary = IrisSalida,
    onSecondary = IrisSurface,
    secondaryContainer = IrisSalidaSoft,
    onSecondaryContainer = IrisSalida,
    background = IrisBg,
    onBackground = IrisInk,
    surface = IrisSurface,
    onSurface = IrisInk,
    surfaceVariant = IrisPrimarySoft,
    onSurfaceVariant = IrisInkSoft,
    error = IrisDanger,
    onError = IrisSurface,
    errorContainer = IrisDangerSoft,
    outline = IrisLine,
    outlineVariant = IrisLine,
)

// Tokens de diseño extra accesibles desde cualquier Composable
data class IrisExtendedColors(
    val inkFaint: Color,
    val done: Color,
    val doneSoft: Color,
    val dangerSoft: Color,
    val cameraBg: Color,
    val scanMatch: Color,
)

val LocalIrisExtended = staticCompositionLocalOf {
    IrisExtendedColors(
        inkFaint = IrisInkFaint,
        done = IrisDone,
        doneSoft = IrisDoneSoft,
        dangerSoft = IrisDangerSoft,
        cameraBg = IrisCameraBg,
        scanMatch = IrisScanMatch,
    )
}

@Composable
fun IrisTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalIrisExtended provides LocalIrisExtended.current) {
        MaterialTheme(
            colorScheme = IrisColorScheme,
            typography = IrisTypography,
            content = content
        )
    }
}

// Accessor de conveniencia: MaterialTheme.irisColors.done
val MaterialTheme.irisColors: IrisExtendedColors
    @Composable get() = LocalIrisExtended.current
