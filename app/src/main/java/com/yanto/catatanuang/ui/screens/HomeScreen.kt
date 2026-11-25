package com.yanto.catatanuang.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.model.CalendarNote
import com.yanto.catatanuang.model.CoupleProfile
import com.yanto.catatanuang.model.NoteColorTag
import com.yanto.catatanuang.model.PersonalNote
import com.yanto.catatanuang.model.SavingsGoal
import com.yanto.catatanuang.model.SavingsMode
import com.yanto.catatanuang.model.SavingsTransaction
import com.yanto.catatanuang.model.SummaryStats
import com.yanto.catatanuang.model.TransactionType
import com.yanto.catatanuang.ui.components.DepositVaultIcon
import com.yanto.catatanuang.ui.components.EmptyStateCard
import com.yanto.catatanuang.ui.components.ModeSwitcherPill
import com.yanto.catatanuang.ui.components.PartnerGemIcon
import com.yanto.catatanuang.ui.components.PressScaleCard
import com.yanto.catatanuang.ui.components.PrivacyEyeIcon
import com.yanto.catatanuang.ui.components.TargetFlagIcon
import com.yanto.catatanuang.ui.components.TransactionRow
import com.yanto.catatanuang.ui.components.WithdrawVaultIcon
import com.yanto.catatanuang.ui.theme.BorderSubtle
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandBlueDeep
import com.yanto.catatanuang.ui.theme.BrandBlueSoft
import com.yanto.catatanuang.ui.theme.BrandOrange
import com.yanto.catatanuang.ui.theme.BrandOrangeDeep
import com.yanto.catatanuang.ui.theme.BrandOrangeSoft
import com.yanto.catatanuang.ui.theme.BrandRed
import com.yanto.catatanuang.ui.theme.BrandRedSoft
import com.yanto.catatanuang.ui.theme.InkMuted
import com.yanto.catatanuang.ui.theme.InkPrimary
import com.yanto.catatanuang.ui.theme.SurfaceWhite

