package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkNavyText
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyDarkest
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.YellowCardSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium 3D-styled Card component featuring:
 * - Isometric bottom extrude shadow for tactile 3D depth
 * - Interactive spring press-down physics with depth compression
 * - Subtle inner bevel highlight on top edge
 * - Optional gentle floating hover animation
 */
@Composable
fun Card3D(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    containerColor: Color = YellowCardSurface,
    borderColor: Color = NavyBorder,
    borderWidth: Dp = 1.8.dp,
    depth: Dp = 6.dp,
    shadowColor: Color = NavyDarkest.copy(alpha = 0.85f),
    onClick: (() -> Unit)? = null,
    enableFloating: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pressProgress = remember { Animatable(0f) }

    // Floating micro-motion if enabled
    val floatAnim = if (enableFloating) {
        val infiniteTransition = rememberInfiniteTransition(label = "3d_float")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float_offset"
        ).value
    } else 0f

    val currentDepthPx = depth.value * (1f - pressProgress.value * 0.75f)

    Box(
        modifier = modifier
            .graphicsLayer {
                val scaleFactor = 1f - (pressProgress.value * 0.015f)
                scaleX = scaleFactor
                scaleY = scaleFactor
                translationY = floatAnim + (pressProgress.value * (depth.value * 0.75f))
            }
            .drawBehind {
                val cornerRadiusPx = shape.topStart.toPx(size, this)
                // Draw 3D Extrusion base shadow layer underneath
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(0f, currentDepthPx),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            }
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        while (true) {
                            awaitPointerEventScope {
                                awaitFirstDown(requireUnconsumed = false)
                                coroutineScope.launch {
                                    pressProgress.animateTo(
                                        1f,
                                        animationSpec = spring(stiffness = 800f, dampingRatio = 0.6f)
                                    )
                                }
                                val up = waitForUpOrCancellation()
                                coroutineScope.launch {
                                    pressProgress.animateTo(
                                        0f,
                                        animationSpec = spring(stiffness = 500f, dampingRatio = 0.5f)
                                    )
                                }
                                if (up != null) {
                                    onClick()
                                }
                            }
                        }
                    }
                } else Modifier
            )
            .background(containerColor, shape)
            .border(borderWidth, borderColor, shape)
            .drawBehind {
                // Subtle top inner highlight bevel for tactile realism
                val bevelHeight = 2.dp.toPx()
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = bevelHeight * 3f
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, bevelHeight * 3f),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
    ) {
        content()
    }
}

/**
 * 3D Button component with physically tactile bevel & click press action.
 */
@Composable
fun Button3D(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    backgroundColor: Color = NavyPrimary,
    borderColor: Color = NavyDarkest,
    shadowColor: Color = NavyDarkest,
    depth: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pressProgress = remember { Animatable(0f) }

    val currentDepth = depth.value * (1f - pressProgress.value * 0.85f)

    Box(
        modifier = modifier
            .graphicsLayer {
                val s = 1f - (pressProgress.value * 0.02f)
                scaleX = s
                scaleY = s
                translationY = pressProgress.value * (depth.value * 0.85f)
            }
            .drawBehind {
                val cr = shape.topStart.toPx(size, this)
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(0f, currentDepth),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cr, cr)
                )
            }
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        coroutineScope.launch {
                            pressProgress.animateTo(1f, spring(stiffness = 900f, dampingRatio = 0.5f))
                        }
                        val up = waitForUpOrCancellation()
                        coroutineScope.launch {
                            pressProgress.animateTo(0f, spring(stiffness = 600f, dampingRatio = 0.4f))
                        }
                        if (up != null) {
                            onClick()
                        }
                    }
                }
            }
            .background(backgroundColor, shape)
            .border(1.5.dp, borderColor, shape)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 6.dp.toPx()
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, 6.dp.toPx()),
                    cornerRadius = CornerRadius(shape.topStart.toPx(size, this), shape.topStart.toPx(size, this))
                )
            }
    ) {
        content()
    }
}
