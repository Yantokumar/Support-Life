package com.yanto.catatanuang

import com.yanto.catatanuang.data.PinRateLimiter
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.model.CoupleProfile
import com.yanto.catatanuang.model.SavingsTransaction
import com.yanto.catatanuang.model.TimeRange
import com.yanto.catatanuang.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TabunganLogicTest {

    @Test
    fun computeSummary_calculatesCoupleTotalsCorrectly() {
        val txs = listOf(
            SavingsTransaction("1", "2026-01-10", 1, TransactionType.SETOR, 500_000, "Gaji", "", 1L),
            SavingsTransaction("2", "2026-01-11", 2, TransactionType.SETOR, 300_000, "Bonus", "", 2L),
            SavingsTransaction("3", "2026-01-12", 1, TransactionType.TARIK, 100_000, "Belanja", "", 3L)
        )
        val summary = TabunganLogic.computeSummary(txs)
        assertEquals(700_000L, summary.totalBalance)
        assertEquals(800_000L, summary.totalSetor)
        assertEquals(100_000L, summary.totalTarik)
        assertEquals(400_000L, summary.partner1Net)
        assertEquals(300_000L, summary.partner2Net)
        assertTrue(TabunganLogic.formatRupiah(-30_000L).startsWith("- "))
    }

    @Test
    fun pinVerificationAndRateLimiter_enforcesConstantTimeAndFiveAttemptLock() {
        val salt = "abcdef123456"
        val hash = TabunganLogic.hashPin("2468", salt)
        assertTrue(TabunganLogic.verifyPinConstantTime("2468", hash, salt))
        assertFalse(TabunganLogic.verifyPinConstantTime("0000", hash, salt))

        val limiter = PinRateLimiter()
        val t0 = 1_000_000L
        repeat(4) { idx ->
            assertEquals(0, limiter.recordFailure(t0 + idx * 1000L))
        }
        val lockedSecs = limiter.recordFailure(t0 + 4000L)
        assertEquals(60, lockedSecs)
        assertTrue(limiter.remainingLockSeconds(t0 + 34_000L) > 0)
        assertEquals(0, limiter.remainingLockSeconds(t0 + 65_000L))
    }

    @Test
    fun csvExportAndImport_roundTripsValidRowsAndSkipsInvalid() {
        val profile = CoupleProfile(partner1Name = "Andi", partner2Name = "Budi")
        val csv = """
            Tanggal,Pasangan,Tipe,Nominal,Kategori,Keterangan
            2026-02-01,Andi,Setor,250000,Liburan,"Tiket, Pesawat"
            2026-02-02,Budi,Tarik,50000,Lainnya,Makan
            bad-date,Andi,Setor,10000,Lainnya,Invalid
            2026-02-03,Andi,Setor,0,Lainnya,ZeroAmount
        """.trimIndent()

        val result = TabunganLogic.parseTransactionsFromCsv(csv, profile)
        assertNull(result.errorMessage)
        assertEquals(2, result.imported.size)
        assertEquals(2, result.skippedCount)
        assertEquals(1, result.imported[0].partnerId)
        assertEquals("Tiket, Pesawat", result.imported[0].note)
        assertEquals(2, result.imported[1].partnerId)
        assertEquals(TransactionType.TARIK, result.imported[1].type)

        val exported = TabunganLogic.exportTransactionsToCsv(result.imported, profile)
        assertTrue(exported.contains("\"Tiket, Pesawat\""))
    }

    @Test
    fun csvImport_rejectsMoreThan500Rows() {
        val header = "Tanggal,Pasangan,Tipe,Nominal,Kategori,Keterangan\n"
        val rows = (1..501).joinToString("\n") { "2026-01-01,1,Setor,1000,Lainnya,Row$it" }
        val result = TabunganLogic.parseTransactionsFromCsv(header + rows, CoupleProfile())
        assertNotNull(result.errorMessage)
        assertTrue(result.imported.isEmpty())
    }

    @Test
    fun buildTransactionAndWeeklySeries_accumulateBalanceChronologically() {
        val txs = listOf(
            SavingsTransaction("1", "2026-01-05", 1, TransactionType.SETOR, 1_000_000, "Gaji", "", 1L),
            SavingsTransaction("2", "2026-01-06", 2, TransactionType.SETOR, 200_000, "Bonus", "", 2L),
            SavingsTransaction("3", "2026-01-14", 2, TransactionType.TARIK, 250_000, "Tagihan", "", 3L)
        )
        val perTx = TabunganLogic.buildTransactionSeries(txs, TimeRange.ALL)
        assertEquals(3, perTx.size)
        assertEquals(1_000_000L, perTx[0].cumulativeBalance)
        assertEquals(1_200_000L, perTx[1].cumulativeBalance)
        assertEquals(950_000L, perTx[2].cumulativeBalance)

        val weekly = TabunganLogic.buildWeeklySeries(txs, TimeRange.ALL)
        assertEquals(2, weekly.size)
        assertEquals(1_200_000L, weekly[0].cumulativeBalance)
        assertEquals(950_000L, weekly[1].cumulativeBalance)
    }
}
