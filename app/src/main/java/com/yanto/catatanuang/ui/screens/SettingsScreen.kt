package com.yanto.catatanuang.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.yanto.catatanuang.model.CoupleProfile
import com.yanto.catatanuang.model.SavingsMode
import com.yanto.catatanuang.ui.components.ConfirmDeleteDialog
import com.yanto.catatanuang.ui.components.ShieldLockIcon
import com.yanto.catatanuang.ui.components.TwinCoinLogo
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
fun SettingsScreen(
    modifier: Modifier,
    profile: CoupleProfile,
    currentMode: SavingsMode,
    privacyMode: Boolean,
    soloTxCount: Int,
    soloGoalCount: Int,
    duoTxCount: Int,
    duoGoalCount: Int,
    appVersion: String,
    onBack: () -> Unit,
    onTogglePrivacy: (Boolean) -> Unit,
    onSaveProfile: (CoupleProfile, String?) -> Unit,
    onLockNow: () -> Unit,
    onOpenAuditLog: () -> Unit,
    onExportPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onImportCsv: () -> Unit,
    onResetCurrentMode: () -> Unit
) {
    var soloName by remember(profile) { mutableStateOf(profile.soloName) }
    var p1 by remember(profile) { mutableStateOf(profile.partner1Name) }
    var p2 by remember(profile) { mutableStateOf(profile.partner2Name) }
    var profileError by remember { mutableStateOf<String?>(null) }

    var newPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = InkPrimary)
            }
            Column {
                Text("Pengaturan & Informasi", color = InkPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Kelola profil, keamanan PIN, dan pencadangan data.", color = InkMuted, fontSize = 12.sp)
            }
        }

        // 1. Profil & Label Pemilik (Judul aplikasi terkunci: Support Life)
        SettingsCard(title = "Profil & Label Pemilik") {
            Text(
                text = "Judul aplikasi terkunci sebagai \"${CoupleProfile.APP_TITLE}\". Anda dapat menyesuaikan label pemilik untuk setiap mode.",
                color = InkMuted,
                fontSize = 11.sp
            )
            OutlinedTextField(
                value = soloName,
                onValueChange = { soloName = it; profileError = null },
                label = { Text("Label Tabungan Pribadi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = p1,
                onValueChange = { p1 = it; profileError = null },
                label = { Text("Nama Pasangan 1") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = p2,
                onValueChange = { p2 = it; profileError = null },
                label = { Text("Nama Pasangan 2") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            profileError?.let { Text(it, color = BrandRed, fontSize = 11.sp) }
            Button(
                onClick = {
                    if (soloName.isBlank() || p1.isBlank() || p2.isBlank()) {
                        profileError = "Semua label nama wajib diisi."
                    } else {
                        onSaveProfile(
                            profile.copy(
                                soloName = soloName.trim(),
                                partner1Name = p1.trim(),
                                partner2Name = p2.trim()
                            ),
                            null
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan Profil", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // 2. Keamanan & Privasi
        SettingsCard(title = "Keamanan & Privasi") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Mode Privasi", color = InkPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Sembunyikan nominal saldo di semua layar", color = InkMuted, fontSize = 11.sp)
                }
                Switch(checked = privacyMode, onCheckedChange = onTogglePrivacy)
            }

            HorizontalDivider(color = BorderSubtle)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShieldLockIcon(tint = if (profile.hasPin) BrandBlue else InkMuted, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Kunci PIN Aplikasi", color = InkPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (profile.hasPin) "Aktif (SHA-256 · maks. 5 percobaan / 60 dtk)" else "Belum diatur",
                            color = if (profile.hasPin) BrandBlue else InkMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            OutlinedTextField(
                value = newPin,
                onValueChange = { newPin = it.filter(Char::isDigit).take(6); pinError = null },
                label = { Text(if (profile.hasPin) "PIN Baru (6 digit)" else "Buat PIN Kunci (6 digit)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            pinError?.let { Text(it, color = BrandRed, fontSize = 11.sp) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (newPin.length != 6) {
                            pinError = "PIN harus tepat 6 digit angka."
                        } else {
                            val salt = TabunganLogic.generateSalt()
                            val hash = TabunganLogic.hashPin(newPin, salt)
                            newPin = ""
                            onSaveProfile(profile.copy(pinHash = hash, pinSalt = salt), "PIN keamanan diperbarui")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (profile.hasPin) "Ubah PIN" else "Aktifkan PIN", fontSize = 12.sp)
                }

                if (profile.hasPin) {
                    OutlinedButton(
                        onClick = {
                            newPin = ""
                            onSaveProfile(profile.copy(pinHash = "", pinSalt = ""), "PIN keamanan dinonaktifkan")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Nonaktifkan", color = BrandRed, fontSize = 12.sp)
                    }
                }
            }

            if (profile.hasPin) {
                OutlinedButton(
                    onClick = onLockNow,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kunci Aplikasi Sekarang", color = BrandBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 3. Data & Pencadangan Offline
        SettingsCard(title = "Data & Pencadangan") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DataStatBox(
                    title = "Tabungan Pribadi",
                    detail = "$soloTxCount transaksi · $soloGoalCount target",
                    tint = BrandOrange,
                    bg = BrandOrangeSoft,
                    modifier = Modifier.weight(1f)
                )
                DataStatBox(
                    title = "Tabungan Bersama",
                    detail = "$duoTxCount transaksi · $duoGoalCount target",
                    tint = BrandBlue,
                    bg = BrandBlueSoft,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Aksi ekspor/impor dan reset di bawah berlaku untuk mode aktif saat ini (${currentMode.label}).",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onImportCsv,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Impor CSV", color = BrandBlue, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onOpenAuditLog,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Riwayat Aktivitas", color = InkPrimary, fontSize = 12.sp)
                }
            }

            OutlinedButton(
                onClick = { showResetConfirm = true },
                border = BorderStroke(1.dp, BrandRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Data Mode ${currentMode.label}", color = BrandRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // 4. Tentang Support Life
        SettingsCard(title = "Tentang Aplikasi") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TwinCoinLogo(size = 34.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(CoupleProfile.APP_TITLE, color = InkPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Versi $appVersion", color = BrandBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(color = BorderSubtle)
            InfoLine("Mode Tabungan", "Tabungan Pribadi & Tabungan Bersama dengan penyimpanan terpisah")
            InfoLine("Penyimpanan", "Penyimpanan lokal di perangkat")
            InfoLine("Keamanan", "Hash PIN SHA-256 + salt, verifikasi constant-time, jeda 60 dtk")
            InfoLine("Laporan", "Ekspor PDF/CSV, impor CSV hingga 500 baris, dan riwayat aktivitas")
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showResetConfirm) {
        ConfirmDeleteDialog(
            title = "Reset Data Mode ${currentMode.label}",
            message = "Semua transaksi, target tabungan, dan riwayat aktivitas di mode ${currentMode.label} akan dihapus permanen. Data mode lainnya tetap aman.",
            onDismiss = { showResetConfirm = false },
            onConfirm = {
                showResetConfirm = false
                onResetCurrentMode()
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, color = InkPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun DataStatBox(
    title: String,
    detail: String,
    tint: androidx.compose.ui.graphics.Color,
    bg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(12.dp)
    ) {
        Column {
            Text(title, color = tint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(detail, color = InkPrimary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = InkPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(value, color = InkMuted, fontSize = 11.sp)
    }
}
