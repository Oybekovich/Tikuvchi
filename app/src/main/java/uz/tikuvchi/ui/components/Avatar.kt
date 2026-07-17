package uz.tikuvchi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

/** Web'dagi Avatar bilan bir xil ranglar — bir xil ism ikkala platformada bir xil rangda. */
private val FallbackColors = listOf(
    Color(0xFFC0674A), Color(0xFF7B4B94), Color(0xFF2F5D50), Color(0xFFB08D3D),
    Color(0xFF5B6B84), Color(0xFFA05A44), Color(0xFF48695E), Color(0xFF8A5A83),
)

enum class AvatarSize(val dp: Dp, val fontSize: TextUnit) {
    SM(32.dp, 12.sp),
    MD(40.dp, 14.sp),
    LG(56.dp, 18.sp),
    XL(80.dp, 24.sp),
}

/**
 * Web'dagi colorFor bilan aynan bir xil hisob: 31 asosli hash, 32-bitga kesilgan
 * (JS'dagi `| 0` — Kotlin'da Int shundoq ishlaydi). Manfiy chegara qiymatida
 * abs() qaytib manfiy bo'lmasligi uchun Long orqali olinadi.
 */
private fun colorFor(name: String): Color {
    var hash = 0
    for (c in name) hash = hash * 31 + c.code
    val index = kotlin.math.abs(hash.toLong()) % FallbackColors.size
    return FallbackColors[index.toInt()]
}

/**
 * Rasm bo'lmasa bo'sh blok emas, ismning bosh harfi va ismdan hosil qilingan
 * rangli fon ko'rsatiladi.
 */
@Composable
fun Avatar(
    name: String,
    src: String? = null,
    size: AvatarSize = AvatarSize.MD,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    if (src != null) {
        AsyncImage(
            model = src,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size.dp).clip(shape),
        )
        return
    }

    Box(
        modifier.size(size.dp).clip(shape).background(colorFor(name)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.trim().take(1).uppercase().ifEmpty { "•" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = size.fontSize,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
