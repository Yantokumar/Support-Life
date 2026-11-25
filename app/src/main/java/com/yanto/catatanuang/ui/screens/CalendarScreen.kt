package com.yanto.catatanuang.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.model.CalendarNote
import com.yanto.catatanuang.model.NoteColorTag
import com.yanto.catatanuang.ui.components.EmptyStateCard
import com.yanto.catatanuang.ui.theme.BorderSubtle
import com.yanto.catatanuang.ui.theme.BrandBlue
import com.yanto.catatanuang.ui.theme.BrandBlueSoft
import com.yanto.catatanuang.ui.theme.BrandOrange
import com.yanto.catatanuang.ui.theme.BrandOrangeSoft
import com.yanto.catatanuang.ui.theme.BrandRed
import com.yanto.catatanuang.ui.theme.BrandRedSoft
import com.yanto.catatanuang.ui.theme.InkMuted
import com.yanto.catatanuang.ui.theme.InkPrimary
import com.yanto.catatanuang.ui.theme.SurfaceWhite
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    modifier: Modifier,
    notes: List<CalendarNote>,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onAddNote: (LocalDate) -> Unit,
    onEditNote: (CalendarNote) -> Unit,
    onDeleteNote: (CalendarNote) -> Unit
) {
    var visibleMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val today = remember { LocalDate.now() }
    val idLocale = remember { Locale("id", "ID") }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", idLocale) }
    val selectedFormatter = remember { DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", idLocale) }

    val notesByDate = remember(notes.toList()) {
        notes.groupBy { it.date }
    }
    val selectedIso = selectedDate.toString()
    val selectedNotes = notesByDate[selectedIso].orEmpty().sortedByDescending { it.createdAt }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Kalender Note", color = InkPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Tandai tanggal penting, acara, atau pengingat pribadi.", color = InkMuted, fontSize = 12.sp)
            }
            TextButton(onClick = {
                visibleMonth = YearMonth.from(today)
                onSelectDate(today)
            }) {
                Text("Hari Ini", color = BrandOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { visibleMonth = visibleMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Bulan sebelumnya", tint = InkPrimary)
                    }
                    Text(
                        text = visibleMonth.format(monthFormatter).replaceFirstChar { it.uppercase() },
                        color = InkPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { visibleMonth = visibleMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Bulan berikutnya", tint = InkPrimary)
                    }
                }

                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { day ->
                        Text(
                            text = day,
                            color = InkMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                val firstOfMonth = visibleMonth.atDay(1)
                val leadingEmpty = firstOfMonth.dayOfWeek.value - 1 // Monday = 0
                val totalDays = visibleMonth.lengthOfMonth()
                val totalCells = ((leadingEmpty + totalDays + 6) / 7) * 7

                for (rowStart in 0 until totalCells step 7) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = rowStart + col
                            val dayNum = cellIndex - leadingEmpty + 1
                            if (dayNum in 1..totalDays) {
                                val cellDate = visibleMonth.atDay(dayNum)
                                val cellNotes = notesByDate[cellDate.toString()].orEmpty()
                                val isSelected = cellDate == selectedDate
                                val isToday = cellDate == today

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) BrandBlue else Color.Transparent)
                                        .then(
                                            if (isToday && !isSelected) {
                                                Modifier.border(1.5.dp, BrandOrange, RoundedCornerShape(12.dp))
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .clickable { onSelectDate(cellDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            color = when {
                                                isSelected -> SurfaceWhite
                                                isToday -> BrandOrange
                                                else -> InkPrimary
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                                        )
                                        if (cellNotes.isNotEmpty()) {
                                            Spacer(Modifier.height(2.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                cellNotes.take(3).forEach { note ->
                                                    val dotColor = if (isSelected) SurfaceWhite else tagTint(note.colorTag)
                                                    Box(
                                                        Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(dotColor)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = selectedDate.format(selectedFormatter).replaceFirstChar { it.uppercase() },
                    color = InkPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (selectedNotes.isEmpty()) "Tidak ada catatan" else "${selectedNotes.size} catatan tersimpan",
                    color = InkMuted,
                    fontSize = 11.sp
                )
            }
            Button(
                onClick = { onAddNote(selectedDate) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Catatan", fontSize = 12.sp)
            }
        }

        if (selectedNotes.isEmpty()) {
            EmptyStateCard(
                title = "Tanggal ini belum ditandai",
                message = "Tambahkan catatan untuk menandai acara, hari penting, atau pengingat pada tanggal ini.",
                actionLabel = "Tambah Catatan",
                onAction = { onAddNote(selectedDate) }
            )
        } else {
            selectedNotes.forEach { note ->
                CalendarNoteCard(
                    note = note,
                    onEdit = { onEditNote(note) },
                    onDelete = { onDeleteNote(note) }
                )
            }
        }

        Spacer(Modifier.height(84.dp))
    }
}

@Composable
private fun CalendarNoteCard(
    note: CalendarNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tint = tagTint(note.colorTag)
    val bg = tagBg(note.colorTag)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.colorTag.label,
                    color = tint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(note.title, color = InkPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (note.description.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(note.description, color = InkMuted, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = "Ubah catatan", tint = InkMuted, modifier = Modifier.size(17.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus catatan", tint = BrandRed, modifier = Modifier.size(17.dp))
            }
        }
    }
}

private fun tagTint(tag: NoteColorTag): Color = when (tag) {
    NoteColorTag.BLUE -> BrandBlue
    NoteColorTag.ORANGE -> BrandOrange
    NoteColorTag.RED -> BrandRed
}

private fun tagBg(tag: NoteColorTag): Color = when (tag) {
    NoteColorTag.BLUE -> BrandBlueSoft
    NoteColorTag.ORANGE -> BrandOrangeSoft
    NoteColorTag.RED -> BrandRedSoft
}
