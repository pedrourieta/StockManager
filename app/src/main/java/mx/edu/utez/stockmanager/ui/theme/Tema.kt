package mx.edu.utez.stockmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun StockManagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF193052),
            background = Color.White,
            surface = Color.White,
            surfaceContainer = Color.White,
            surfaceContainerHighest = Color.White,
            surfaceVariant = Color.White,
            secondaryContainer = Color(0xFFE0E0E0)
        ),
        content = content
    )
}
