package com.cybergah.securewipe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Uygulamanin tamami: Android ve masaustu ayni ekrani kullanir.
 * Platforma ozgu isler (dosya secici, silme motoru) geri cagrilarla disaridan verilir.
 */
@Composable
fun WipeScreen(
    lang: Lang,
    onLangChange: (Lang) -> Unit,
    items: List<PickedItem>,
    progress: WipeProgress,
    onPickFiles: () -> Unit,
    onPickFolder: () -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    onWipe: () -> Unit,
    onStop: () -> Unit,
    showBgNote: Boolean = true,
    note: String? = null,
    /** Masaustunde disk ekranini acar; Android'de null (disk modu yok). */
    onOpenDisk: (() -> Unit)? = null,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeChange: (ThemeMode) -> Unit = {}
) {
    val s = stringsFor(lang)
    var confirmOpen by remember { mutableStateOf(false) }
    var splashDone by remember { mutableStateOf(false) }
    var aboutOpen by remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (lang.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        if (!splashDone) {
            SplashScreen(lang) { splashDone = true }
            return@CompositionLocalProvider
        }
        if (aboutOpen) {
            AboutScreen(lang) { aboutOpen = false }
            return@CompositionLocalProvider
        }

        GlowBackground {
            Box(Modifier.fillMaxSize()) {

                // Hakkinda girisi - dil secicinin karsi kosesinde
                AnimatedVisibility(
                    visible = !progress.running,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(Modifier.padding(4.dp)) {
                        TextButton(onClick = { aboutOpen = true }) {
                            Text(
                                s.about,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        }
                        if (onOpenDisk != null) {
                            TextButton(onClick = onOpenDisk) {
                                Text(
                                    s.diskEntry,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }
                }

                // Dil secici - sadece bekleme ekraninda, kosede
                AnimatedVisibility(
                    visible = !progress.running,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ThemeToggle(themeMode, onThemeChange)
                        LanguagePicker(lang, onLangChange)
                    }
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(progress.running, enter = fadeIn(), exit = fadeOut()) {
                        RunningView(progress, s, showBgNote, onStop)
                    }
                    AnimatedVisibility(!progress.running, enter = fadeIn(), exit = fadeOut()) {
                        IdleView(
                            s = s,
                            items = items,
                            progress = progress,
                            note = note,
                            onPickFiles = onPickFiles,
                            onPickFolder = onPickFolder,
                            onRemove = onRemove,
                            onClear = onClear,
                            onWipe = { confirmOpen = true }
                        )
                    }
                }
            }
        }

        if (confirmOpen) {
            val total = items.sumOf { it.size }
            AlertDialog(
                onDismissRequest = { confirmOpen = false },
                title = { Text(s.confirmTitle) },
                text = {
                    Text(
                        s.confirmBody.format(items.size, formatSize(total), PASSES) +
                            "\n\n" + s.confirmWarn
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        confirmOpen = false
                        onWipe()
                    }) { Text(s.confirmYes, color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { confirmOpen = false }) { Text(s.confirmNo) }
                }
            )
        }
    }
}

/** Sistem -> aydinlik -> karanlik arasinda dolasan kucuk dugme. */
@Composable
private fun ThemeToggle(mode: ThemeMode, onChange: (ThemeMode) -> Unit) {
    TextButton(onClick = { onChange(mode.next()) }) {
        Text(
            mode.glyph,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LanguagePicker(lang: Lang, onChange: (Lang) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box(Modifier.padding(8.dp)) {
        TextButton(onClick = { open = true }) {
            Text(
                lang.nativeName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Lang.entries.forEach { l ->
                DropdownMenuItem(
                    text = {
                        Text(
                            l.nativeName,
                            fontWeight = if (l == lang) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onChange(l)
                        open = false
                    }
                )
            }
        }
    }
}

/**
 * Ana eylem dugmesi: duz renk yerine hafif degrade ve golge.
 * Pasifken tamamen sonuk, aktifken dikkat cekici.
 */
@Composable
private fun DestroyButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(20.dp)
    val brush = if (enabled) {
        Brush.horizontalGradient(
            listOf(accent, lerp(accent, MaterialTheme.colorScheme.secondary, 0.28f))
        )
    } else {
        SolidColor(MaterialTheme.colorScheme.surfaceVariant)
    }

    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(brush)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = if (enabled) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

// ---------------------------------------------------------------- bekleme ekrani

@Composable
private fun IdleView(
    s: Strings,
    items: List<PickedItem>,
    progress: WipeProgress,
    note: String?,
    onPickFiles: () -> Unit,
    onPickFolder: () -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    onWipe: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BesopLogo(Modifier.size(72.dp))
        Spacer(Modifier.height(14.dp))
        Text(s.appName, fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(
            s.subtitle.format(PASSES),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Dile gore etiketler uzayip iki satira sarabiliyor -> sabit degil, en az yukseklik
        val pickPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onPickFiles,
                contentPadding = pickPadding,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 68.dp)
            ) { Text(s.pickFiles, textAlign = TextAlign.Center, maxLines = 2) }

            OutlinedButton(
                onClick = onPickFolder,
                contentPadding = pickPadding,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 68.dp)
            ) { Text(s.pickFolder, textAlign = TextAlign.Center, maxLines = 2) }
        }

        Spacer(Modifier.height(20.dp))

        if (items.isNotEmpty()) {
            SelectedFiles(s, items, onRemove, onClear)
            Spacer(Modifier.height(20.dp))
        }

        DestroyButton(
            text = s.wipeButton,
            enabled = items.isNotEmpty(),
            onClick = onWipe
        )

        note?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        if (progress.finished && progress.results.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Results(s, progress)
        }
    }
}

@Composable
private fun SelectedFiles(
    s: Strings,
    items: List<PickedItem>,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    s.filesSummary.format(items.size, formatSize(items.sumOf { it.size })),
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onClear) { Text(s.clearBtn) }
            }

            LazyColumn(Modifier.heightIn(max = 190.dp)) {
                itemsIndexed(items) { i, f ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                f.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                formatSize(f.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        TextButton(onClick = { onRemove(i) }) { Text("×", fontSize = 20.sp) }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun Results(s: Strings, p: WipeProgress) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            val ok = p.results.count { it.ok }
            Text(
                if (p.cancelled) s.stoppedTitle else s.doneTitle,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                s.doneSummary.format(ok, p.results.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.heightIn(max = 160.dp)) {
                items(p.results) { r ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            if (r.ok) "✓" else "✕",
                            modifier = Modifier.padding(end = 10.dp),
                            color = if (r.ok) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                        Column {
                            Text(
                                r.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                r.detailText(s),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- silme ekrani

@Composable
private fun RunningView(p: WipeProgress, s: Strings, showBgNote: Boolean, onStop: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WipeAnimation(p, s)

        Spacer(Modifier.height(32.dp))

        OutlinedButton(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) { Text(s.stopBtn) }

        if (showBgNote) {
            Spacer(Modifier.height(12.dp))
            Text(
                s.bgNote,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
