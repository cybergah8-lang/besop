package com.cybergah.securewipe

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Silme sirasinda gosterilen animasyon:
 *  - donen kesik dis halka + ters yonde donen yaylar
 *  - nabiz gibi atan cekirdek
 *  - ilerlemeyi gosteren kalin halka ve ortadaki yuzde
 *  - yazilan rastgele veriyi temsil eden kayan hex satiri
 *  - her gecisi gosteren segment cubugu
 */
@Composable
fun WipeAnimation(p: WipeProgress, s: Strings, modifier: Modifier = Modifier) {
    val accent = MaterialTheme.colorScheme.error
    val infinite = rememberInfiniteTransition(label = "wipe")

    val rotOuter by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "rotOuter"
    )
    val rotInner by infinite.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "rotInner"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(1100, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val shimmer by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    val animated by animateFloatAsState(
        targetValue = p.overall.coerceIn(0f, 1f),
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Saniyede bir tiklayan saat. Disk modunda isletim sistemi dakikalarca
    // sessiz kalabildigi icin sureyi ciktidan degil saatten okuyoruz.
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(p.startedAtMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val elapsedMs = if (p.startedAtMs > 0) (now - p.startedAtMs).coerceAtLeast(0L) else 0L
    // Tahmini kalan: gecen sure ve ilerleme oranindan
    val remainingMs = if (p.overall > 0.02f && elapsedMs > 3000) {
        (elapsedMs * (1f - p.overall) / p.overall).toLong()
    } else {
        -1L
    }

    // Yazilan rastgele veriyi temsil eden kayan hex satiri
    var scramble by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val hex = "0123456789ABCDEF"
        while (true) {
            val sb = StringBuilder(36)
            repeat(12) { sb.append(hex.random()).append(hex.random()).append(' ') }
            scramble = sb.toString().trim()
            delay(70)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(280.dp)) {
                val r = size.minDimension / 2f
                val c = center

                // yumusak arka isik
                drawCircle(color = accent.copy(alpha = 0.06f), radius = r * pulse)
                drawCircle(color = accent.copy(alpha = 0.05f), radius = r * 0.82f * pulse)

                // donen kesik dis halka
                rotate(rotOuter) {
                    drawCircle(
                        color = accent.copy(alpha = 0.30f),
                        radius = r * 0.96f,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 16f))
                        )
                    )
                }

                // ters yonde donen uc yay
                rotate(rotInner) {
                    val rr = r * 0.80f
                    repeat(3) { i ->
                        drawArc(
                            color = accent.copy(alpha = shimmer),
                            startAngle = i * 120f,
                            sweepAngle = 44f,
                            useCenter = false,
                            topLeft = Offset(c.x - rr, c.y - rr),
                            size = Size(rr * 2f, rr * 2f),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // ilerleme halkasi: once yol, sonra dolan kisim
                val pr = r * 0.62f
                drawArc(
                    color = accent.copy(alpha = 0.14f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(c.x - pr, c.y - pr),
                    size = Size(pr * 2f, pr * 2f),
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(c.x - pr, c.y - pr),
                    size = Size(pr * 2f, pr * 2f),
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )

                // nabiz atan cekirdek
                drawCircle(color = accent.copy(alpha = 0.10f), radius = r * 0.40f * pulse)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "%" + (animated * 100).toInt(),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    s.passOf.format(p.pass, p.passCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        PassBar(p.pass, p.passCount, accent)

        Spacer(Modifier.height(24.dp))

        Text(
            p.currentName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (p.fileCount > 0) {
            Text(
                s.fileOf.format(p.fileIndex, p.fileCount) + "   ·   " +
                    s.written.format(formatSize(p.bytesDone)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                s.written.format(formatSize(p.bytesDone)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(10.dp))

        // Gecen sure  ·  tahmini kalan sure
        Text(
            s.elapsed.format(formatDuration(elapsedMs)) + "   ·   " +
                if (remainingMs >= 0) s.remaining.format(formatDuration(remainingMs))
                else s.calculating,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            scramble,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = accent.copy(alpha = 0.55f),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun PassBar(pass: Int, total: Int, accent: Color) {
    val dim = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(10.dp)
    ) {
        val n = total.coerceAtLeast(1)
        val gap = if (n > 60) 1.dp.toPx() else 2.dp.toPx()
        val w = (size.width - gap * (n - 1)) / n
        for (i in 0 until n) {
            drawRoundRect(
                color = if (i < pass) accent else dim,
                topLeft = Offset(i * (w + gap), 0f),
                size = Size(w.coerceAtLeast(1f), size.height),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }
    }
}
