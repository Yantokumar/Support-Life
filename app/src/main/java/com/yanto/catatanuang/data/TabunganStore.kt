package com.yanto.catatanuang.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.yanto.catatanuang.model.AuditEntry
import com.yanto.catatanuang.model.CalendarNote
import com.yanto.catatanuang.model.CoupleProfile
import com.yanto.catatanuang.model.CsvImportResult
import com.yanto.catatanuang.model.MonthlyPoint
import com.yanto.catatanuang.model.NoteColorTag
import com.yanto.catatanuang.model.PersonalNote
import com.yanto.catatanuang.model.SavingsGoal
import com.yanto.catatanuang.model.SavingsMode
import com.yanto.catatanuang.model.SavingsTransaction
import com.yanto.catatanuang.model.SummaryStats
import com.yanto.catatanuang.model.TimeRange
import com.yanto.catatanuang.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

object TabunganLogic {
    const val MAX_IMPORT_ROWS = 500
    const val MAX_PIN_ATTEMPTS = 5
    const val PIN_LOCK_MILLIS = 60_000L

    private val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")

    val defaultCategories = listOf(
        "Tabungan Rutin",
        "Dana Darurat",
        "Liburan",
        "Rumah",
        "Belanja",
        "Tagihan",
        "Lainnya"
    )

    fun formatRupiah(amount: Long, masked: Boolean = false): String {
        if (masked) return "Rp ••••••"
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        val absFormatted = formatter.format(kotlin.math.abs(amount)).replace('\u00A0', ' ')
        return if (amount < 0L) "- $absFormatted" else absFormatted
    }

    fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun formatDisplayDate(isoDate: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            val parsed = parser.parse(isoDate) ?: return isoDate
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(parsed)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatAuditTime(millis: Long): String {
        return SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(millis))
    }

