package com.cybergah.securewipe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

/**
 * BÊŞOP LOGOSU
 *
 * Fikir: "şop" (iz) yok oluyor.
 * Halka, saat yonunde giderek incelen ve sonunda tamamen kaybolan
 * parcalardan olusur - basi belli, sonu yok. Ortadaki nokta geriye
 * kalan tek sey: hicbir sey.
 *
 * @param draw 0..1  halkanin ne kadarinin cizildigi (acilis animasyonu icin)
 * @param dot  0..1  ortadaki noktanin belirginligi
 */
@Composable
fun BesopLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.error,
    draw: Float = 1f,
    dot: Float = 1f
) {
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val c = center
        val ringR = r * 0.78f
        val segments = 30
        val step = 360f / segments
        val gap = step * 0.34f

        val shown = (segments * draw.coerceIn(0f, 1f)).toInt()
        for (i in 0 until shown) {
            val t = i / (segments - 1f)
            // iz solarak yok oluyor
            val alpha = (1f - t).pow(1.7f)
            if (alpha <= 0.02f) continue
            val w = (r * 0.155f) * (1f - t * 0.82f)
            drawArc(
                color = color.copy(alpha = alpha),
                startAngle = -90f + i * step,
                sweepAngle = step - gap,
                useCenter = false,
                topLeft = Offset(c.x - ringR, c.y - ringR),
                size = Size(ringR * 2f, ringR * 2f),
                style = Stroke(width = w, cap = StrokeCap.Round)
            )
        }

        if (dot > 0.01f) {
            drawCircle(
                color = color.copy(alpha = dot),
                radius = r * 0.15f,
                center = c
            )
        }
    }
}

/** Logo + isim yan yana. */
@Composable
fun BesopWordmark(
    s: Strings,
    modifier: Modifier = Modifier,
    logoSize: Int = 40,
    fontSize: Int = 26
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        BesopLogo(Modifier.size(logoSize.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            s.appName,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * CyberGah damgasi - hakkinda ekraninin ve acilisin sonuna basilir.
 */
@Composable
fun CyberGahStamp(modifier: Modifier = Modifier, alpha: Float = 1f) {
    val ink = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f * alpha)
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Rule(ink)
        Box(Modifier.padding(horizontal = 12.dp)) {
            Text(
                "CYBERGAH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 5.sp,
                color = ink
            )
        }
        Rule(ink)
    }
}

@Composable
private fun Rule(color: Color) {
    Canvas(
        Modifier
            .width(28.dp)
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height
        )
    }
}
