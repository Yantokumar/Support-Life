package com.yanto.catatanuang.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.model.SavingsGoal
import com.yanto.catatanuang.ui.components.EmptyStateCard
import com.yanto.catatanuang.ui.components.TargetFlagIcon
import com.yanto.catatanuang.ui.theme.BorderSubtle
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandBlueSoft
import com.yanto.catatanuang.ui.theme.BrandOrange
import com.yanto.catatanuang.ui.theme.BrandOrangeSoft
import com.yanto.catatanuang.ui.theme.BrandRed
import com.yanto.catatanuang.ui.theme.InkMuted
import com.yanto.catatanuang.ui.theme.InkPrimary
import com.yanto.catatanuang.ui.theme.SurfaceWhite

@Composable
fun GoalsScreen(
    modifier: Modifier,
    goals: List<SavingsGoal>,
    privacyMode: Boolean,
    onAddGoal: () -> Unit,
    onEditGoal: (SavingsGoal) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Target Tabungan", color = InkPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Pantau tujuan bersama dengan progres yang jelas.", color = InkMuted, fontSize = 12.sp)
            }
            Button(
                onClick = onAddGoal,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Target", fontSize = 12.sp)
            }
        }

        if (goals.isEmpty()) {
            EmptyStateCard(
                title = "Belum ada target tabungan",
                message = "Tambahkan target seperti Liburan, Dana Darurat, atau Rumah.",
                actionLabel = "Tambah Target",
                onAction = onAddGoal
            )
        } else {
            goals.forEach { goal ->
                GoalDetailCard(
                    goal = goal,
                    masked = privacyMode,
                    onEdit = { onEditGoal(goal) },
                    onDelete = { onDeleteGoal(goal) }
                )
            }
        }

        Spacer(Modifier.height(84.dp))
    }
}

@Composable
private fun GoalDetailCard(
    goal: SavingsGoal,
    masked: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val ratio = if (goal.targetAmount > 0) (goal.savedAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f) else 0f
    val animProgress by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "goal_progress"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandOrangeSoft),
                    contentAlignment = Alignment.Center
                ) {
                    TargetFlagIcon(tint = BrandOrange, size = 20.dp)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(goal.title, color = InkPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    val dlText = if (goal.deadline.isNotBlank()) "Tenggat ${TabunganLogic.formatDisplayDate(goal.deadline)}" else "Tanpa tenggat"
                    Text("${goal.category} · $dlText", color = InkMuted, fontSize = 11.sp)
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Ubah target", tint = InkMuted, modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus target", tint = BrandRed, modifier = Modifier.size(17.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (ratio >= 1f) BrandOrange else BrandBlue,
                trackColor = BrandBlueSoft
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${TabunganLogic.formatRupiah(goal.savedAmount, masked)} / ${TabunganLogic.formatRupiah(goal.targetAmount, masked)}",
                    color = InkPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(ratio * 100).toInt()}%",
                    color = BrandBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
