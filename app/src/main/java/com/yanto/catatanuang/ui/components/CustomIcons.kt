package com.yanto.catatanuang.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandOrange

@Composable
fun TwinCoinLogo(modifier: Modifier = Modifier, size: Dp = 28.dp) {
    Canvas(modifier = modifier.size(size)) {
        val r = this.size.minDimension * 0.30f
        val stroke = Stroke(width = this.size.minDimension * 0.11f)
        drawCircle(
            color = BrandBlue,
            radius = r,
            center = Offset(this.size.width * 0.36f, this.size.height * 0.52f),
            style = stroke
        )
        drawCircle(
            color = BrandOrange,
            radius = r,
            center = Offset(this.size.width * 0.64f, this.size.height * 0.48f),
            style = stroke
        )
    }
}

@Composable
fun DepositVaultIcon(tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.10f
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Arrow pointing down into vault tray
        drawLine(tint, Offset(w * 0.5f, h * 0.15f), Offset(w * 0.5f, h * 0.62f), strokeWidth = sw, cap = StrokeCap.Round)
        val head = Path().apply {
            moveTo(w * 0.30f, h * 0.44f)
            lineTo(w * 0.50f, h * 0.64f)
            lineTo(w * 0.70f, h * 0.44f)
        }
        drawPath(head, tint, style = stroke)
        val tray = Path().apply {
            moveTo(w * 0.18f, h * 0.68f)
            lineTo(w * 0.18f, h * 0.84f)
            lineTo(w * 0.82f, h * 0.84f)
            lineTo(w * 0.82f, h * 0.68f)
        }
        drawPath(tray, tint, style = stroke)
    }
}

@Composable
fun WithdrawVaultIcon(tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.10f
        val stroke = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(tint, Offset(w * 0.5f, h * 0.64f), Offset(w * 0.5f, h * 0.16f), strokeWidth = sw, cap = StrokeCap.Round)
        val head = Path().apply {
            moveTo(w * 0.30f, h * 0.34f)
            lineTo(w * 0.50f, h * 0.14f)
            lineTo(w * 0.70f, h * 0.34f)
        }
        drawPath(head, tint, style = stroke)
        val tray = Path().apply {
            moveTo(w * 0.18f, h * 0.68f)
            lineTo(w * 0.18f, h * 0.84f)
            lineTo(w * 0.82f, h * 0.84f)
            lineTo(w * 0.82f, h * 0.68f)
        }
        drawPath(tray, tint, style = stroke)
    }
}

@Composable
fun TargetFlagIcon(tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.09f
        val center = Offset(w * 0.48f, h * 0.56f)
        drawCircle(tint, radius = w * 0.34f, center = center, style = Stroke(width = sw))
        drawCircle(tint, radius = w * 0.16f, center = center, style = Stroke(width = sw))
        drawLine(tint, center, Offset(w * 0.82f, h * 0.20f), strokeWidth = sw, cap = StrokeCap.Round)
        val flag = Path().apply {
            moveTo(w * 0.82f, h * 0.20f)
            lineTo(w * 0.82f, h * 0.36f)
            lineTo(w * 0.64f, h * 0.28f)
            close()
        }
        drawPath(flag, tint)
    }
}

@Composable
fun WaveTrendIcon(tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.095f
        val line = Path().apply {
            moveTo(w * 0.14f, h * 0.72f)
            lineTo(w * 0.38f, h * 0.46f)
            lineTo(w * 0.56f, h * 0.58f)
            lineTo(w * 0.86f, h * 0.24f)
        }
        drawPath(line, tint, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(tint, radius = w * 0.08f, center = Offset(w * 0.86f, h * 0.24f))
        drawLine(tint.copy(alpha = 0.45f), Offset(w * 0.14f, h * 0.84f), Offset(w * 0.86f, h * 0.84f), strokeWidth = sw * 0.8f, cap = StrokeCap.Round)
    }
}

