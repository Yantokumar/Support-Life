package com.yanto.catatanuang.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.model.AuditEntry
import com.yanto.catatanuang.model.CalendarNote
import com.yanto.catatanuang.model.CoupleProfile
import com.yanto.catatanuang.model.NoteColorTag
import com.yanto.catatanuang.model.PersonalNote
import com.yanto.catatanuang.model.SavingsGoal
import com.yanto.catatanuang.model.SavingsMode
import com.yanto.catatanuang.model.SavingsTransaction
import com.yanto.catatanuang.model.TransactionType
import com.yanto.catatanuang.ui.theme.BorderSubtle
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandBlueSoft
import com.yanto.catatanuang.ui.theme.BrandOrange
import com.yanto.catatanuang.ui.theme.BrandRed
import com.yanto.catatanuang.ui.theme.InkMuted
import com.yanto.catatanuang.ui.theme.InkPrimary
import com.yanto.catatanuang.ui.theme.SurfaceWhite

@Composable
fun TransactionDialog(
    mode: SavingsMode,
    initialType: TransactionType,
    existing: SavingsTransaction?,
    profile: CoupleProfile,
    onDismiss: () -> Unit,
    onSave: (type: TransactionType, partnerId: Int, amount: Long, category: String, note: String, date: String) -> Unit
) {
    var type by remember { mutableStateOf(existing?.type ?: initialType) }
    var partnerId by remember { mutableIntStateOf(if (mode == SavingsMode.SOLO) 1 else existing?.partnerId ?: 1) }
    var amountText by remember { mutableStateOf(existing?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: TabunganLogic.defaultCategories.first()) }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var dateText by remember { mutableStateOf(existing?.date ?: TabunganLogic.todayIso()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = if (existing == null) "Catat Transaksi" else "Ubah Transaksi",
                color = InkPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Jenis Transaksi", color = InkMuted, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { option ->
                        FilterChip(
                            selected = type == option,
                            onClick = { type = option },
                            label = { Text(option.label, fontSize = 12.sp) }
                        )
                    }
                }

                if (mode == SavingsMode.DUO) {
                    Text("Oleh Pasangan", color = InkMuted, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to profile.partner1Name, 2 to profile.partner2Name).forEach { (id, name) ->
                            FilterChip(
                                selected = partnerId == id,
                                onClick = { partnerId = id },
                                label = { Text(name, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter(Char::isDigit); errorMsg = null },
                    label = { Text("Nominal (Rupiah)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it.trim(); errorMsg = null },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Kategori", color = InkMuted, fontSize = 11.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TabunganLogic.defaultCategories.forEach { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Keterangan (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let {
                    Text(it, color = BrandRed, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull()
                    when {
                        amount == null || amount <= 0L -> errorMsg = "Nominal harus lebih dari 0."
                        !TabunganLogic.isValidIsoDate(dateText) -> errorMsg = "Format tanggal harus YYYY-MM-DD."
                        else -> onSave(type, partnerId, amount, category, note.trim(), dateText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun GoalDialog(
    existing: SavingsGoal?,
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmount: Long, savedAmount: Long, deadline: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var targetText by remember { mutableStateOf(existing?.targetAmount?.toString() ?: "") }
    var savedText by remember { mutableStateOf(existing?.savedAmount?.toString() ?: "0") }
    var deadline by remember { mutableStateOf(existing?.deadline ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: TabunganLogic.defaultCategories.first()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = if (existing == null) "Tambah Target Tabungan" else "Ubah Target Tabungan",
                color = InkPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMsg = null },
                    label = { Text("Nama Target (mis. Dana Darurat)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter(Char::isDigit); errorMsg = null },
                    label = { Text("Target Nominal (Rupiah)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = savedText,
                    onValueChange = { savedText = it.filter(Char::isDigit); errorMsg = null },
                    label = { Text("Terkumpul Saat Ini (Rupiah)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it.trim(); errorMsg = null },
                    label = { Text("Tenggat (YYYY-MM-DD, opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TabunganLogic.defaultCategories.forEach { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item, fontSize = 11.sp) }
                        )
                    }
                }
                errorMsg?.let { Text(it, color = BrandRed, fontSize = 11.sp) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toLongOrNull()
                    val saved = savedText.toLongOrNull() ?: 0L
                    when {
                        title.isBlank() -> errorMsg = "Nama target tidak boleh kosong."
                        target == null || target <= 0L -> errorMsg = "Target nominal harus lebih dari 0."
                        deadline.isNotBlank() && !TabunganLogic.isValidIsoDate(deadline) ->
                            errorMsg = "Format tenggat harus YYYY-MM-DD."
                        else -> onSave(title.trim(), target, saved, deadline, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Simpan Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AuditLogDialog(
    logs: List<AuditEntry>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text("Riwayat Aktivitas", color = InkPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            if (logs.isEmpty()) {
                Text("Belum ada aktivitas yang tercatat.", color = InkMuted, fontSize = 13.sp)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.action,
                                        color = BrandBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BrandBlueSoft)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = TabunganLogic.formatAuditTime(item.timestamp),
                                        color = InkMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(item.detail, color = InkPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
fun CalendarNoteDialog(
    defaultDate: String,
    existing: CalendarNote?,
    onDismiss: () -> Unit,
    onSave: (date: String, title: String, description: String, colorTag: NoteColorTag) -> Unit
) {
    var dateText by remember { mutableStateOf(existing?.date ?: defaultDate) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var colorTag by remember { mutableStateOf(existing?.colorTag ?: NoteColorTag.BLUE) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = if (existing == null) "Tambah Catatan Tanggal" else "Ubah Catatan Tanggal",
                color = InkPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it.trim(); errorMsg = null },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMsg = null },
                    label = { Text("Judul / Nama Hari (mis. Gajian, Ulang Tahun)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detail Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Penanda Warna", color = InkMuted, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NoteColorTag.entries.forEach { tag ->
                        FilterChip(
                            selected = colorTag == tag,
                            onClick = { colorTag = tag },
                            label = { Text(tag.label, fontSize = 11.sp) }
                        )
                    }
                }
                errorMsg?.let { Text(it, color = BrandRed, fontSize = 11.sp) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        !TabunganLogic.isValidIsoDate(dateText) -> errorMsg = "Format tanggal harus YYYY-MM-DD."
                        title.isBlank() -> errorMsg = "Judul catatan wajib diisi."
                        else -> onSave(dateText, title.trim(), description.trim(), colorTag)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun PersonalNoteDialog(
    existing: PersonalNote?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, pinned: Boolean, colorTag: NoteColorTag) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var content by remember { mutableStateOf(existing?.content ?: "") }
    var pinned by remember { mutableStateOf(existing?.pinned ?: false) }
    var colorTag by remember { mutableStateOf(existing?.colorTag ?: NoteColorTag.BLUE) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Text(
                text = if (existing == null) "Tambah Catatan" else "Ubah Catatan",
                color = InkPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMsg = null },
                    label = { Text("Judul Catatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi Catatan") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = pinned,
                        onClick = { pinned = !pinned },
                        label = { Text(if (pinned) "Disematkan" else "Sematkan", fontSize = 11.sp) }
                    )
                    NoteColorTag.entries.forEach { tag ->
                        FilterChip(
                            selected = colorTag == tag,
                            onClick = { colorTag = tag },
                            label = { Text(tag.label, fontSize = 11.sp) }
                        )
                    }
                }
                errorMsg?.let { Text(it, color = BrandRed, fontSize = 11.sp) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) errorMsg = "Judul catatan wajib diisi."
                    else onSave(title.trim(), content.trim(), pinned, colorTag)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = { Text(title, color = InkPrimary, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = InkMuted, fontSize = 13.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
