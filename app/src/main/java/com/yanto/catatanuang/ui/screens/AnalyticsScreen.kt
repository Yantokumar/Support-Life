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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.model.SavingsTransaction
import com.yanto.catatanuang.model.TimeRange
import com.yanto.catatanuang.model.TransactionType
import com.yanto.catatanuang.ui.components.BalanceAreaChart
import com.yanto.catatanuang.ui.components.EmptyStateCard
import com.yanto.catatanuang.ui.components.SetorTarikBarChart
import com.yanto.catatanuang.ui.theme.BorderSubtle
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandBlueSoft
import com.yanto.catatanuang.ui.theme.BrandOrange
import com.yanto.catatanuang.ui.theme.CanvasWhite
import com.yanto.catatanuang.ui.theme.InkMuted
import com.yanto.catatanuang.ui.theme.InkPrimary
import com.yanto.catatanuang.ui.theme.SurfaceWhite

@Composable
fun AnalyticsScreen(
    modifier: Modifier,
    transactions: List<SavingsTransaction>,
    privacyMode: Boolean,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit
) {
    var range by remember { mutableStateOf(TimeRange.ALL) }
    var chartTab by remember { mutableIntStateOf(0) } // 0 = Akumulasi Saldo, 1 = Setor vs Tarik
    val txSnapshot = transactions.toList()
    val transactionSeries = remember(txSnapshot, range) {
        TabunganLogic.buildTransactionSeries(txSnapshot, range)
    }
    val weeklySeries = remember(txSnapshot, range) {
        TabunganLogic.buildWeeklySeries(txSnapshot, range)
    }

    val setorByCategory = remember(txSnapshot) {
        val setorList = txSnapshot.filter { it.type == TransactionType.SETOR }
        val total = setorList.sumOf { it.amount }.coerceAtLeast(1L)
        setorList.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .map { (cat, amt) -> Triple(cat, amt, amt.toFloat() / total.toFloat()) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Grafik & Laporan", color = InkPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Tren tabungan dan ekspor dokumen.", color = InkMuted, fontSize = 12.sp)
        }

        if (transactionSeries.isEmpty()) {
            EmptyStateCard(
                title = "Grafik belum tersedia",
                message = "Tambahkan transaksi setor atau tarik untuk melihat akumulasi saldo dan perbandingan."
            )
        } else {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tren Keuangan", color = InkPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Perkembangan akumulasi saldo dan perbandingan setor vs tarik",
                        color = InkMuted,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // Segmented chart switcher: Akumulasi Saldo | Setor vs Tarik
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CanvasWhite)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Akumulasi Saldo", "Setor vs Tarik").forEachIndexed { idx, label ->
                            val selected = chartTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) SurfaceWhite else CanvasWhite)
                                    .clickable { chartTab = idx }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) BrandBlue else InkMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeRange.entries.forEach { option ->
                            FilterChip(
                                selected = range == option,
                                onClick = { range = option },
                                label = { Text(option.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (chartTab == 0) {
                        BalanceAreaChart(points = transactionSeries, masked = privacyMode)
                    } else {
                        SetorTarikBarChart(points = weeklySeries, masked = privacyMode)
                    }
                }
            }
        }

        if (setorByCategory.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Kategori Setoran Teratas", color = InkPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    setorByCategory.take(5).forEach { (cat, amt, ratio) ->
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat, color = InkPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(TabunganLogic.formatRupiah(amt, privacyMode), color = InkMuted, fontSize = 11.sp)
                            }
                            Spacer(Modifier.height(5.dp))
                            LinearProgressIndicator(
                                progress = { ratio.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = BrandBlue,
                                trackColor = BrandBlueSoft
                            )
                        }
                    }
                }
            }
        }

        // Offline Export & Import card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ekspor & Impor Data", color = InkPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Simpan laporan PDF/CSV langsung ke penyimpanan perangkat atau impor hingga 500 baris transaksi CSV.",
                    color = InkMuted,
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onExportPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ekspor PDF", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onExportCsv,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ekspor CSV", fontSize = 12.sp)
                    }
                }
                OutlinedButton(
                    onClick = onImportCsv,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Impor Transaksi dari CSV", color = BrandBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(84.dp))
    }
}
