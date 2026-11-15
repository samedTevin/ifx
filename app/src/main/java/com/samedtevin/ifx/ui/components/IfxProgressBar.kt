package com.samedtevin.ifx.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Clean, flat, solid-fill IFX linear progress bar.
 * - Zero gradients or alpha smudges; uses solid high-contrast IFX colors.
 * - Smooth pill glider clipped cleanly inside a full-rounded capsule track.
 */
@Composable
fun IfxProgressBar(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    height: Dp = 4.dp,
    trackColor: Color = Color(0xFFE5E7EB),
    indicatorColor: Color = IfxBlack,
    borderColor: Color? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ifxProgressBarTransition")

    val travelPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "travelPhase"
    )

    val animatedDeterminateProgress by animateFloatAsState(
        targetValue = (progress ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "determinateProgress"
    )

    val barModifier = modifier
        .fillMaxWidth()
        .height(height)
        .clip(CircleShape)
        .background(trackColor)
        .then(
            if (borderColor != null) {
                Modifier.border(1.dp, borderColor, CircleShape)
            } else {
                Modifier
            }
        )

    Canvas(modifier = barModifier) {
        val radius = size.height / 2f
        val cornerRadius = CornerRadius(radius, radius)

        if (progress != null) {
            val barWidth = size.width * animatedDeterminateProgress
            if (barWidth > 0f) {
                drawRoundRect(
                    color = indicatorColor,
                    topLeft = Offset.Zero,
                    size = Size(width = barWidth, height = size.height),
                    cornerRadius = cornerRadius
                )
            }
        } else {
            val segWidth = size.width * 0.36f
            val startX = -segWidth + travelPhase * (size.width + segWidth)
            drawRoundRect(
                color = indicatorColor,
                topLeft = Offset(x = startX, y = 0f),
                size = Size(width = segWidth, height = size.height),
                cornerRadius = cornerRadius
            )
        }
    }
}

/**
 * Single, cohesive status progress indicator for "Check your inbox" waiting states
 * (e.g., Forgot Password & Email Verification).
 * Uses strictly one progress bar centered with the status text (no duplicate pulsing dot).
 */
@Composable
fun IfxWaitingIndicator(
    text: String,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Text(
            text = text,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(8.dp))
        IfxProgressBar(
            modifier = Modifier.width(136.dp),
            height = 4.dp,
            trackColor = Color(0xFFE5E7EB),
            indicatorColor = IfxBlack
        )
    }
}

/**
 * Minimal circular spinner for compact inline locations.
 */
@Composable
fun IfxSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    strokeWidth: Dp = 2.dp,
    primaryColor: Color = IfxBlack,
    trackColor: Color = primaryColor.copy(alpha = 0.18f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ifxSpinnerTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinnerRotation"
    )

    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawCircle(
            color = trackColor,
            style = stroke
        )
        drawArc(
            color = primaryColor,
            startAngle = rotation,
            sweepAngle = 100f,
            useCenter = false,
            style = stroke
        )
    }
}

/**
 * Inline loading content used inside primary IFX CTA buttons while waiting for backend responses.
 * Uses strictly ONE progress bar below the button label (no second spinner or modal overlay).
 */
@Composable
fun IfxButtonProgressContent(
    text: String,
    contentColor: Color = IfxBlack,
    trackColor: Color = IfxBlack.copy(alpha = 0.16f),
    indicatorColor: Color = IfxBlack
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = contentColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        IfxProgressBar(
            modifier = Modifier.width(112.dp),
            height = 3.5.dp,
            trackColor = trackColor,
            indicatorColor = indicatorColor
        )
    }
}

/**
 * Minimalist modal progress card used only when an action has no dedicated loading button
 * (such as Google Sign-In or initial session verification).
 * Contains strictly ONE linear progress bar.
 */
@Composable
fun IfxBackendProgressOverlay(
    visible: Boolean,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(160)),
        exit = fadeOut(animationSpec = tween(140)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(IfxBlack.copy(alpha = 0.35f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(horizontal = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    color = IfxBlack,
                    textAlign = TextAlign.Center
                )

                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                IfxProgressBar(
                    modifier = Modifier.fillMaxWidth(),
                    height = 4.dp,
                    trackColor = Color(0xFFE5E7EB),
                    indicatorColor = IfxBlack
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 200)
@Composable
private fun IfxProgressComponentsPreview() {
    IFXTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IfxWaitingIndicator(text = "Waiting for password reset...")
        }
    }
}
