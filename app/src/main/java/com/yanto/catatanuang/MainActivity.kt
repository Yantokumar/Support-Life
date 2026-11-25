package com.yanto.catatanuang

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.data.PinRateLimiter
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.data.TabunganStore
import com.yanto.catatanuang.model.AuditEntry
import com.yanto.catatanuang.model.CalendarNote
import com.yanto.catatanuang.model.MainTab
import com.yanto.catatanuang.model.PersonalNote
import com.yanto.catatanuang.model.SavingsGoal
import com.yanto.catatanuang.model.SavingsMode
import com.yanto.catatanuang.model.SavingsSubTab
import com.yanto.catatanuang.model.SavingsTransaction
import com.yanto.catatanuang.model.TransactionType
import com.yanto.catatanuang.ui.components.AuditLogDialog
import com.yanto.catatanuang.ui.components.CalendarNoteDialog
import com.yanto.catatanuang.ui.components.CalendarNoteIcon
import com.yanto.catatanuang.ui.components.ConfirmDeleteDialog
import com.yanto.catatanuang.ui.components.GoalDialog
import com.yanto.catatanuang.ui.components.HomeRoofIcon
import com.yanto.catatanuang.ui.components.LedgerScrollIcon
import com.yanto.catatanuang.ui.components.NotePadIcon
import com.yanto.catatanuang.ui.components.PersonalNoteDialog
import com.yanto.catatanuang.ui.components.PrivacyEyeIcon
import com.yanto.catatanuang.ui.components.ShieldLockIcon
import com.yanto.catatanuang.ui.components.TransactionDialog
import com.yanto.catatanuang.ui.components.TwinCoinLogo
import com.yanto.catatanuang.ui.components.WaveTrendIcon
import com.yanto.catatanuang.ui.screens.CalendarScreen
import com.yanto.catatanuang.ui.screens.HomeScreen
import com.yanto.catatanuang.ui.screens.NotesScreen
import com.yanto.catatanuang.ui.screens.PinLockScreen
import com.yanto.catatanuang.ui.screens.SavingsHubScreen
import com.yanto.catatanuang.ui.screens.SettingsScreen
import com.yanto.catatanuang.ui.screens.SplashScreen
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandBlueSoft
import com.yanto.catatanuang.ui.theme.BrandOrange
import com.yanto.catatanuang.ui.theme.CanvasWhite
import com.yanto.catatanuang.ui.theme.InkMuted
import com.yanto.catatanuang.ui.theme.InkPrimary
import com.yanto.catatanuang.ui.theme.SurfaceWhite
import com.yanto.catatanuang.ui.theme.TabunganTheme
import java.time.LocalDate
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.parseColor("#09090B")
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.parseColor("#18181B")
        setContent {
            TabunganTheme {
                SupportLifeApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportLifeApp() {
    val context = LocalContext.current
    val store = remember { TabunganStore(context) }
    val rateLimiter = remember { PinRateLimiter() }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    var profile by remember { mutableStateOf(store.loadProfile()) }
    var isLocked by remember { mutableStateOf(profile.hasPin) }
    var privacyMode by remember { mutableStateOf(store.loadPrivacyMode()) }
    var currentMode by remember { mutableStateOf(store.loadLastMode()) }

    val transactions = remember {
        mutableStateListOf<SavingsTransaction>().apply { addAll(store.loadTransactions(currentMode)) }
    }
    val goals = remember {
        mutableStateListOf<SavingsGoal>().apply { addAll(store.loadGoals(currentMode)) }
    }
    val auditLogs = remember {
        mutableStateListOf<AuditEntry>().apply { addAll(store.loadAuditLogs(currentMode)) }
    }
    val calendarNotes = remember {
        mutableStateListOf<CalendarNote>().apply { addAll(store.loadCalendarNotes()) }
    }
    val personalNotes = remember {
        mutableStateListOf<PersonalNote>().apply { addAll(store.loadPersonalNotes()) }
    }

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var savingsSubTab by remember { mutableStateOf(SavingsSubTab.HISTORY) }
    var showTxDialog by remember { mutableStateOf(false) }
    var txInitialType by remember { mutableStateOf(TransactionType.SETOR) }
    var editingTx by remember { mutableStateOf<SavingsTransaction?>(null) }
    var deletingTx by remember { mutableStateOf<SavingsTransaction?>(null) }

    var showGoalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var deletingGoal by remember { mutableStateOf<SavingsGoal?>(null) }

    var selectedCalendarDate by remember { mutableStateOf(LocalDate.now()) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<CalendarNote?>(null) }
    var deletingNote by remember { mutableStateOf<CalendarNote?>(null) }

    var showPersonalNoteDialog by remember { mutableStateOf(false) }
    var editingPersonalNote by remember { mutableStateOf<PersonalNote?>(null) }
    var deletingPersonalNote by remember { mutableStateOf<PersonalNote?>(null) }

    var showSettings by remember { mutableStateOf(false) }
    var showAuditDialog by remember { mutableStateOf(false) }
    val appVersion = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        }.getOrDefault("1.0")
    }

    fun switchMode(nextMode: SavingsMode) {
        if (nextMode == currentMode) return
        currentMode = nextMode
        store.saveLastMode(nextMode)
        transactions.clear()
        transactions.addAll(store.loadTransactions(nextMode))
        goals.clear()
        goals.addAll(store.loadGoals(nextMode))
        auditLogs.clear()
        auditLogs.addAll(store.loadAuditLogs(nextMode))
    }

    fun addAudit(action: String, detail: String) {
        auditLogs.add(0, AuditEntry(UUID.randomUUID().toString(), System.currentTimeMillis(), action, detail))
        store.saveAuditLogs(currentMode, auditLogs)
    }

    val stats = TabunganLogic.computeSummary(transactions)

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                store.generatePdfReport(out, profile, currentMode, stats, goals, transactions)
            }
            addAudit("Ekspor", "Laporan PDF (${currentMode.label}) berhasil diekspor")
            Toast.makeText(context, "Laporan PDF berhasil disimpan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal menyimpan PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val csv = TabunganLogic.exportTransactionsToCsv(transactions, profile, currentMode)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(csv.toByteArray(Charsets.UTF_8))
            }
            addAudit("Ekspor", "${transactions.size} transaksi (${currentMode.label}) diekspor ke CSV")
            Toast.makeText(context, "Data CSV berhasil disimpan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal menyimpan CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    val importCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val csvText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
            val result = TabunganLogic.parseTransactionsFromCsv(csvText, profile)
            if (result.errorMessage != null) {
                Toast.makeText(context, result.errorMessage, Toast.LENGTH_LONG).show()
            } else {
                transactions.addAll(0, result.imported)
                store.saveTransactions(currentMode, transactions)
                addAudit("Impor", "Impor CSV (${currentMode.label}): ${result.imported.size} berhasil, ${result.skippedCount} dilewati")
                Toast.makeText(
                    context,
                    "Impor selesai: ${result.imported.size} masuk, ${result.skippedCount} dilewati",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membaca CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    if (profile.hasPin && isLocked) {
        PinLockScreen(
            profile = profile,
            rateLimiter = rateLimiter,
            onUnlocked = { isLocked = false }
        )
        return
    }

    Scaffold(
        containerColor = CanvasWhite,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TwinCoinLogo(size = 28.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = profile.appTitle,
                            color = InkPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        privacyMode = !privacyMode
                        store.savePrivacyMode(privacyMode)
                    }) {
                        PrivacyEyeIcon(hidden = privacyMode, tint = InkPrimary, size = 20.dp)
                    }
                    IconButton(onClick = { showAuditDialog = true }) {
                        LedgerScrollIcon(tint = InkPrimary, size = 20.dp)
                    }
                    if (profile.hasPin) {
                        IconButton(onClick = { isLocked = true }) {
                            ShieldLockIcon(tint = BrandBlue, size = 20.dp)
                        }
                    }
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Pengaturan", tint = if (showSettings) BrandBlue else InkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CanvasWhite)
            )
        },
        bottomBar = {
            if (!showSettings) {
                NavigationBar(containerColor = SurfaceWhite) {
                    MainTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        val tint = if (selected) BrandBlue else InkMuted
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                when (tab) {
                                    MainTab.HOME -> HomeRoofIcon(tint = tint, size = 21.dp)
                                    MainTab.SAVINGS -> WaveTrendIcon(tint = tint, size = 21.dp)
                                    MainTab.CALENDAR -> CalendarNoteIcon(tint = tint, size = 21.dp)
                                    MainTab.NOTES -> NotePadIcon(tint = tint, size = 21.dp)
                                }
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandBlue,
                                selectedTextColor = BrandBlue,
                                unselectedIconColor = InkMuted,
                                unselectedTextColor = InkMuted,
                                indicatorColor = BrandBlueSoft
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val showFab = !showSettings &&
                currentTab != MainTab.CALENDAR &&
                currentTab != MainTab.NOTES &&
                !(currentTab == MainTab.SAVINGS && savingsSubTab == SavingsSubTab.ANALYTICS)
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        editingTx = null
                        txInitialType = TransactionType.SETOR
                        showTxDialog = true
                    },
                    containerColor = if (currentMode == SavingsMode.SOLO) BrandBlue else BrandOrange,
                    contentColor = SurfaceWhite,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Catat Transaksi")
                }
            }
        }
    ) { padding ->
        if (showSettings) {
            val soloTxCount = if (currentMode == SavingsMode.SOLO) transactions.size else store.loadTransactions(SavingsMode.SOLO).size
            val soloGoalCount = if (currentMode == SavingsMode.SOLO) goals.size else store.loadGoals(SavingsMode.SOLO).size
            val duoTxCount = if (currentMode == SavingsMode.DUO) transactions.size else store.loadTransactions(SavingsMode.DUO).size
            val duoGoalCount = if (currentMode == SavingsMode.DUO) goals.size else store.loadGoals(SavingsMode.DUO).size

            SettingsScreen(
                modifier = Modifier.padding(padding),
                profile = profile,
                currentMode = currentMode,
                privacyMode = privacyMode,
                soloTxCount = soloTxCount,
                soloGoalCount = soloGoalCount,
                duoTxCount = duoTxCount,
                duoGoalCount = duoGoalCount,
                appVersion = appVersion,
                onBack = { showSettings = false },
                onTogglePrivacy = { enabled ->
                    privacyMode = enabled
                    store.savePrivacyMode(enabled)
                },
                onSaveProfile = { updated, pinNote ->
                    profile = updated
                    store.saveProfile(updated)
                    if (pinNote != null) {
                        addAudit("Keamanan", pinNote)
                    } else {
                        addAudit("Profil", "Label pemilik diperbarui (${updated.soloName} / ${updated.partner1Name} & ${updated.partner2Name})")
                    }
                    Toast.makeText(context, "Pengaturan disimpan", Toast.LENGTH_SHORT).show()
                },
                onLockNow = {
                    showSettings = false
                    isLocked = true
                },
                onOpenAuditLog = { showAuditDialog = true },
                onExportPdf = {
                    exportPdfLauncher.launch("support-life-${currentMode.label.lowercase()}-${TabunganLogic.todayIso()}.pdf")
                },
                onExportCsv = {
                    exportCsvLauncher.launch("support-life-${currentMode.label.lowercase()}-${TabunganLogic.todayIso()}.csv")
                },
                onImportCsv = {
                    importCsvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain"))
                },
                onResetCurrentMode = {
                    store.clearModeData(currentMode)
                    transactions.clear()
                    goals.clear()
                    auditLogs.clear()
                    Toast.makeText(context, "Data mode ${currentMode.label} berhasil direset", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            AnimatedContent(
                targetState = currentTab to currentMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_mode_switch"
            ) { (tab, mode) ->
                when (tab) {
                    MainTab.HOME -> HomeScreen(
                        modifier = Modifier.padding(padding),
                        mode = mode,
                        onModeChange = ::switchMode,
                        profile = profile,
                        stats = stats,
                        goals = goals,
                        recentTransactions = transactions.sortedWith(
                            compareByDescending<SavingsTransaction> { it.date }.thenByDescending { it.createdAt }
                        ),
                        calendarNotes = calendarNotes,
                        personalNotes = personalNotes,
                        privacyMode = privacyMode,
                        onTogglePrivacy = {
                            privacyMode = !privacyMode
                            store.savePrivacyMode(privacyMode)
                        },
                        onQuickAdd = { type ->
                            editingTx = null
                            txInitialType = type
                            showTxDialog = true
                        },
                        onOpenGoals = {
                            savingsSubTab = SavingsSubTab.GOALS
                            currentTab = MainTab.SAVINGS
                        },
                        onOpenHistory = {
                            savingsSubTab = SavingsSubTab.HISTORY
                            currentTab = MainTab.SAVINGS
                        },
                        onOpenCalendar = { currentTab = MainTab.CALENDAR },
                        onOpenNotes = { currentTab = MainTab.NOTES },
                        onEditTx = { tx ->
                            editingTx = tx
                            showTxDialog = true
                        },
                        onDeleteTx = { tx -> deletingTx = tx }
                    )

                    MainTab.SAVINGS -> SavingsHubScreen(
                        modifier = Modifier.padding(padding),
                        subTab = savingsSubTab,
                        onSubTabChange = { savingsSubTab = it },
                        mode = mode,
                        onModeChange = ::switchMode,
                        transactions = transactions,
                        goals = goals,
                        profile = profile,
                        privacyMode = privacyMode,
                        onAddTx = {
                            editingTx = null
                            txInitialType = TransactionType.SETOR
                            showTxDialog = true
                        },
                        onEditTx = { tx ->
                            editingTx = tx
                            showTxDialog = true
                        },
                        onDeleteTx = { tx -> deletingTx = tx },
                        onAddGoal = {
                            editingGoal = null
                            showGoalDialog = true
                        },
                        onEditGoal = { goal ->
                            editingGoal = goal
                            showGoalDialog = true
                        },
                        onDeleteGoal = { goal -> deletingGoal = goal },
                        onExportPdf = {
                            exportPdfLauncher.launch("support-life-${mode.label.lowercase()}-${TabunganLogic.todayIso()}.pdf")
                        },
                        onExportCsv = {
                            exportCsvLauncher.launch("support-life-${mode.label.lowercase()}-${TabunganLogic.todayIso()}.csv")
                        },
                        onImportCsv = {
                            importCsvLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain"))
                        }
                    )

                    MainTab.CALENDAR -> CalendarScreen(
                        modifier = Modifier.padding(padding),
                        notes = calendarNotes,
                        selectedDate = selectedCalendarDate,
                        onSelectDate = { selectedCalendarDate = it },
                        onAddNote = { date ->
                            selectedCalendarDate = date
                            editingNote = null
                            showNoteDialog = true
                        },
                        onEditNote = { note ->
                            editingNote = note
                            showNoteDialog = true
                        },
                        onDeleteNote = { note -> deletingNote = note }
                    )

                    MainTab.NOTES -> NotesScreen(
                        modifier = Modifier.padding(padding),
                        notes = personalNotes,
                        onAddNote = {
                            editingPersonalNote = null
                            showPersonalNoteDialog = true
                        },
                        onEditNote = { note ->
                            editingPersonalNote = note
                            showPersonalNoteDialog = true
                        },
                        onTogglePin = { note ->
                            val idx = personalNotes.indexOfFirst { it.id == note.id }
                            if (idx >= 0) {
                                personalNotes[idx] = note.copy(pinned = !note.pinned)
                                store.savePersonalNotes(personalNotes)
                            }
                        },
                        onDeleteNote = { note -> deletingPersonalNote = note }
                    )
                }
            }
        }
    }

    if (showTxDialog) {
        TransactionDialog(
            mode = currentMode,
            initialType = txInitialType,
            existing = editingTx,
            profile = profile,
            onDismiss = { showTxDialog = false; editingTx = null },
            onSave = { type, partnerId, amount, category, note, date ->
                val currentEditing = editingTx
                val ownerLabel = profile.ownerName(currentMode, partnerId)
                if (currentEditing == null) {
                    val created = SavingsTransaction(
                        id = UUID.randomUUID().toString(),
                        date = date,
                        partnerId = partnerId,
                        type = type,
                        amount = amount,
                        category = category,
                        note = note,
                        createdAt = System.currentTimeMillis()
                    )
                    transactions.add(0, created)
                    addAudit("Tambah", "$ownerLabel · ${type.label} ${TabunganLogic.formatRupiah(amount)} ($category)")
                } else {
                    val idx = transactions.indexOfFirst { it.id == currentEditing.id }
                    if (idx >= 0) {
                        transactions[idx] = currentEditing.copy(
                            type = type,
                            partnerId = partnerId,
                            amount = amount,
                            category = category,
                            note = note,
                            date = date
                        )
                        addAudit("Ubah", "Transaksi ${type.label} ${TabunganLogic.formatRupiah(amount)} diperbarui")
                    }
                }
                store.saveTransactions(currentMode, transactions)
                showTxDialog = false
                editingTx = null
                Toast.makeText(context, "Transaksi tersimpan (${currentMode.label})", Toast.LENGTH_SHORT).show()
            }
        )
    }

    deletingTx?.let { target ->
        ConfirmDeleteDialog(
            title = "Hapus Transaksi",
            message = "Yakin ingin menghapus ${target.type.label} ${TabunganLogic.formatRupiah(target.amount, privacyMode)}?",
            onDismiss = { deletingTx = null },
            onConfirm = {
                transactions.removeAll { it.id == target.id }
                store.saveTransactions(currentMode, transactions)
                addAudit("Hapus", "${target.type.label} ${TabunganLogic.formatRupiah(target.amount)} (${target.category}) dihapus")
                deletingTx = null
            }
        )
    }

    if (showGoalDialog) {
        GoalDialog(
            existing = editingGoal,
            onDismiss = { showGoalDialog = false; editingGoal = null },
            onSave = { title, targetAmount, savedAmount, deadline, category ->
                val current = editingGoal
                if (current == null) {
                    goals.add(
                        0,
                        SavingsGoal(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            targetAmount = targetAmount,
                            savedAmount = savedAmount,
                            deadline = deadline,
                            category = category
                        )
                    )
                    addAudit("Target", "Target baru (${currentMode.label}): $title (${TabunganLogic.formatRupiah(targetAmount)})")
                } else {
                    val idx = goals.indexOfFirst { it.id == current.id }
                    if (idx >= 0) {
                        goals[idx] = current.copy(
                            title = title,
                            targetAmount = targetAmount,
                            savedAmount = savedAmount,
                            deadline = deadline,
                            category = category
                        )
                        addAudit("Target", "Target diperbarui: $title")
                    }
                }
                store.saveGoals(currentMode, goals)
                showGoalDialog = false
                editingGoal = null
            }
        )
    }

    deletingGoal?.let { target ->
        ConfirmDeleteDialog(
            title = "Hapus Target",
            message = "Yakin ingin menghapus target \"${target.title}\"?",
            onDismiss = { deletingGoal = null },
            onConfirm = {
                goals.removeAll { it.id == target.id }
                store.saveGoals(currentMode, goals)
                addAudit("Target", "Target dihapus: ${target.title}")
                deletingGoal = null
            }
        )
    }

    if (showNoteDialog) {
        CalendarNoteDialog(
            defaultDate = selectedCalendarDate.toString(),
            existing = editingNote,
            onDismiss = { showNoteDialog = false; editingNote = null },
            onSave = { date, title, description, colorTag ->
                val current = editingNote
                if (current == null) {
                    calendarNotes.add(
                        0,
                        CalendarNote(
                            id = UUID.randomUUID().toString(),
                            date = date,
                            title = title,
                            description = description,
                            colorTag = colorTag,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    val idx = calendarNotes.indexOfFirst { it.id == current.id }
                    if (idx >= 0) {
                        calendarNotes[idx] = current.copy(
                            date = date,
                            title = title,
                            description = description,
                            colorTag = colorTag
                        )
                    }
                }
                store.saveCalendarNotes(calendarNotes)
                runCatching { LocalDate.parse(date) }.getOrNull()?.let { selectedCalendarDate = it }
                showNoteDialog = false
                editingNote = null
                Toast.makeText(context, "Catatan kalender disimpan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    deletingNote?.let { target ->
        ConfirmDeleteDialog(
            title = "Hapus Catatan Kalender",
            message = "Yakin ingin menghapus catatan \"${target.title}\" pada ${TabunganLogic.formatDisplayDate(target.date)}?",
            onDismiss = { deletingNote = null },
            onConfirm = {
                calendarNotes.removeAll { it.id == target.id }
                store.saveCalendarNotes(calendarNotes)
                deletingNote = null
            }
        )
    }

    if (showPersonalNoteDialog) {
        PersonalNoteDialog(
            existing = editingPersonalNote,
            onDismiss = { showPersonalNoteDialog = false; editingPersonalNote = null },
            onSave = { title, content, pinned, colorTag ->
                val current = editingPersonalNote
                val now = System.currentTimeMillis()
                if (current == null) {
                    personalNotes.add(
                        0,
                        PersonalNote(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            content = content,
                            pinned = pinned,
                            colorTag = colorTag,
                            updatedAt = now
                        )
                    )
                } else {
                    val idx = personalNotes.indexOfFirst { it.id == current.id }
                    if (idx >= 0) {
                        personalNotes[idx] = current.copy(
                            title = title,
                            content = content,
                            pinned = pinned,
                            colorTag = colorTag,
                            updatedAt = now
                        )
                    }
                }
                store.savePersonalNotes(personalNotes)
                showPersonalNoteDialog = false
                editingPersonalNote = null
                Toast.makeText(context, "Catatan disimpan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    deletingPersonalNote?.let { target ->
        ConfirmDeleteDialog(
            title = "Hapus Catatan",
            message = "Yakin ingin menghapus catatan \"${target.title}\"?",
            onDismiss = { deletingPersonalNote = null },
            onConfirm = {
                personalNotes.removeAll { it.id == target.id }
                store.savePersonalNotes(personalNotes)
                deletingPersonalNote = null
            }
        )
    }

    if (showAuditDialog) {
        AuditLogDialog(logs = auditLogs, onDismiss = { showAuditDialog = false })
    }
}
