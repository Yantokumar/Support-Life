package com.yanto.catatanuang.model

enum class SavingsMode(val label: String, val tagline: String) {
    SOLO("Pribadi", "Tabungan Pribadi"),
    DUO("Bersama", "Tabungan Bersama")
}

enum class TransactionType(val label: String) {
    SETOR("Setor"),
    TARIK("Tarik")
}

enum class TimeRange(val label: String, val months: Int?) {
    ALL("Semua", null),
    ONE_YEAR("1 Thn", 12),
    SIX_MONTHS("6 Bln", 6)
}

enum class MainTab(val label: String) {
    HOME("Beranda"),
    SAVINGS("Tabungan"),
    CALENDAR("Kalender"),
    NOTES("Catatan")
}

enum class SavingsSubTab(val label: String) {
    HISTORY("Riwayat"),
    GOALS("Target"),
    ANALYTICS("Grafik")
}

enum class NoteColorTag(val label: String) {
    BLUE("Acara"),
    ORANGE("Pengingat"),
    RED("Penting")
}

data class CalendarNote(
    val id: String,
    val date: String, // yyyy-MM-dd
    val title: String,
    val description: String,
    val colorTag: NoteColorTag,
    val createdAt: Long
)

data class PersonalNote(
    val id: String,
    val title: String,
    val content: String,
    val pinned: Boolean,
    val colorTag: NoteColorTag,
    val updatedAt: Long
)

data class SavingsTransaction(
    val id: String,
    val date: String, // yyyy-MM-dd
    val partnerId: Int, // 1 or 2 (in SOLO mode always 1)
    val type: TransactionType,
    val amount: Long,
    val category: String,
    val note: String,
    val createdAt: Long
)

data class SavingsGoal(
    val id: String,
    val title: String,
    val targetAmount: Long,
    val savedAmount: Long,
    val deadline: String,
    val category: String
)

data class CoupleProfile(
    val soloName: String = "Pribadi",
    val partner1Name: String = "Pasangan 1",
    val partner2Name: String = "Pasangan 2",
    val pinHash: String = "",
    val pinSalt: String = ""
) {
    val appTitle: String get() = APP_TITLE
    val hasPin: Boolean get() = pinHash.isNotEmpty() && pinSalt.isNotEmpty()

    fun ownerName(mode: SavingsMode, partnerId: Int): String =
        if (mode == SavingsMode.SOLO) soloName
        else if (partnerId == 2) partner2Name else partner1Name

    fun partnerName(partnerId: Int): String = if (partnerId == 2) partner2Name else partner1Name

    companion object {
        const val APP_TITLE = "Support Life"
    }
}

data class AuditEntry(
    val id: String,
    val timestamp: Long,
    val action: String,
    val detail: String
)

data class SummaryStats(
    val totalBalance: Long,
    val totalSetor: Long,
    val totalTarik: Long,
    val partner1Net: Long,
    val partner2Net: Long,
    val partner1Setor: Long,
    val partner2Setor: Long
)

data class MonthlyPoint(
    val monthKey: String,
    val label: String,
    val setor: Long,
    val tarik: Long,
    val cumulativeBalance: Long
)

data class CsvImportResult(
    val imported: List<SavingsTransaction>,
    val skippedCount: Int,
    val errorMessage: String? = null
)
