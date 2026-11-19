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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * IFX Progress Kit — Every loader uses the lime fill, black outline, and traveling dot.
 */

/**
 * 1. Indeterminate Bar (matching Progress Kit "Indeterminate bar"):
 * Smoothly gliding Electric Lime signal inside a clean rounded track.
 */
@Composable
fun IfxProgressBar(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    height: Dp = 8.dp,
    trackColor: Color = Color(0xFFECEEF2),
    indicatorColor: Color = IfxLime,
    borderColor: Color? = Color(0xFFE0E3E8)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ifxProgressBarTransition")

    val travelPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "travelPhase"
    )

    val animatedDeterminateProgress by animateFloatAsState(
        targetValue = (progress ?: 0f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "determinateProgress"
    )

    val barModifier = modifier
        .fillMaxWidth()
        .height(height)
        .clip(CircleShape)
        .background(trackColor)
        .then(
            if (borderColor != null) {
                Modifier.border(0.75.dp, borderColor, CircleShape)
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
            val segWidth = size.width * 0.42f
            val startX = -segWidth + travelPhase * (size.width + segWidth)
            val endX = startX + segWidth
            val brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.0f to indicatorColor.copy(alpha = 0f),
                    0.22f to indicatorColor,
                    0.78f to indicatorColor,
                    1.0f to indicatorColor.copy(alpha = 0f)
                ),
                startX = startX,
                endX = endX
            )
            drawRoundRect(
                brush = brush,
                topLeft = Offset(x = startX, y = 0f),
                size = Size(width = segWidth, height = size.height),
                cornerRadius = cornerRadius
            )
        }
    }
}

/**
 * 2. Signal Bar (Determinate progress with lime fill, black outline, traveling dot knob & percentage pill):
 * Matches Progress Kit "Signal bar" (24%, 68%, 100% with checkmark).
 */
@Composable
fun IfxSignalBar(
    progress: Float,
    modifier: Modifier = Modifier,
    showPercentagePill: Boolean = true
) {
    val clamped by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "signalBarProgress"
    )
    val pct = (clamped * 100f).roundToInt()
    val isComplete = pct >= 100

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalWidth = maxWidth
        val knobOuterSize = 24.dp
        val pillWidth = 48.dp
        val travelWidth = (totalWidth - knobOuterSize).coerceAtLeast(0.dp)
        val knobOffsetX = travelWidth * clamped
        val pillOffsetX = (knobOffsetX + knobOuterSize / 2 - pillWidth / 2)
            .coerceIn(0.dp, (totalWidth - pillWidth).coerceAtLeast(0.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            if (showPercentagePill) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = pillOffsetX)
                            .width(pillWidth)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(IfxBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$pct%",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            color = IfxLime,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(knobOuterSize),
                contentAlignment = Alignment.CenterStart
            ) {
                // Track + Lime Fill with Black Outline
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                ) {
                    val r = size.height / 2f
                    val corner = CornerRadius(r, r)

                    // Background track
                    drawRoundRect(
                        color = Color(0xFFECEEF2),
                        size = size,
                        cornerRadius = corner
                    )

                    // Filled lime track with black outline
                    val fillW = (size.width * clamped).coerceAtLeast(if (clamped > 0f) size.height else 0f)
                    if (fillW > 0f) {
                        drawRoundRect(
                            color = IfxLime,
                            size = Size(fillW, size.height),
                            cornerRadius = corner
                        )
                        drawRoundRect(
                            color = IfxBlack,
                            size = Size(fillW, size.height),
                            cornerRadius = corner,
                            style = Stroke(width = 1.25.dp.toPx())
                        )
                    }
                }

                // Traveling Dot Knob (Lime halo + Black ring + Lime dot or Checkmark at 100%)
                Box(
                    modifier = Modifier
                        .offset(x = knobOffsetX)
                        .size(knobOuterSize)
                        .clip(CircleShape)
                        .background(IfxLime.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(IfxBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isComplete) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Complete",
                                tint = IfxLime,
                                modifier = Modifier.size(10.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(IfxLime)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Circular Ring Loader with Traveling Lime Dot (matching Progress Kit "Loaders" & "Button loading"):
 * - Light surface / Lime button: gray ring track, rotating black arc, traveling lime dot with black border.
 * - Dark surface / Black button (`isDarkSurface = true`): dark charcoal track, rotating lime arc & dot.
 */
@Composable
fun IfxRingLoader(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    strokeWidth: Dp = 2.6.dp,
    isDarkSurface: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ifxRingLoader")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAngle"
    )

    val trackColor = if (isDarkSurface) Color(0xFF2B2F36) else Color(0xFFE5E7EB)
    val arcColor = if (isDarkSurface) IfxLime else IfxBlack
    val dotFill = IfxLime
    val dotStroke = IfxBlack

    Canvas(modifier = modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        val inset = strokePx + 2.dp.toPx()
        val arcDiameter = (this.size.minDimension - inset * 2).coerceAtLeast(4f)
        val topLeft = Offset((this.size.width - arcDiameter) / 2f, (this.size.height - arcDiameter) / 2f)
        val arcSize = Size(arcDiameter, arcDiameter)

        // Base ring track
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // Rotating arc segment
        val sweep = 110f
        drawArc(
            color = arcColor,
            startAngle = angle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // Traveling Lime dot anchored at the leading tip of the arc
        val tipRad = (angle + sweep) * (PI / 180.0)
        val radius = arcDiameter / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val dotCenter = Offset(
            x = center.x + (radius * cos(tipRad)).toFloat(),
            y = center.y + (radius * sin(tipRad)).toFloat()
        )
        val dotRadius = (strokePx * 0.95f).coerceAtLeast(2.6.dp.toPx())

        drawCircle(
            color = dotFill,
            radius = dotRadius,
            center = dotCenter
        )
        drawCircle(
            color = dotStroke,
            radius = dotRadius,
            center = dotCenter,
            style = Stroke(width = 1.25.dp.toPx())
        )
    }
}

/**
 * 4. 3-Dot Pulse Loader (matching Progress Kit "Loaders" middle indicator).
 */
@Composable
fun IfxDotPulseLoader(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ifxDotPulse")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotPhase"
    )
    val activeIdx = phase.toInt().coerceIn(0, 2)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 3) {
            val isActive = i == activeIdx
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(if (isActive) IfxLime else Color(0xFF9CA3AF))
                    .then(
                        if (isActive) Modifier.border(1.dp, IfxBlack, CircleShape)
                        else Modifier
                    )
            )
        }
    }
}