    fun isValidIsoDate(value: String): Boolean {
        if (!dateRegex.matches(value)) return false
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            parser.parse(value) != null
        } catch (_: Exception) {
            false
        }
    }

    fun computeSummary(transactions: List<SavingsTransaction>): SummaryStats {
        var totalSetor = 0L
        var totalTarik = 0L
        var p1Setor = 0L
        var p1Tarik = 0L
        var p2Setor = 0L
        var p2Tarik = 0L

        for (tx in transactions) {
            when (tx.type) {
                TransactionType.SETOR -> {
                    totalSetor += tx.amount
                    if (tx.partnerId == 2) p2Setor += tx.amount else p1Setor += tx.amount
                }
                TransactionType.TARIK -> {
                    totalTarik += tx.amount
                    if (tx.partnerId == 2) p2Tarik += tx.amount else p1Tarik += tx.amount
                }
            }
        }

        return SummaryStats(
            totalBalance = totalSetor - totalTarik,
            totalSetor = totalSetor,
            totalTarik = totalTarik,
            partner1Net = p1Setor - p1Tarik,
            partner2Net = p2Setor - p2Tarik,
            partner1Setor = p1Setor,
            partner2Setor = p2Setor
        )
    }

    fun filterTransactions(
        transactions: List<SavingsTransaction>,
        query: String,
        partnerFilter: Int?, // null = all, 1 = p1, 2 = p2
        typeFilter: TransactionType?,
        categoryFilter: String?
    ): List<SavingsTransaction> {
        val q = query.trim().lowercase(Locale.ROOT)
        return transactions.filter { tx ->
            val matchesQuery = q.isEmpty() ||
                tx.note.lowercase(Locale.ROOT).contains(q) ||
                tx.category.lowercase(Locale.ROOT).contains(q) ||
                tx.date.contains(q)
            val matchesPartner = partnerFilter == null || tx.partnerId == partnerFilter
            val matchesType = typeFilter == null || tx.type == typeFilter
            val matchesCat = categoryFilter.isNullOrEmpty() || tx.category == categoryFilter
            matchesQuery && matchesPartner && matchesType && matchesCat
        }.sortedWith(compareByDescending<SavingsTransaction> { it.date }.thenByDescending { it.createdAt })
    }

    fun buildTransactionSeries(
        transactions: List<SavingsTransaction>,
        range: TimeRange,
        nowMillis: Long = System.currentTimeMillis()
    ): List<MonthlyPoint> {
        if (transactions.isEmpty()) return emptyList()

        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val labelFmt = SimpleDateFormat("dd MMM", Locale("id", "ID"))

        val sorted = transactions.sortedWith(compareBy<SavingsTransaction> { it.date }.thenBy { it.createdAt })
        var runningBalance = 0L
        val allPoints = sorted.map { tx ->
            val setor = if (tx.type == TransactionType.SETOR) tx.amount else 0L
            val tarik = if (tx.type == TransactionType.TARIK) tx.amount else 0L
            runningBalance += (setor - tarik)
            val label = runCatching {
                parser.parse(tx.date)?.let { labelFmt.format(it) } ?: tx.date
            }.getOrDefault(tx.date)
            MonthlyPoint(
                monthKey = tx.date,
                label = label,
                setor = setor,
                tarik = tarik,
                cumulativeBalance = runningBalance
            )
        }

        val limitMonths = range.months ?: return allPoints
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.MONTH, -limitMonths)
        }
        val cutoffKey = isoFmt.format(cal.time)
        return allPoints.filter { it.monthKey >= cutoffKey }
    }

    fun buildWeeklySeries(
        transactions: List<SavingsTransaction>,
        range: TimeRange,
        nowMillis: Long = System.currentTimeMillis()
    ): List<MonthlyPoint> {
        if (transactions.isEmpty()) return emptyList()

        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val weekFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val labelFmt = SimpleDateFormat("dd MMM", Locale("id", "ID"))

        val grouped = linkedMapOf<String, Pair<Long, Long>>()
        val sorted = transactions.sortedBy { it.date }
        for (tx in sorted) {
            val parsed = runCatching { parser.parse(tx.date) }.getOrNull() ?: continue
            val cal = Calendar.getInstance().apply {
                time = parsed
                firstDayOfWeek = Calendar.MONDAY
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }
            val weekKey = weekFmt.format(cal.time)
            val prev = grouped[weekKey] ?: (0L to 0L)
            grouped[weekKey] = when (tx.type) {
                TransactionType.SETOR -> (prev.first + tx.amount) to prev.second
                TransactionType.TARIK -> prev.first to (prev.second + tx.amount)
            }
        }

        var runningBalance = 0L
        val allPoints = grouped.entries.sortedBy { it.key }.map { (key, sums) ->
            runningBalance += (sums.first - sums.second)
            val label = runCatching {
                parser.parse(key)?.let { labelFmt.format(it) } ?: key
            }.getOrDefault(key)
            MonthlyPoint(
                monthKey = key,
                label = label,
                setor = sums.first,
                tarik = sums.second,
                cumulativeBalance = runningBalance
            )
        }

        val limitMonths = range.months ?: return allPoints
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.MONTH, -limitMonths)
        }
        val cutoffKey = weekFmt.format(cal.time)
        return allPoints.filter { it.monthKey >= cutoffKey }
    }

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPinConstantTime(inputPin: String, expectedHash: String, salt: String): Boolean {
        if (expectedHash.isEmpty() || salt.isEmpty()) return false
        val actualBytes = hashPin(inputPin, salt).toByteArray(Charsets.UTF_8)
        val expectedBytes = expectedHash.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(actualBytes, expectedBytes)
    }

    fun exportTransactionsToCsv(
        transactions: List<SavingsTransaction>,
        profile: CoupleProfile,
        mode: SavingsMode = SavingsMode.DUO
    ): String {
        val sb = StringBuilder()
        sb.appendLine("Tanggal,Pemilik,Tipe,Nominal,Kategori,Keterangan")
        val sorted = transactions.sortedWith(compareByDescending<SavingsTransaction> { it.date }.thenByDescending { it.createdAt })
        for (tx in sorted) {
            val owner = escapeCsv(profile.ownerName(mode, tx.partnerId))
            val type = tx.type.label
            val category = escapeCsv(tx.category)
            val note = escapeCsv(tx.note)
            sb.appendLine("${tx.date},$owner,$type,${tx.amount},$category,$note")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        if (!needsQuotes) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }

    fun parseTransactionsFromCsv(csvText: String, profile: CoupleProfile): CsvImportResult {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return CsvImportResult(emptyList(), 0, "File CSV kosong.")
        }

        val startIndex = if (lines.first().lowercase(Locale.ROOT).contains("tanggal")) 1 else 0
        val dataLines = lines.drop(startIndex)
        if (dataLines.size > MAX_IMPORT_ROWS) {
            return CsvImportResult(
                emptyList(),
                dataLines.size,
                "Maksimal $MAX_IMPORT_ROWS baris per impor (ditemukan ${dataLines.size} baris)."
            )
        }

        val imported = mutableListOf<SavingsTransaction>()
        var skipped = 0
        val baseTime = System.currentTimeMillis()

        dataLines.forEachIndexed { idx, line ->
            val cols = parseCsvLine(line)
            if (cols.size < 4) {
                skipped++
                return@forEachIndexed
            }
            val dateStr = cols[0].trim()
            val partnerRaw = cols[1].trim().lowercase(Locale.ROOT)
            val typeRaw = cols[2].trim().lowercase(Locale.ROOT)
            val amountRaw = cols[3].trim().replace(".", "").replace(",", "").replace("Rp", "", ignoreCase = true).trim()
            val category = cols.getOrNull(4)?.trim().orEmpty().ifEmpty { "Lainnya" }
            val note = cols.getOrNull(5)?.trim().orEmpty()

            val amount = amountRaw.toLongOrNull()
            val type = when (typeRaw) {
                "setor", "masuk", "pemasukan", "deposit" -> TransactionType.SETOR
                "tarik", "keluar", "pengeluaran", "withdraw" -> TransactionType.TARIK
                else -> null
            }

            if (!isValidIsoDate(dateStr) || amount == null || amount <= 0 || type == null) {
                skipped++
                return@forEachIndexed
            }

            val partnerId = when {
                partnerRaw == "2" || partnerRaw == profile.partner2Name.lowercase(Locale.ROOT) -> 2
                else -> 1
            }

            imported.add(
                SavingsTransaction(
                    id = UUID.randomUUID().toString(),
                    date = dateStr,
                    partnerId = partnerId,
                    type = type,
                    amount = amount,
                    category = category,
                    note = note,
                    createdAt = baseTime + idx
                )
            )
        }

        return CsvImportResult(imported, skipped, null)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString())
                current.clear()
            } else {
                current.append(ch)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}

