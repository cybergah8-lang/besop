package com.cybergah.securewipe

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Acilis ekrani (~2.2 sn):
 *   1) Logo halkasi saat yonunde cizilir ve solarak yok olur
 *   2) Ortadaki nokta belirir
 *   3) "Bêşop" ve altinda "iz birakmadan siler" yazisi belirir
 *   4) Altta CyberGah damgasi
 *   5) Ekranin tamami solarak ana ekrana gecer
 */
@Composable
fun SplashScreen(lang: Lang, onDone: () -> Unit) {
    val s = stringsFor(lang)
    var started by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }

    val ring by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1200, easing = LinearOutSlowInEasing),
        label = "ring"
    )
    val dot by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(500, delayMillis = 950, easing = FastOutSlowInEasing),
        label = "dot"
    )
    val nameAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600, delayMillis = 1150),
        label = "name"
    )
    val stampAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600, delayMillis = 1500),
        label = "stamp"
    )
    val exit by animateFloatAsState(
        targetValue = if (leaving) 0f else 1f,
        animationSpec = tween(420),
        label = "exit"
    )

    LaunchedEffect(Unit) {
        started = true
        delay(2200)
        leaving = true
        delay(430)
        onDone()
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
            .alpha(exit)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BesopLogo(
                    modifier = Modifier.size(150.dp),
                    draw = ring,
                    dot = dot
                )

                Spacer(Modifier.height(28.dp))

                Text(
                    s.appName,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.alpha(nameAlpha)
                )
                Text(
                    s.tagline,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    modifier = Modifier.alpha(nameAlpha)
                )
            }

            CyberGahStamp(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 44.dp),
                alpha = stampAlpha
            )
        }
    }
}
