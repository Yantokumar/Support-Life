package com.yanto.catatanuang.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.model.CoupleProfile
import com.yanto.catatanuang.model.SavingsGoal
import com.yanto.catatanuang.model.SavingsMode
import com.yanto.catatanuang.model.SavingsSubTab
import com.yanto.catatanuang.model.SavingsTransaction
import com.yanto.catatanuang.ui.components.ModeSwitcherPill
import com.yanto.catatanuang.ui.theme.BorderSubtle
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandOrange
import com.yanto.catatanuang.ui.theme.InkMuted
import com.yanto.catatanuang.ui.theme.SurfaceWhite

@Composable
fun SavingsHubScreen(
    modifier: Modifier,
    subTab: SavingsSubTab,
    onSubTabChange: (SavingsSubTab) -> Unit,
    mode: SavingsMode,
    onModeChange: (SavingsMode) -> Unit,
    transactions: List<SavingsTransaction>,
    goals: List<SavingsGoal>,
    profile: CoupleProfile,
    privacyMode: Boolean,
    onAddTx: () -> Unit,
    onEditTx: (SavingsTransaction) -> Unit,
    onDeleteTx: (SavingsTransaction) -> Unit,
    onAddGoal: () -> Unit,
    onEditGoal: (SavingsGoal) -> Unit,
    onDeleteGoal: (SavingsGoal) -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModeSwitcherPill(mode = mode, onModeChange = onModeChange)

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SavingsSubTab.entries.forEach { tab ->
                        val selected = subTab == tab
                        val activeBg = if (mode == SavingsMode.SOLO) BrandOrange else BrandBlue
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) activeBg else Color.Transparent)
                                .clickable { onSubTabChange(tab) }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                color = if (selected) SurfaceWhite else InkMuted,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
        }

        when (subTab) {
            SavingsSubTab.HISTORY -> HistoryScreen(
                modifier = Modifier.weight(1f),
                mode = mode,
                transactions = transactions,
                profile = profile,
                privacyMode = privacyMode,
                onAddClick = onAddTx,
                onEditTx = onEditTx,
                onDeleteTx = onDeleteTx
            )

            SavingsSubTab.GOALS -> GoalsScreen(
                modifier = Modifier.weight(1f),
                goals = goals,
                privacyMode = privacyMode,
                onAddGoal = onAddGoal,
                onEditGoal = onEditGoal,
                onDeleteGoal = onDeleteGoal
            )

            SavingsSubTab.ANALYTICS -> AnalyticsScreen(
                modifier = Modifier.weight(1f),
                transactions = transactions,
                privacyMode = privacyMode,
                onExportPdf = onExportPdf,
                onExportCsv = onExportCsv,
                onImportCsv = onImportCsv
            )
        }
    }
}