/**
 * 5. IFX Node-Flow Loader (`• — ◇ — x`) matching Progress Kit "Loaders" right indicator.
 * Used for waiting states (Forgot Password, Email Verification, AI generating).
 */
@Composable
fun IfxNodeFlowLoader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ifxNodeFlow")
    val stepPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nodeStepPhase"
    )
    val activeStage = stepPhase.toInt().coerceIn(0, 2)

    Canvas(modifier = modifier.size(width = 88.dp, height = 22.dp)) {
        val cy = size.height / 2f
        val strokeW = 1.8.dp.toPx()

        // 1. Circle Node (left)
        val cCenter = Offset(10.dp.toPx(), cy)
        val cRad = 5.5.dp.toPx()
        drawCircle(
            color = if (activeStage == 0) IfxLime else Color(0xFFE5E7EB),
            radius = cRad,
            center = cCenter
        )
        drawCircle(
            color = if (activeStage == 0) IfxBlack else Color(0xFF9CA3AF),
            radius = cRad,
            center = cCenter,
            style = Stroke(width = 1.3.dp.toPx())
        )

        // Connector 1
        drawLine(
            color = IfxBlack,
            start = Offset(20.dp.toPx(), cy),
            end = Offset(34.dp.toPx(), cy),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // 2. Diamond Node (center)
        val dCenter = Offset(44.dp.toPx(), cy)
        val dHalf = 6.dp.toPx()
        val diamond = Path().apply {
            moveTo(dCenter.x, dCenter.y - dHalf)
            lineTo(dCenter.x + dHalf, dCenter.y)
            lineTo(dCenter.x, dCenter.y + dHalf)
            lineTo(dCenter.x - dHalf, dCenter.y)
            close()
        }
        if (activeStage == 1) {
            drawPath(path = diamond, color = IfxLime)
        }
        drawPath(
            path = diamond,
            color = if (activeStage == 1) IfxBlack else Color(0xFF9CA3AF),
            style = Stroke(width = strokeW, join = StrokeJoin.Round)
        )

        // Connector 2
        drawLine(
            color = IfxBlack,
            start = Offset(54.dp.toPx(), cy),
            end = Offset(68.dp.toPx(), cy),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // 3. X Node (right)
        val xCenter = Offset(78.dp.toPx(), cy)
        val xArm = 4.2.dp.toPx()
        val xColor = if (activeStage == 2) IfxBlack else Color(0xFF6B7280)
        if (activeStage == 2) {
            drawCircle(
                color = IfxLime,
                radius = 7.dp.toPx(),
                center = xCenter
            )
        }
        drawLine(
            color = xColor,
            start = Offset(xCenter.x - xArm, cy - xArm),
            end = Offset(xCenter.x + xArm, cy + xArm),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = xColor,
            start = Offset(xCenter.x + xArm, cy - xArm),
            end = Offset(xCenter.x - xArm, cy + xArm),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * 6. Step Rail (matching Progress Kit "Step rail" for Registration & Setup):
 * - Completed steps: Lime circle with black outline + checkmark, connected by solid black line.
 * - Current step: Pulsing black circle with Electric Lime center dot.
 * - Upcoming steps: White hollow circle with gray border, connected by dashed gray line.
 */
@Composable
fun IfxStepRail(
    currentStep: Int,
    totalSteps: Int = 5,
    categoryName: String = "",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stepRailPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "currentStepPulse"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STEP ${currentStep + 1} OF $totalSteps",
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.5.sp,
                letterSpacing = 0.8.sp,
                color = Color(0xFF6B7280)
            )
            if (categoryName.isNotBlank()) {
                Text(
                    text = categoryName,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.8.sp,
                    color = IfxBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (index in 0 until totalSteps) {
                val isCompleted = index < currentStep
                val isCurrent = index == currentStep

                // Node
                when {
                    isCompleted -> {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(IfxLime)
                                .border(1.5.dp, IfxBlack, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Completed step ${index + 1}",
                                tint = IfxBlack,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                    isCurrent -> {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(IfxBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((8f * pulseScale).dp)
                                    .clip(CircleShape)
                                    .background(IfxLime)
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFFD1D5DB), CircleShape)
                        )
                    }
                }

                // Connector Line to Next Node
                if (index < totalSteps - 1) {
                    val isLineCompleted = index < currentStep
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        val y = size.height / 2f
                        if (isLineCompleted) {
                            drawLine(
                                color = IfxBlack,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        } else {
                            drawLine(
                                color = Color(0xFFD1D5DB),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.6.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 7. Skeleton Loading Card with soft shimmer (matching Progress Kit "Skeleton").
 */
@Composable
fun IfxSkeletonCard(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ifxSkeletonShimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFFAFAFB))
            .border(1.dp, Color(0xFFECEEF2), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ShimmerLine(fraction = 0.42f, height = 10.dp, shimmerProgress = shimmerX)
            ShimmerLine(fraction = 0.86f, height = 10.dp, shimmerProgress = shimmerX)
        }
    }
}

@Composable
private fun ShimmerLine(
    fraction: Float,
    height: Dp,
    shimmerProgress: Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth(fraction)
            .height(height)
            .clip(CircleShape)
    ) {
        val brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFECEEF2),
                Color(0xFFF6F7F9),
                Color(0xFFE4E7EC)
            ),
            startX = size.width * shimmerProgress,
            endX = size.width * (shimmerProgress + 0.65f)
        )
        drawRect(brush = brush)
    }
}

/**
 * Single, cohesive status progress indicator for "Check your inbox" waiting states
 * (Forgot Password & Email Verification) using the IFX Node-Flow Loader (`• — ◇ — x`).
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
        IfxNodeFlowLoader()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            color = Color(0xFF6B7280)
        )
    }
}

/**
 * Backward-compatible alias for compact circular spinner using the IFX Ring Loader.
 */
@Composable
fun IfxSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    strokeWidth: Dp = 2.4.dp,
    isDarkSurface: Boolean = false
) {
    IfxRingLoader(
        modifier = modifier,
        size = size,
        strokeWidth = strokeWidth,
        isDarkSurface = isDarkSurface
    )
}

/**
 * Inline Button Loading Content (matching Progress Kit "Button loading: Keeps its size so nothing jumps"):
 * Renders the circular IFX Ring Loader (with traveling lime dot) directly beside the bold label.
 */
@Composable
fun IfxButtonProgressContent(
    text: String,
    contentColor: Color = IfxBlack,
    isDarkButton: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IfxRingLoader(
            size = 20.dp,
            strokeWidth = 2.4.dp,
            isDarkSurface = isDarkButton
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 15.5.sp,
            color = contentColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Minimalist modal progress card used when an action has no dedicated loading button
 * (such as Google Sign-In or initial session verification).
 * Uses the IFX Progress Kit Node-Flow Loader + Indeterminate Lime Signal Bar inside a crisp black-bordered card.
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
                .background(IfxBlack.copy(alpha = 0.42f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(horizontal = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.5.dp, IfxBlack, RoundedCornerShape(22.dp))
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IfxNodeFlowLoader()

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
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

                Spacer(modifier = Modifier.height(14.dp))

                IfxProgressBar(
                    modifier = Modifier.fillMaxWidth(),
                    height = 7.dp
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 520)
@Composable
private fun IfxProgressKitPreview() {
    IFXTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            IfxStepRail(currentStep = 2, totalSteps = 5, categoryName = "OCCUPATION")
            IfxSignalBar(progress = 0.68f)
            IfxProgressBar()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IfxRingLoader(size = 28.dp)
                IfxDotPulseLoader()
                IfxNodeFlowLoader()
            }
            IfxSkeletonCard()
        }
    }
}