@Composable
fun HomeScreen(
    modifier: Modifier,
    mode: SavingsMode,
    onModeChange: (SavingsMode) -> Unit,
    profile: CoupleProfile,
    stats: SummaryStats,
    goals: List<SavingsGoal>,
    recentTransactions: List<SavingsTransaction>,
    calendarNotes: List<CalendarNote>,
    personalNotes: List<PersonalNote>,
    privacyMode: Boolean,
    onTogglePrivacy: () -> Unit,
    onQuickAdd: (TransactionType) -> Unit,
    onOpenGoals: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenNotes: () -> Unit,
    onEditTx: (SavingsTransaction) -> Unit,
    onDeleteTx: (SavingsTransaction) -> Unit
) {
    val heroColors = if (mode == SavingsMode.SOLO) {
        listOf(BrandOrangeDeep.copy(alpha = 0.72f), SurfaceWhite)
    } else {
        listOf(BrandBlueDeep.copy(alpha = 0.78f), SurfaceWhite)
    }
    val heroBorder = if (mode == SavingsMode.SOLO) {
        BrandOrange.copy(alpha = 0.40f)
    } else {
        BrandBlue.copy(alpha = 0.40f)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(2.dp))

        ModeSwitcherPill(mode = mode, onModeChange = onModeChange)

        // OLED Hero balance card with subtle accent border and geometric ring motif.
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, heroBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(heroColors))
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val stroke = Stroke(width = 24f)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.height * 0.42f,
                        center = Offset(size.width * 0.86f, size.height * 0.24f),
                        style = stroke
                    )
                    if (mode == SavingsMode.DUO) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.06f),
                            radius = size.height * 0.34f,
                            center = Offset(size.width * 0.96f, size.height * 0.46f),
                            style = stroke
                        )
                    }
                }

                Column(Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (mode == SavingsMode.SOLO) "Saldo ${profile.soloName}" else "Saldo Bersama",
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = onTogglePrivacy,
                            modifier = Modifier.size(34.dp)
                        ) {
                            PrivacyEyeIcon(hidden = privacyMode, tint = Color.White, size = 20.dp)
                        }
                    }

                    AnimatedContent(
                        targetState = TabunganLogic.formatRupiah(stats.totalBalance, privacyMode),
                        label = "balance_privacy"
                    ) { formatted ->
                        Text(
                            text = formatted,
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        BalanceStatItem(
                            label = "Total Setor",
                            value = TabunganLogic.formatRupiah(stats.totalSetor, privacyMode),
                            isDeposit = true
                        )
                        BalanceStatItem(
                            label = "Total Tarik",
                            value = TabunganLogic.formatRupiah(stats.totalTarik, privacyMode),
                            isDeposit = false
                        )
                    }
                }
            }
        }

        // Quick actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PressScaleCard(
                onClick = { onQuickAdd(TransactionType.SETOR) },
                modifier = Modifier.weight(1f),
                containerColor = if (mode == SavingsMode.SOLO) BrandBlue else BrandOrange,
                borderColor = if (mode == SavingsMode.SOLO) BrandBlue else BrandOrange
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DepositVaultIcon(tint = SurfaceWhite, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Setor", color = SurfaceWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            PressScaleCard(
                onClick = { onQuickAdd(TransactionType.TARIK) },
                modifier = Modifier.weight(1f),
                containerColor = SurfaceWhite,
                borderColor = BrandRedSoft
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WithdrawVaultIcon(tint = BrandRed, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Tarik", color = BrandRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (mode == SavingsMode.DUO) {
            PartnerContributionCard(profile = profile, stats = stats, masked = privacyMode)
        }

        // Active goals preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (mode == SavingsMode.SOLO) "Target Pribadi" else "Target Bersama",
                color = InkPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kelola",
                color = BrandOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onOpenGoals).padding(4.dp)
            )
        }

        if (goals.isEmpty()) {
            EmptyStateCard(
                title = "Belum ada target ${mode.label.lowercase()}",
                message = "Buat target tabungan agar progres lebih terukur.",
                actionLabel = "Buat Target",
                onAction = onOpenGoals
            )
        } else {
            goals.take(2).forEach { goal ->
                GoalPreviewCard(goal = goal, masked = privacyMode, onClick = onOpenGoals)
            }
        }

        // Calendar notes summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kalender Note", color = InkPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Buka Kalender",
                color = BrandOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onOpenCalendar).padding(4.dp)
            )
        }

        val upcomingNotes = calendarNotes
            .filter { it.date >= TabunganLogic.todayIso() }
            .sortedBy { it.date }
            .ifEmpty { calendarNotes.sortedByDescending { it.date } }

        if (upcomingNotes.isEmpty()) {
            EmptyStateCard(
                title = "Belum ada catatan kalender",
                message = "Tandai tanggal penting atau pengingat pribadi di menu Kalender.",
                actionLabel = "Buka Kalender",
                onAction = onOpenCalendar
            )
        } else {
            upcomingNotes.take(2).forEach { note ->
                CalendarSummaryRow(note = note, onClick = onOpenCalendar)
            }
        }

        // Personal notes summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Catatan", color = InkPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Buka Catatan",
                color = BrandOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onOpenNotes).padding(4.dp)
            )
        }

        val topNotes = personalNotes
            .sortedWith(compareByDescending<PersonalNote> { it.pinned }.thenByDescending { it.updatedAt })
        if (topNotes.isEmpty()) {
            EmptyStateCard(
                title = "Belum ada catatan",
                message = "Simpan ide, daftar, atau catatan pribadi di menu Catatan.",
                actionLabel = "Buka Catatan",
                onAction = onOpenNotes
            )
        } else {
            topNotes.take(2).forEach { note ->
                PersonalNoteSummaryRow(note = note, onClick = onOpenNotes)
            }
        }

        // Recent transactions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Transaksi Terbaru", color = InkPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Semua",
                color = BrandBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onOpenHistory).padding(4.dp)
            )
        }

        if (recentTransactions.isEmpty()) {
            EmptyStateCard(
                title = "Belum ada transaksi ${mode.label.lowercase()}",
                message = "Mulai catat setoran atau penarikan di mode ${mode.label}.",
                actionLabel = "Catat Setor",
                onAction = { onQuickAdd(TransactionType.SETOR) }
            )
        } else {
            recentTransactions.take(4).forEach { tx ->
                TransactionRow(
                    tx = tx,
                    profile = profile,
                    mode = mode,
                    masked = privacyMode,
                    onEdit = { onEditTx(tx) },
                    onDelete = { onDeleteTx(tx) }
                )
            }
        }

        Spacer(Modifier.height(84.dp))
    }
}