class PinRateLimiter {
    private var failedAttempts = 0
    private var lockedUntilMillis = 0L

    fun remainingLockSeconds(nowMillis: Long = System.currentTimeMillis()): Int {
        if (nowMillis >= lockedUntilMillis) return 0
        return ((lockedUntilMillis - nowMillis + 999L) / 1000L).toInt()
    }

    fun recordFailure(nowMillis: Long = System.currentTimeMillis()): Int {
        if (nowMillis < lockedUntilMillis) return remainingLockSeconds(nowMillis)
        failedAttempts++
        if (failedAttempts >= TabunganLogic.MAX_PIN_ATTEMPTS) {
            lockedUntilMillis = nowMillis + TabunganLogic.PIN_LOCK_MILLIS
            failedAttempts = 0
        }
        return remainingLockSeconds(nowMillis)
    }

    fun reset() {
        failedAttempts = 0
        lockedUntilMillis = 0L
    }

    fun attemptsRemaining(): Int = (TabunganLogic.MAX_PIN_ATTEMPTS - failedAttempts).coerceAtLeast(0)
}

class TabunganStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tabungan_berdua_offline", Context.MODE_PRIVATE)

    private fun modeKey(base: String, mode: SavingsMode): String = "${base}_${mode.name.lowercase()}"

    fun loadTransactions(mode: SavingsMode): List<SavingsTransaction> {
        val raw = prefs.getString(modeKey(KEY_TRANSACTIONS, mode), null)
            ?: (if (mode == SavingsMode.DUO) prefs.getString(KEY_TRANSACTIONS, null) else null)
            ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                SavingsTransaction(
                    id = obj.getString("id"),
                    date = obj.getString("date"),
                    partnerId = obj.optInt("partnerId", 1),
                    type = TransactionType.valueOf(obj.getString("type")),
                    amount = obj.getLong("amount"),
                    category = obj.optString("category", "Lainnya"),
                    note = obj.optString("note", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTransactions(mode: SavingsMode, items: List<SavingsTransaction>) {
        val arr = JSONArray()
        for (tx in items) {
            arr.put(
                JSONObject()
                    .put("id", tx.id)
                    .put("date", tx.date)
                    .put("partnerId", tx.partnerId)
                    .put("type", tx.type.name)
                    .put("amount", tx.amount)
                    .put("category", tx.category)
                    .put("note", tx.note)
                    .put("createdAt", tx.createdAt)
            )
        }
        prefs.edit().putString(modeKey(KEY_TRANSACTIONS, mode), arr.toString()).apply()
    }

    fun loadGoals(mode: SavingsMode): List<SavingsGoal> {
        val raw = prefs.getString(modeKey(KEY_GOALS, mode), null)
            ?: (if (mode == SavingsMode.DUO) prefs.getString(KEY_GOALS, null) else null)
            ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                SavingsGoal(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    targetAmount = obj.getLong("targetAmount"),
                    savedAmount = obj.optLong("savedAmount", 0L),
                    deadline = obj.optString("deadline", ""),
                    category = obj.optString("category", "Tabungan Rutin")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveGoals(mode: SavingsMode, items: List<SavingsGoal>) {
        val arr = JSONArray()
        for (goal in items) {
            arr.put(
                JSONObject()
                    .put("id", goal.id)
                    .put("title", goal.title)
                    .put("targetAmount", goal.targetAmount)
                    .put("savedAmount", goal.savedAmount)
                    .put("deadline", goal.deadline)
                    .put("category", goal.category)
            )
        }
        prefs.edit().putString(modeKey(KEY_GOALS, mode), arr.toString()).apply()
    }

    fun loadProfile(): CoupleProfile {
        val raw = prefs.getString(KEY_PROFILE, null) ?: return CoupleProfile()
        return try {
            val obj = JSONObject(raw)
            CoupleProfile(
                soloName = obj.optString("soloName", "Pribadi"),
                partner1Name = obj.optString("partner1Name", "Pasangan 1"),
                partner2Name = obj.optString("partner2Name", "Pasangan 2"),
                pinHash = obj.optString("pinHash", ""),
                pinSalt = obj.optString("pinSalt", "")
            )
        } catch (_: Exception) {
            CoupleProfile()
        }
    }

    fun saveProfile(profile: CoupleProfile) {
        val obj = JSONObject()
            .put("soloName", profile.soloName)
            .put("partner1Name", profile.partner1Name)
            .put("partner2Name", profile.partner2Name)
            .put("pinHash", profile.pinHash)
            .put("pinSalt", profile.pinSalt)
        prefs.edit().putString(KEY_PROFILE, obj.toString()).apply()
    }

    fun clearModeData(mode: SavingsMode) {
        prefs.edit()
            .remove(modeKey(KEY_TRANSACTIONS, mode))
            .remove(modeKey(KEY_GOALS, mode))
            .remove(modeKey(KEY_AUDIT, mode))
            .apply()
        if (mode == SavingsMode.DUO) {
            prefs.edit()
                .remove(KEY_TRANSACTIONS)
                .remove(KEY_GOALS)
                .remove(KEY_AUDIT)
                .apply()
        }
    }

    fun loadAuditLogs(mode: SavingsMode): List<AuditEntry> {
        val raw = prefs.getString(modeKey(KEY_AUDIT, mode), null)
            ?: (if (mode == SavingsMode.DUO) prefs.getString(KEY_AUDIT, null) else null)
            ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                AuditEntry(
                    id = obj.getString("id"),
                    timestamp = obj.getLong("timestamp"),
                    action = obj.getString("action"),
                    detail = obj.getString("detail")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveAuditLogs(mode: SavingsMode, logs: List<AuditEntry>) {
        val arr = JSONArray()
        for (entry in logs.take(200)) {
            arr.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("timestamp", entry.timestamp)
                    .put("action", entry.action)
                    .put("detail", entry.detail)
            )
        }
        prefs.edit().putString(modeKey(KEY_AUDIT, mode), arr.toString()).apply()
    }

    fun loadLastMode(): SavingsMode {
        val raw = prefs.getString(KEY_LAST_MODE, SavingsMode.DUO.name)
        return runCatching { SavingsMode.valueOf(raw ?: SavingsMode.DUO.name) }.getOrDefault(SavingsMode.DUO)
    }

    fun saveLastMode(mode: SavingsMode) {
        prefs.edit().putString(KEY_LAST_MODE, mode.name).apply()
    }

    fun loadCalendarNotes(): List<CalendarNote> {
        val raw = prefs.getString(KEY_CALENDAR_NOTES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val tag = runCatching { NoteColorTag.valueOf(obj.optString("colorTag", NoteColorTag.BLUE.name)) }
                    .getOrDefault(NoteColorTag.BLUE)
                CalendarNote(
                    id = obj.getString("id"),
                    date = obj.getString("date"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    colorTag = tag,
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCalendarNotes(items: List<CalendarNote>) {
        val arr = JSONArray()
        for (note in items) {
            arr.put(
                JSONObject()
                    .put("id", note.id)
                    .put("date", note.date)
                    .put("title", note.title)
                    .put("description", note.description)
                    .put("colorTag", note.colorTag.name)
                    .put("createdAt", note.createdAt)
            )
        }
        prefs.edit().putString(KEY_CALENDAR_NOTES, arr.toString()).apply()
    }

    fun loadPersonalNotes(): List<PersonalNote> {
        val raw = prefs.getString(KEY_PERSONAL_NOTES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val tag = runCatching { NoteColorTag.valueOf(obj.optString("colorTag", NoteColorTag.BLUE.name)) }
                    .getOrDefault(NoteColorTag.BLUE)
                PersonalNote(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    content = obj.optString("content", ""),
                    pinned = obj.optBoolean("pinned", false),
                    colorTag = tag,
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun savePersonalNotes(items: List<PersonalNote>) {
        val arr = JSONArray()
        for (note in items) {
            arr.put(
                JSONObject()
                    .put("id", note.id)
                    .put("title", note.title)
                    .put("content", note.content)
                    .put("pinned", note.pinned)
                    .put("colorTag", note.colorTag.name)
                    .put("updatedAt", note.updatedAt)
            )
        }
        prefs.edit().putString(KEY_PERSONAL_NOTES, arr.toString()).apply()
    }

    fun loadPrivacyMode(): Boolean = prefs.getBoolean(KEY_PRIVACY, false)
    fun savePrivacyMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY, enabled).apply()
    }

    fun generatePdfReport(
        outputStream: OutputStream,
        profile: CoupleProfile,
        mode: SavingsMode,
        stats: SummaryStats,
        goals: List<SavingsGoal>,
        transactions: List<SavingsTransaction>
    ) {
        val doc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f

        val titlePaint = Paint().apply {
            color = AndroidColor.rgb(29, 78, 216)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint().apply {
            color = AndroidColor.rgb(100, 116, 139)
            textSize = 11f
        }
        val sectionPaint = Paint().apply {
            color = AndroidColor.rgb(15, 23, 42)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            color = AndroidColor.rgb(30, 41, 59)
            textSize = 10.5f
        }
        val linePaint = Paint().apply {
            color = AndroidColor.rgb(226, 232, 240)
            strokeWidth = 1f
        }

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = 52f

        fun newPage() {
            doc.finishPage(page)
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = 52f
        }

        canvas.drawText("${profile.appTitle} (${mode.label})", margin, y, titlePaint)
        y += 18f
        canvas.drawText("Laporan ${mode.tagline} · Dicetak ${TabunganLogic.formatDisplayDate(TabunganLogic.todayIso())}", margin, y, subtitlePaint)
        y += 16f
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 22f

        canvas.drawText("Ringkasan Saldo", margin, y, sectionPaint)
        y += 18f
        canvas.drawText("Total Saldo: ${TabunganLogic.formatRupiah(stats.totalBalance)}", margin, y, bodyPaint)
        y += 16f
        canvas.drawText("Total Setor: ${TabunganLogic.formatRupiah(stats.totalSetor)}   |   Total Tarik: ${TabunganLogic.formatRupiah(stats.totalTarik)}", margin, y, bodyPaint)
        if (mode == SavingsMode.DUO) {
            y += 16f
            canvas.drawText("${profile.partner1Name}: ${TabunganLogic.formatRupiah(stats.partner1Net)}   |   ${profile.partner2Name}: ${TabunganLogic.formatRupiah(stats.partner2Net)}", margin, y, bodyPaint)
        }
        y += 24f

        if (goals.isNotEmpty()) {
            canvas.drawText("Target Tabungan (${goals.size})", margin, y, sectionPaint)
            y += 18f
            for (goal in goals) {
                if (y > pageHeight - 60f) newPage()
                val pct = if (goal.targetAmount > 0) ((goal.savedAmount * 100) / goal.targetAmount).coerceIn(0, 100) else 0
                val dl = if (goal.deadline.isNotBlank()) " · Tenggat ${TabunganLogic.formatDisplayDate(goal.deadline)}" else ""
                canvas.drawText("• ${goal.title}: ${TabunganLogic.formatRupiah(goal.savedAmount)} / ${TabunganLogic.formatRupiah(goal.targetAmount)} ($pct%)$dl", margin, y, bodyPaint)
                y += 15f
            }
            y += 14f
        }

        canvas.drawText("Riwayat Transaksi (${transactions.size})", margin, y, sectionPaint)
        y += 18f
        val sorted = transactions.sortedWith(compareByDescending<SavingsTransaction> { it.date }.thenByDescending { it.createdAt })
        for (tx in sorted) {
            if (y > pageHeight - 50f) newPage()
            val sign = if (tx.type == TransactionType.SETOR) "+" else "-"
            val owner = profile.ownerName(mode, tx.partnerId)
            val noteText = if (tx.note.isNotBlank()) " (${tx.note.take(28)})" else ""
            canvas.drawText("${tx.date} | $owner | ${tx.type.label} | $sign${TabunganLogic.formatRupiah(tx.amount)} | ${tx.category}$noteText", margin, y, bodyPaint)
            y += 15f
        }

        doc.finishPage(page)
        doc.writeTo(outputStream)
        doc.close()
    }

    companion object {
        private const val KEY_TRANSACTIONS = "transactions_json"
        private const val KEY_GOALS = "goals_json"
        private const val KEY_PROFILE = "profile_json"
        private const val KEY_AUDIT = "audit_json"
        private const val KEY_PRIVACY = "privacy_mode"
        private const val KEY_LAST_MODE = "last_savings_mode"
        private const val KEY_CALENDAR_NOTES = "calendar_notes_json"
        private const val KEY_PERSONAL_NOTES = "personal_notes_json"
    }
}
