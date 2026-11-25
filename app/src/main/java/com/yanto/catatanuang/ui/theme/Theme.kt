package com.yanto.catatanuang.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// OLED Dark Mode Elegan palette:
// 60% Dominant: #09090B (Deep Obsidian)
// 30% Secondary: #18181B (Dark Zinc)
// 10% Accent: #8B5CF6 (Electric Violet) & #10B981 (Mint Neon)
// Text: #F4F4F5 (Off-White)
val BrandBlue = Color(0xFF8B5CF6)       // Electric Violet (primary & Duo)
val BrandBlueDeep = Color(0xFF4C1D95)
val BrandBlueSoft = Color(0xFF2E1065)

val BrandOrange = Color(0xFF10B981)     // Mint Neon (CTA & Solo)
val BrandOrangeDeep = Color(0xFF065F46)
val BrandOrangeBright = Color(0xFF34D399)
val BrandOrangeSoft = Color(0xFF064E3B)

val BrandRed = Color(0xFFF43F5E)        // Rose (Tarik / destructive)
val BrandRedSoft = Color(0xFF4C0519)

val SurfaceWhite = Color(0xFF18181B)    // Dark Zinc (30% card surfaces)
val CanvasWhite = Color(0xFF09090B)     // Deep Obsidian (60% main background)
val InkPrimary = Color(0xFFF4F4F5)      // Off-White text
val InkSecondary = Color(0xFFD4D4D8)
val InkMuted = Color(0xFFA1A1AA)
val BorderSubtle = Color(0xFF27272A)

private val TabunganColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = InkPrimary,
    secondary = BrandOrange,
    onSecondary = Color(0xFF052E16),
    error = BrandRed,
    onError = InkPrimary,
    background = CanvasWhite,
    onBackground = InkPrimary,
    surface = SurfaceWhite,
    onSurface = InkPrimary,
    surfaceVariant = Color(0xFF27272A),
    onSurfaceVariant = InkSecondary,
    outline = BorderSubtle
)

private val TabunganTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
)

@Composable
fun TabunganTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TabunganColorScheme,
        typography = TabunganTypography,
        content = content
    )
}
