package uz.tikuvchi.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Web ilova faqat yorug' temada — Android'da ham shunday, aks holda ikki
// platformada ikki xil ko'rinish bo'lardi.
private val TikuvchiColors = lightColorScheme(
    primary = Terra600,
    onPrimary = Cream50,
    primaryContainer = Terra50,
    onPrimaryContainer = Terra700,
    secondary = Gold400,
    onSecondary = Ink900,
    secondaryContainer = Gold100,
    onSecondaryContainer = Ink900,
    tertiary = Sage500,
    onTertiary = Cream50,
    background = Cream50,
    onBackground = Ink900,
    surface = Cream50,
    onSurface = Ink900,
    surfaceVariant = Cream100,
    onSurfaceVariant = Ink500,
    outline = Cream200,
    outlineVariant = Cream300,
    error = Red700,
    onError = Cream50,
    errorContainer = Red50,
    onErrorContainer = Red700,
)

@Composable
fun TikuvchiTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            // Fon doim och rangda — tizim temasi qorong'i bo'lsa ham status bar
            // ikonkalari qora bo'lishi kerak.
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }
    MaterialTheme(
        colorScheme = TikuvchiColors,
        typography = TikuvchiTypography,
        content = content,
    )
}
