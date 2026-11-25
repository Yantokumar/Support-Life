package com.yanto.catatanuang.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.model.CoupleProfile
import com.yanto.catatanuang.model.SavingsMode
import com.yanto.catatanuang.model.SavingsTransaction
import com.yanto.catatanuang.model.TransactionType
import com.yanto.catatanuang.ui.components.EmptyStateCard
import com.yanto.catatanuang.ui.components.TransactionRow
import com.yanto.catatanuang.ui.theme.InkMuted

@Composable
fun HistoryScreen(
    modifier: Modifier,
    mode: SavingsMode,
    transactions: List<SavingsTransaction>,
    profile: CoupleProfile,
    privacyMode: Boolean,
    onAddClick: () -> Unit,
    onEditTx: (SavingsTransaction) -> Unit,
    onDeleteTx: (SavingsTransaction) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var partnerFilter by remember { mutableStateOf<Int?>(null) }
    var typeFilter by remember { mutableStateOf<TransactionType?>(null) }
    var categoryFilter by remember { mutableStateOf<String?>(null) }

    val txSnapshot = transactions.toList()
    val filtered = remember(txSnapshot, query, partnerFilter, typeFilter, categoryFilter) {
        TabunganLogic.filterTransactions(
            transactions = txSnapshot,
            query = query,
            partnerFilter = partnerFilter,
            typeFilter = typeFilter,
            categoryFilter = categoryFilter
        )
    }

    val categories = remember(txSnapshot) {
        (TabunganLogic.defaultCategories + txSnapshot.map { it.category }).distinct()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Cari catatan, kategori, tanggal...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (mode == SavingsMode.DUO) {
                FilterChip(
                    selected = partnerFilter == null,
                    onClick = { partnerFilter = null },
                    label = { Text("Semua Pasangan", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = partnerFilter == 1,
                    onClick = { partnerFilter = if (partnerFilter == 1) null else 1 },
                    label = { Text(profile.partner1Name, fontSize = 11.sp) }
                )
                FilterChip(
                    selected = partnerFilter == 2,
                    onClick = { partnerFilter = if (partnerFilter == 2) null else 2 },
                    label = { Text(profile.partner2Name, fontSize = 11.sp) }
                )
            }
            TransactionType.entries.forEach { t ->
                FilterChip(
                    selected = typeFilter == t,
                    onClick = { typeFilter = if (typeFilter == t) null else t },
                    label = { Text(t.label, fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = categoryFilter == null,
                onClick = { categoryFilter = null },
                label = { Text("Semua Kategori", fontSize = 11.sp) }
            )
            categories.forEach { cat ->
                FilterChip(
                    selected = categoryFilter == cat,
                    onClick = { categoryFilter = if (categoryFilter == cat) null else cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        if (filtered.isEmpty()) {
            Spacer(Modifier.height(12.dp))
            EmptyStateCard(
                title = if (transactions.isEmpty()) "Belum ada transaksi" else "Tidak ada hasil yang cocok",
                message = if (transactions.isEmpty()) {
                    "Catat setoran atau penarikan pertama untuk melihat riwayat."
                } else {
                    "Coba ubah kata kunci pencarian atau reset filter."
                },
                actionLabel = if (transactions.isEmpty()) "Catat Transaksi" else "Reset Filter",
                onAction = {
                    if (transactions.isEmpty()) {
                        onAddClick()
                    } else {
                        query = ""
                        partnerFilter = null
                        typeFilter = null
                        categoryFilter = null
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 92.dp)
            ) {
                itemsIndexed(filtered, key = { _, item -> item.id }) { index, item ->
                    if (index == 0 || filtered[index - 1].date != item.date) {
                        Text(
                            text = TabunganLogic.formatDisplayDate(item.date),
                            color = InkMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }
                    TransactionRow(
                        tx = item,
                        profile = profile,
                        mode = mode,
                        masked = privacyMode,
                        onEdit = { onEditTx(item) },
                        onDelete = { onDeleteTx(item) }
                    )
                }
            }
        }
    }
}