@Composable
private fun BalanceStatItem(label: String, value: String, isDeposit: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            if (isDeposit) DepositVaultIcon(tint = Color.White, size = 15.dp)
            else WithdrawVaultIcon(tint = Color.White, size = 15.dp)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.80f), fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PartnerContributionCard(
    profile: CoupleProfile,
    stats: SummaryStats,
    masked: Boolean
) {
    val totalSetor = (stats.partner1Setor + stats.partner2Setor).coerceAtLeast(1L)
    val p1Target = if (stats.partner1Setor + stats.partner2Setor == 0L) 0.5f else stats.partner1Setor.toFloat() / totalSetor.toFloat()
    val p1Fraction by animateFloatAsState(
        targetValue = p1Target.coerceIn(0.05f, 0.95f),
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "partner_split"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Kontribusi Pasangan", color = InkPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(BrandOrangeSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(p1Fraction)
                        .background(BrandBlue)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(BrandOrange)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PartnerStatColumn(
                    partnerId = 1,
                    name = profile.partner1Name,
                    netAmount = stats.partner1Net,
                    setorAmount = stats.partner1Setor,
                    masked = masked,
                    tint = BrandBlue,
                    bg = BrandBlueSoft
                )
                PartnerStatColumn(
                    partnerId = 2,
                    name = profile.partner2Name,
                    netAmount = stats.partner2Net,
                    setorAmount = stats.partner2Setor,
                    masked = masked,
                    tint = BrandOrange,
                    bg = BrandOrangeSoft,
                    alignEnd = true
                )
            }
        }
    }
}

@Composable
private fun PartnerStatColumn(
    partnerId: Int,
    name: String,
    netAmount: Long,
    setorAmount: Long,
    masked: Boolean,
    tint: Color,
    bg: Color,
    alignEnd: Boolean = false
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            PartnerGemIcon(partnerId = partnerId, tint = tint, size = 10.dp)
            Spacer(Modifier.width(5.dp))
            Text(name, color = tint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = TabunganLogic.formatRupiah(netAmount, masked),
            color = if (netAmount < 0L) BrandRed else InkPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Setor: ${TabunganLogic.formatRupiah(setorAmount, masked)}",
            color = InkMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun GoalPreviewCard(goal: SavingsGoal, masked: Boolean, onClick: () -> Unit) {
    val targetFraction = if (goal.targetAmount > 0) (goal.savedAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f) else 0f
    val animProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "goal_preview"
    )
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TargetFlagIcon(tint = BrandOrange, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                Text(goal.title, color = InkPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("${(targetFraction * 100).toInt()}%", color = BrandBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape),
                color = BrandBlue,
                trackColor = BrandBlueSoft
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${TabunganLogic.formatRupiah(goal.savedAmount, masked)} dari ${TabunganLogic.formatRupiah(goal.targetAmount, masked)}",
                color = InkMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CalendarSummaryRow(note: CalendarNote, onClick: () -> Unit) {
    val dotColor = when (note.colorTag) {
        NoteColorTag.BLUE -> BrandBlue
        NoteColorTag.ORANGE -> BrandOrange
        NoteColorTag.RED -> BrandRed
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(note.title, color = InkPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${TabunganLogic.formatDisplayDate(note.date)} · ${note.colorTag.label}",
                    color = InkMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PersonalNoteSummaryRow(note: PersonalNote, onClick: () -> Unit) {
    val dotColor = when (note.colorTag) {
        NoteColorTag.BLUE -> BrandBlue
        NoteColorTag.ORANGE -> BrandOrange
        NoteColorTag.RED -> BrandRed
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(note.title, color = InkPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content.replace('\n', ' ').take(60),
                        color = InkMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