@Composable
fun LedgerScrollIcon(tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.09f
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.18f, h * 0.14f),
            size = Size(w * 0.64f, h * 0.72f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = sw)
        )
        drawLine(tint, Offset(w * 0.32f, h * 0.36f), Offset(w * 0.68f, h * 0.36f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.32f, h * 0.52f), Offset(w * 0.68f, h * 0.52f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.32f, h * 0.68f), Offset(w * 0.54f, h * 0.68f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
fun CalendarNoteIcon(tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.09f
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.16f, h * 0.22f),
            size = Size(w * 0.68f, h * 0.62f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = sw)
        )
        drawLine(tint, Offset(w * 0.16f, h * 0.42f), Offset(w * 0.84f, h * 0.42f), strokeWidth = sw)
        drawLine(tint, Offset(w * 0.34f, h * 0.14f), Offset(w * 0.34f, h * 0.28f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.66f, h * 0.14f), Offset(w * 0.66f, h * 0.28f), strokeWidth = sw, cap = StrokeCap.Round)
        drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.38f, h * 0.60f))
        drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.62f, h * 0.60f))
    }
}

@Composable
fun NotePadIcon(tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.09f
        val doc = Path().apply {
            moveTo(w * 0.22f, h * 0.14f)
            lineTo(w * 0.64f, h * 0.14f)
            lineTo(w * 0.80f, h * 0.30f)
            lineTo(w * 0.80f, h * 0.86f)
            lineTo(w * 0.22f, h * 0.86f)
            close()
        }
        drawPath(doc, tint, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(tint, Offset(w * 0.34f, h * 0.44f), Offset(w * 0.66f, h * 0.44f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.34f, h * 0.60f), Offset(w * 0.66f, h * 0.60f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.34f, h * 0.74f), Offset(w * 0.52f, h * 0.74f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
fun HomeRoofIcon(tint: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.095f
        val roof = Path().apply {
            moveTo(w * 0.14f, h * 0.48f)
            lineTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.86f, h * 0.48f)
        }
        drawPath(roof, tint, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val body = Path().apply {
            moveTo(w * 0.25f, h * 0.42f)
            lineTo(w * 0.25f, h * 0.82f)
            lineTo(w * 0.75f, h * 0.82f)
            lineTo(w * 0.75f, h * 0.42f)
        }
        drawPath(body, tint, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(tint, radius = w * 0.07f, center = Offset(w * 0.50f, h * 0.60f))
    }
}

@Composable
fun PrivacyEyeIcon(hidden: Boolean, tint: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.09f
        val eye = Path().apply {
            moveTo(w * 0.10f, h * 0.50f)
            quadraticTo(w * 0.50f, h * 0.16f, w * 0.90f, h * 0.50f)
            quadraticTo(w * 0.50f, h * 0.84f, w * 0.10f, h * 0.50f)
        }
        drawPath(eye, tint, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(tint, radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.50f))
        if (hidden) {
            drawLine(tint, Offset(w * 0.20f, h * 0.80f), Offset(w * 0.80f, h * 0.20f), strokeWidth = sw * 1.1f, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun ShieldLockIcon(tint: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.095f
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.22f, h * 0.44f),
            size = Size(w * 0.56f, h * 0.42f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
            style = Stroke(width = sw)
        )
        val shackle = Path().apply {
            moveTo(w * 0.34f, h * 0.44f)
            lineTo(w * 0.34f, h * 0.30f)
            quadraticTo(w * 0.50f, h * 0.14f, w * 0.66f, h * 0.30f)
            lineTo(w * 0.66f, h * 0.44f)
        }
        drawPath(shackle, tint, style = Stroke(width = sw, cap = StrokeCap.Round))
    }
}

@Composable
fun PartnerGemIcon(partnerId: Int, tint: Color, modifier: Modifier = Modifier, size: Dp = 14.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        if (partnerId == 2) {
            val diamond = Path().apply {
                moveTo(w * 0.50f, h * 0.10f)
                lineTo(w * 0.90f, h * 0.50f)
                lineTo(w * 0.50f, h * 0.90f)
                lineTo(w * 0.10f, h * 0.50f)
                close()
            }
            drawPath(diamond, tint)
        } else {
            drawCircle(tint, radius = w * 0.36f, center = Offset(w * 0.50f, h * 0.50f))
        }
    }
}
