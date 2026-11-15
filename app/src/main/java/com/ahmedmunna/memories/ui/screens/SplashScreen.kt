package com.ahmedmunna.memories.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedmunna.memories.ui.components.MemoriesLogo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val creditAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // coroutineScope waits for both child animations before the text fades in.
        coroutineScope {
            launch { logoAlpha.animateTo(1f, tween(500)) }
            launch {
                logoScale.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                )
            }
        }
        titleAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        taglineAlpha.animateTo(1f, tween(400))
        creditAlpha.animateTo(1f, tween(400))
        delay(650)
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF312E81), Color(0xFF1E1B4B), Color(0xFF0F0E2A)))),
    ) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MemoriesLogo(
                size = 112.dp,
                modifier = Modifier.scale(logoScale.value).alpha(logoAlpha.value),
            )
            Spacer(Modifier.height(28.dp))
            Text(
                "MEMORIES",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp,
                modifier = Modifier.alpha(titleAlpha.value),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Your Photos, Your Memories.",
                color = Color(0xFFC7D2FE),
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(taglineAlpha.value),
            )
        }
        Text(
            "Developed by Ahmed Munna",
            color = Color(0x99FFFFFF),
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .alpha(creditAlpha.value),
        )
    }
}
