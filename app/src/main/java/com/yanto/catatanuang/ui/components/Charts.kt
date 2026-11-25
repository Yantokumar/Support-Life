package com.yanto.catatanuang.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.model.MonthlyPoint
import com.yanto.catatanuang.ui.theme.BorderSubtle
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandRed
import com.yanto.catatanuang.ui.theme.InkMuted
import kotlin.math.abs
import kotlin.math.max

private fun formatCompactRupiah(amount: Long, masked: Boolean): String {
    if (masked) return "•••"
    val absVal = abs(amount)
    val prefix = if (amount < 0) "-Rp" else "Rp"
    return when {
        absVal >= 1_000_000_000L -> "$prefix${String.format("%.1f", absVal / 1_000_000_000.0)}M"
        absVal >= 1_000_000L -> "$prefix${String.format("%.1f", absVal / 1_000_000.0)}jt"
        absVal >= 1_000L -> "$prefix${absVal / 1_000L}rb"
        else -> "$prefix$absVal"
    }
}

@Composable
fun BalanceAreaChart(
    points: List<MonthlyPoint>,
    masked: Boolean,
    modifier: Modifier = Modifier
) {
    var revealed by remember(points) { mutableStateOf(false) }
    LaunchedEffect(points) { revealed = true }
    val progress by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "area_reveal"
    )

    val maxVal = points.maxOfOrNull { max(it.cumulativeBalance, 0L) }?.coerceAtLeast(1L) ?: 1L
    val minVal = points.minOfOrNull { it.cumulativeBalance }?.coerceAtMost(0L) ?: 0L
    val span = (maxVal - minVal).coerceAtLeast(1L).toFloat()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .height(170.dp)
                    .width(52.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatCompactRupiah(maxVal, masked), color = InkMuted, fontSize = 10.sp)
                Text(formatCompactRupiah((maxVal + minVal) / 2L, masked), color = InkMuted, fontSize = 10.sp)
                Text(formatCompactRupiah(minVal, masked), color = InkMuted, fontSize = 10.sp)
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(170.dp)
            ) {
                val w = size.width
                val h = size.height
                val padX = 16f
                val padTop = 14f
                val padBottom = 14f
                val chartW = (w - padX * 2f).coerceAtLeast(10f)
                val chartH = (h - padTop - padBottom).coerceAtLeast(10f)

                for (i in 0..3) {
                    val y = padTop + (chartH / 3f) * i
                    drawLine(
                        color = BorderSubtle,
                        start = Offset(padX, y),
                        end = Offset(w - padX, y),
                        strokeWidth = 1.5f
                    )
                }

                if (points.isEmpty()) return@Canvas

                val stepX = if (points.size == 1) 0f else chartW / (points.size - 1).toFloat()
                val coords = points.mapIndexed { idx, pt ->
                    val x = if (points.size == 1) padX + chartW / 2f else padX + idx * stepX
                    val ratio = (pt.cumulativeBalance - minVal).toFloat() / span
                    val targetY = padTop + chartH * (1f - ratio)
                    val animatedY = (padTop + chartH) - ((padTop + chartH) - targetY) * progress
                    Offset(x, animatedY)
                }

                if (coords.size == 1) {
                    val pt = coords.first()
                    val flatFill = Path().apply {
                        moveTo(padX, padTop + chartH)
                        lineTo(padX, pt.y)
                        lineTo(w - padX, pt.y)
                        lineTo(w - padX, padTop + chartH)
                        close()
                    }
                    drawPath(
                        path = flatFill,
                        brush = Brush.verticalGradient(
                            colors = listOf(BrandBlue.copy(alpha = 0.22f), BrandBlue.copy(alpha = 0.02f)),
                            startY = padTop,
                            endY = padTop + chartH
                        )
                    )
                    drawLine(
                        color = BrandBlue,
                        start = Offset(padX, pt.y),
                        end = Offset(w - padX, pt.y),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                } else {
                    val fillPath = Path().apply {
                        moveTo(coords.first().x, padTop + chartH)
                        lineTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.size - 1) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }
                        lineTo(coords.last().x, padTop + chartH)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(BrandBlue.copy(alpha = 0.26f), BrandBlue.copy(alpha = 0.02f)),
                            startY = padTop,
                            endY = padTop + chartH
                        )
                    )

                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        for (i in 0 until coords.size - 1) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = BrandBlue,
                        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                coords.forEach { pt ->
                    drawCircle(color = Color(0xFF18181B), radius = 9f, center = pt)
                    drawCircle(color = BrandBlue, radius = 5.5f, center = pt)
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.width(52.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = if (points.size == 1) Arrangement.Center else Arrangement.SpaceBetween
            ) {
                val shown = if (points.size <= 5) points else listOf(points.first(), points[points.size / 2], points.last())
                shown.forEach { pt ->
                    Text(pt.label, color = InkMuted, fontSize = 10.sp)
                }
            }
        }
        points.lastOrNull()?.let { latest ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Saldo akhir periode: ${TabunganLogic.formatRupiah(latest.cumulativeBalance, masked)}",
                color = BrandBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SetorTarikBarChart(
    points: List<MonthlyPoint>,
    masked: Boolean = false,
    modifier: Modifier = Modifier
) {
    var revealed by remember(points) { mutableStateOf(false) }
    LaunchedEffect(points) { revealed = true }
    val progress by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bar_reveal"
    )

    val displayed = points.takeLast(6)
    val maxVal = displayed.maxOfOrNull { max(it.setor, it.tarik) }?.coerceAtLeast(1L) ?: 1L

    Column(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(BrandBlue, "Setor")
            LegendDot(BrandRed, "Tarik")
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .height(160.dp)
                    .width(52.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatCompactRupiah(maxVal, masked), color = InkMuted, fontSize = 10.sp)
                Text(formatCompactRupiah(maxVal / 2L, masked), color = InkMuted, fontSize = 10.sp)
                Text("Rp0", color = InkMuted, fontSize = 10.sp)
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
            ) {
                val w = size.width
                val h = size.height
                val chartH = (h - 12f).coerceAtLeast(10f)

                for (i in 0..3) {
                    val y = (chartH / 3f) * i
                    drawLine(BorderSubtle, Offset(0f, y), Offset(w, y), strokeWidth = 1.5f)
                }

                if (displayed.isEmpty()) return@Canvas

                val groupWidth = w / displayed.size.toFloat()
                val barWidth = (groupWidth * 0.26f).coerceIn(12f, 28f)
                val gap = barWidth * 0.35f

                displayed.forEachIndexed { idx, pt ->
                    val centerX = groupWidth * idx + groupWidth / 2f
                    val rawSetorH = ((pt.setor.toFloat() / maxVal.toFloat()) * chartH) * progress
                    val rawTarikH = ((pt.tarik.toFloat() / maxVal.toFloat()) * chartH) * progress
                    val setorH = if (pt.setor > 0L) rawSetorH.coerceAtLeast(8f * progress) else 0f
                    val tarikH = if (pt.tarik > 0L) rawTarikH.coerceAtLeast(8f * progress) else 0f

                    if (setorH > 1f) {
                        drawRoundRect(
                            color = BrandBlue,
                            topLeft = Offset(centerX - barWidth - gap / 2f, chartH - setorH),
                            size = Size(barWidth, setorH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                    if (tarikH > 1f) {
                        drawRoundRect(
                            color = BrandRed,
                            topLeft = Offset(centerX + gap / 2f, chartH - tarikH),
                            size = Size(barWidth, tarikH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(52.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                displayed.forEach { pt ->
                    Text(pt.label, color = InkMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, color = InkMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
