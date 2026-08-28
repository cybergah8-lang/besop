package com.cybergah.securewipe.desktop

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybergah.securewipe.Lang
import com.cybergah.securewipe.Strings
import com.cybergah.securewipe.WipeAnimation
import com.cybergah.securewipe.WipeProgress
import com.cybergah.securewipe.formatSize
import com.cybergah.securewipe.stringsFor

/**
 * Disk silme ekrani. Sadece masaustunde var.
 * Sistem diski listede gorunur ama secilemez.
 */
@Composable
fun DiskScreen(
    lang: Lang,
    disks: List<DiskInfo>,
    elevated: Boolean,
    progress: WipeProgress,
    onRefresh: () -> Unit,
    onWipe: (DiskInfo) -> Unit,
    onStop: () -> Unit,
    onElevate: () -> Unit,
    onClose: () -> Unit
) {
    val s = stringsFor(lang)
    var selected by remember { mutableStateOf<DiskInfo?>(null) }
    var confirmFor by remember { mutableStateOf<DiskInfo?>(null) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            if (progress.running) {
                RunningDisk(progress, s, onStop)
                return@Box
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(s.diskTitle, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    s.diskWarn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                if (!elevated) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(s.diskNeedAdmin, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = onElevate) { Text(s.diskRestartAdmin) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onRefresh) { Text(s.diskRefresh) }
                        }

                        if (disks.isEmpty()) {
                            Text(
                                s.diskNone,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }

                        LazyColumn(Modifier.heightIn(max = 260.dp)) {
                            items(disks) { d ->
                                DiskRow(
                                    d = d,
                                    s = s,
                                    selected = selected?.id == d.id,
                                    onSelect = { if (!d.isSystem) selected = d }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { selected?.let { confirmFor = it } },
                    enabled = selected != null && elevated,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                ) {
                    Text(s.diskButton, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }

                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) { Text(s.close) }
            }
        }
    }

    confirmFor?.let { d ->
        TypedConfirmDialog(
            s = s,
            disk = d,
            onDismiss = { confirmFor = null },
            onConfirm = {
                confirmFor = null
                onWipe(d)
            }
        )
    }
}

@Composable
private fun DiskRow(d: DiskInfo, s: Strings, selected: Boolean, onSelect: () -> Unit) {
    val locked = d.isSystem
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !locked, onClick = onSelect)
            .background(
                if (selected) MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect, enabled = !locked)
        Column(Modifier.weight(1f)) {
            Text(
                d.display,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (locked) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            val kindText = if (d.kind == DiskKind.REMOVABLE) s.diskRemovable else s.diskFixed
            Text(
                if (locked) s.diskSystemLocked
                else kindText + "  ·  " + formatSize(d.totalBytes),
                style = MaterialTheme.typography.labelSmall,
                color = if (locked) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/** Yanlis disk secimini onlemek icin surucu adini elle yazdirir. */
@Composable
private fun TypedConfirmDialog(
    s: Strings,
    disk: DiskInfo,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var typed by remember { mutableStateOf("") }
    val expected = disk.id
    val ok = typed.trim().equals(expected, ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.confirmTitle) },
        text = {
            Column {
                Text(s.diskConfirm.format(disk.display, formatSize(disk.totalBytes), DiskWipe.PASSES))
                Spacer(Modifier.height(8.dp))
                Text(s.confirmWarn, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
                Text(
                    s.diskTypeToConfirm.format(expected),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = ok) {
                Text(s.confirmYes, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.confirmNo) } }
    )
}

@Composable
private fun RunningDisk(p: WipeProgress, s: Strings, onStop: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WipeAnimation(p, s)

        if (p.statusLine.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(
                p.statusLine,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(Modifier.height(28.dp))
        OutlinedButton(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) { Text(s.stopBtn) }
    }
}
