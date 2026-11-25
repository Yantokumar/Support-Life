package com.yanto.catatanuang.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.data.PinRateLimiter
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.model.CoupleProfile
import com.yanto.catatanuang.ui.components.ShieldLockIcon
import com.yanto.catatanuang.ui.components.TwinCoinLogo
import com.yanto.catatanuang.ui.theme.BorderSubtle
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandBlueSoft
import com.yanto.catatanuang.ui.theme.BrandOrange
import com.yanto.catatanuang.ui.theme.BrandRed
import com.yanto.catatanuang.ui.theme.CanvasWhite
import com.yanto.catatanuang.ui.theme.InkMuted
import com.yanto.catatanuang.ui.theme.InkPrimary
import com.yanto.catatanuang.ui.theme.SurfaceWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PinLockScreen(
    profile: CoupleProfile,
    rateLimiter: PinRateLimiter,
    onUnlocked: () -> Unit
) {
    var inputPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var lockSeconds by remember { mutableIntStateOf(rateLimiter.remainingLockSeconds()) }
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(lockSeconds) {
        if (lockSeconds > 0) {
            delay(1000L)
            lockSeconds = rateLimiter.remainingLockSeconds()
            if (lockSeconds == 0) errorText = null
        }
    }

    fun triggerShake() {
        scope.launch {
            for (delta in listOf(-14f, 14f, -10f, 10f, -4f, 0f)) {
                shakeOffset.animateTo(delta, tween(45))
            }
        }
    }

    fun tryVerify() {
        val currentLock = rateLimiter.remainingLockSeconds()
        if (currentLock > 0) {
            lockSeconds = currentLock
            return
        }
        if (inputPin.length != 6) {
            errorText = "Masukkan 6 digit PIN."
            triggerShake()
            return
        }
        if (TabunganLogic.verifyPinConstantTime(inputPin, profile.pinHash, profile.pinSalt)) {
            rateLimiter.reset()
            onUnlocked()
        } else {
            val lockedFor = rateLimiter.recordFailure()
            inputPin = ""
            if (lockedFor > 0) {
                lockSeconds = lockedFor
                errorText = "Terlalu banyak percobaan. Tunggu $lockedFor detik."
            } else {
                errorText = "PIN salah. Sisa ${rateLimiter.attemptsRemaining()} percobaan."
            }
            triggerShake()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TwinCoinLogo(size = 44.dp)
        Spacer(Modifier.height(12.dp))
        Text(profile.appTitle, color = InkPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShieldLockIcon(tint = BrandBlue, size = 16.dp)
            Spacer(Modifier.width(6.dp))
            Text("Masukkan PIN untuk membuka aplikasi", color = InkMuted, fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(6) { idx ->
                val filled = idx < inputPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (filled) BrandBlue else BorderSubtle)
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = when {
                lockSeconds > 0 -> "Terkunci sementara ($lockSeconds dtk)"
                errorText != null -> errorText!!
                else -> " "
            },
            color = if (lockSeconds > 0 || errorText != null) BrandRed else InkMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(20.dp))
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("Hapus", "0", "OK")
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { key ->
                        val isAction = key == "Hapus" || key == "OK"
                        Card(
                            modifier = Modifier
                                .size(width = 78.dp, height = 54.dp)
                                .clickable(enabled = lockSeconds == 0) {
                                    errorText = null
                                    when (key) {
                                        "Hapus" -> if (inputPin.isNotEmpty()) inputPin = inputPin.dropLast(1)
                                        "OK" -> tryVerify()
                                        else -> if (inputPin.length < 6) inputPin += key
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (key) {
                                    "OK" -> BrandOrange
                                    "Hapus" -> BrandBlueSoft
                                    else -> SurfaceWhite
                                }
                            ),
                            border = BorderStroke(1.dp, if (isAction) BrandBlueSoft else BorderSubtle)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = key,
                                    color = when (key) {
                                        "OK" -> SurfaceWhite
                                        "Hapus" -> BrandBlue
                                        else -> InkPrimary
                                    },
                                    fontSize = if (isAction) 13.sp else 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        if (inputPin.length == 6 && lockSeconds == 0) {
            Button(
                onClick = ::tryVerify,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Buka Kunci", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
