package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import kotlinx.coroutines.delay

/**
 * Provides a crisp staggered 3D entrance pop-in animation (scale + slide up + fade).
 */
@Composable
fun Animated3DSection(
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(380, easing = CubicBezierEasing(0.2f, 0.0f, 0f, 1f))) +
                scaleIn(
                    initialScale = 0.92f,
                    transformOrigin = TransformOrigin(0.5f, 0.5f),
                    animationSpec = tween(420, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f))
                ) +
                slideInVertically(
                    initialOffsetY = { 35 },
                    animationSpec = tween(420, easing = CubicBezierEasing(0.2f, 0.0f, 0f, 1f))
                ),
        modifier = modifier
    ) {
        content()
    }
}
