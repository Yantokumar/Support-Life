package com.yanto.catatanuang.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanto.catatanuang.data.TabunganLogic
import com.yanto.catatanuang.model.NoteColorTag
import com.yanto.catatanuang.model.PersonalNote
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
import java.util.Locale

@Composable
fun NotesScreen(
    modifier: Modifier,
    notes: List<PersonalNote>,
    onAddNote: () -> Unit,
    onEditNote: (PersonalNote) -> Unit,
    onTogglePin: (PersonalNote) -> Unit,
    onDeleteNote: (PersonalNote) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var pinnedOnly by remember { mutableStateOf(false) }
    var tagFilter by remember { mutableStateOf<NoteColorTag?>(null) }

    val snapshot = notes.toList()
    val filtered = remember(snapshot, query, pinnedOnly, tagFilter) {
        val q = query.trim().lowercase(Locale.ROOT)
        snapshot.filter { note ->
            val matchQuery = q.isEmpty() ||
                note.title.lowercase(Locale.ROOT).contains(q) ||
                note.content.lowercase(Locale.ROOT).contains(q)
            val matchPin = !pinnedOnly || note.pinned
            val matchTag = tagFilter == null || note.colorTag == tagFilter
            matchQuery && matchPin && matchTag
        }.sortedWith(compareByDescending<PersonalNote> { it.pinned }.thenByDescending { it.updatedAt })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Catatan", color = InkPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Simpan ide, daftar, atau catatan pribadi.", color = InkMuted, fontSize = 12.sp)
            }
            Button(
                onClick = onAddNote,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Catatan", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Cari judul atau isi catatan...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !pinnedOnly && tagFilter == null,
                onClick = { pinnedOnly = false; tagFilter = null },
                label = { Text("Semua", fontSize = 11.sp) }
            )
            FilterChip(
                selected = pinnedOnly,
                onClick = { pinnedOnly = !pinnedOnly },
                label = { Text("Disematkan", fontSize = 11.sp) }
            )
            NoteColorTag.entries.forEach { tag ->
                FilterChip(
                    selected = tagFilter == tag,
                    onClick = { tagFilter = if (tagFilter == tag) null else tag },
                    label = { Text(tag.label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Spacer(Modifier.height(8.dp))
            EmptyStateCard(
                title = if (snapshot.isEmpty()) "Belum ada catatan" else "Tidak ada catatan yang cocok",
                message = if (snapshot.isEmpty()) {
                    "Tulis catatan bebas untuk ide, daftar belanja, atau pengingat harian."
                } else {
                    "Coba ubah kata kunci pencarian atau filter."
                },
                actionLabel = if (snapshot.isEmpty()) "Buat Catatan" else "Reset Filter",
                onAction = {
                    if (snapshot.isEmpty()) {
                        onAddNote()
                    } else {
                        query = ""
                        pinnedOnly = false
                        tagFilter = null
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 92.dp)
            ) {
                items(filtered, key = { it.id }) { note ->
                    PersonalNoteCard(
                        note = note,
                        onEdit = { onEditNote(note) },
                        onTogglePin = { onTogglePin(note) },
                        onDelete = { onDeleteNote(note) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalNoteCard(
    note: PersonalNote,
    onEdit: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val (tint, bg) = when (note.colorTag) {
        NoteColorTag.BLUE -> BrandBlue to BrandBlueSoft
        NoteColorTag.ORANGE -> BrandOrange to BrandOrangeSoft
        NoteColorTag.RED -> BrandRed to BrandRedSoft
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, if (note.pinned) tint.copy(alpha = 0.55f) else BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
                    Text(
                        text = TabunganLogic.formatAuditTime(note.updatedAt),
                        color = InkMuted,
                        fontSize = 10.sp
                    )
                }
                Row {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = if (note.pinned) "Lepas sematan" else "Sematkan",
                            tint = if (note.pinned) BrandOrange else InkMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Ubah catatan", tint = InkMuted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Hapus catatan", tint = BrandRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(note.title, color = InkPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (note.content.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content,
                    color = InkMuted,
                    fontSize = 13.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
