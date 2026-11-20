package com.samedtevin.ifx.home.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime

/**
 * Cohesive, architectural topic palettes grounded in IFX's monochrome + Electric Lime identity.
 * Each Spark topic gives its Detail hero a distinct tone without clashing or loud off-brand colors.
 */
enum class SparkTopic(
    val label: String,
    val heroBg: Color,
    val heroContent: Color,
    val heroSubtle: Color,
    val tileBg: Color,
    val tileIcon: Color,
    val pillAccent: Color
) {
    EMAIL(
        label = "Inbox",
        heroBg = IfxLime,
        heroContent = IfxBlack,
        heroSubtle = Color(0xFF232915),
        tileBg = IfxBlack,
        tileIcon = IfxLime,
        pillAccent = IfxLime
    ),
    SCHEDULE(
        label = "Schedule",
        heroBg = Color(0xFFEAE6DC),      // Warm architectural sand
        heroContent = IfxBlack,
        heroSubtle = Color(0xFF47443C),
        tileBg = IfxBlack,
        tileIcon = Color(0xFFEAE6DC),
        pillAccent = Color(0xFFEAE6DC)
    ),
    ROUTINE(
        label = "Routine",
        heroBg = Color(0xFFD5E9C3),      // Muted botanical lime-sage
        heroContent = IfxBlack,
        heroSubtle = Color(0xFF2B3B20),
        tileBg = IfxBlack,
        tileIcon = Color(0xFFD5E9C3),
        pillAccent = Color(0xFFD5E9C3)
    ),
    FOCUS(
        label = "Focus",
        heroBg = Color(0xFFDCE4EE),      // Cool architectural slate-mist
        heroContent = IfxBlack,
        heroSubtle = Color(0xFF334152),
        tileBg = IfxBlack,
        tileIcon = Color(0xFFDCE4EE),
        pillAccent = Color(0xFFDCE4EE)
    ),
    NIGHT(
        label = "Night",
        heroBg = Color(0xFF1F232C),      // Deep graphite night surface
        heroContent = Color.White,
        heroSubtle = Color(0xFFB5BBC7),
        tileBg = Color(0xFF0E1015),
        tileIcon = IfxLime,
        pillAccent = IfxLime
    ),
    REVIEW(
        label = "Review",
        heroBg = Color(0xFFECEFF3),      // Crisp gallery stone
        heroContent = IfxBlack,
        heroSubtle = Color(0xFF4B525E),
        tileBg = IfxBlack,
        tileIcon = Color.White,
        pillAccent = IfxLime
    ),
    FINANCE(
        label = "Receipts",
        heroBg = Color(0xFFE2EAD8),      // Subtle olive-paper tint
        heroContent = IfxBlack,
        heroSubtle = Color(0xFF36422D),
        tileBg = IfxBlack,
        tileIcon = IfxLime,
        pillAccent = Color(0xFFE2EAD8)
    ),
    CUSTOM(
        label = "Custom",
        heroBg = IfxLime,
        heroContent = IfxBlack,
        heroSubtle = Color(0xFF232915),
        tileBg = IfxBlack,
        tileIcon = IfxLime,
        pillAccent = IfxLime
    )
}

/**
 * Distinct vector icon kinds so every Spark and Builder node carries its own clear visual identity.
 */
enum class SparkIconKind {
    MAIL_DEADLINE,
    SUN_BRIEFING,
    HABIT_ROUTINE,
    FOCUS_TIMER,
    CALENDAR_EVENT,
    WEEKLY_CHART,
    MOON_SLEEP,
    RECEIPT_DOC,
    CLOCK_TRIGGER,
    NOTEBOOK_PAGE,
    BELL_ALERT,
    GEMINI_AGENT,
    LIST_BRIEF,
    SILENT_DND,
    VIDEO_MEET,
    TROPHY_STAR,
    WALLET_VAULT,
    BED_REST
}

/**
 * Crisp custom-drawn vector icon tile used across Home cards, Spark Detail hero/flowchart,
 * and n8n-style Builder canvas nodes.
 */
@Composable
fun SparkGlyphTile(
    iconKind: SparkIconKind,
    size: Dp = 44.dp,
    cornerRadius: Dp = 13.dp,
    containerColor: Color = IfxBlack,
    iconTint: Color = IfxLime,
    borderColor: Color = IfxBlack.copy(alpha = 0.18f),
    borderWidth: Dp = 1.2.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        SparkGlyphVector(
            iconKind = iconKind,
            tint = iconTint,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

@Composable
fun SparkGlyphVector(
    iconKind: SparkIconKind,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.105f).coerceIn(1.8.dp.toPx(), 2.8.dp.toPx())
        val strokeStyle = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (iconKind) {
            SparkIconKind.MAIL_DEADLINE -> {
                // Crisp envelope
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.08f, h * 0.20f),
                    size = Size(w * 0.84f, h * 0.60f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = strokeStyle
                )
                val flap = Path().apply {
                    moveTo(w * 0.14f, h * 0.26f)
                    lineTo(w * 0.50f, h * 0.54f)
                    lineTo(w * 0.86f, h * 0.26f)
                }
                drawPath(flap, color = tint, style = strokeStyle)
            }

            SparkIconKind.SUN_BRIEFING -> {
                // Rising sun over horizon line
                drawLine(
                    color = tint,
                    start = Offset(w * 0.08f, h * 0.74f),
                    end = Offset(w * 0.92f, h * 0.74f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawArc(
                    color = tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.24f, h * 0.46f),
                    size = Size(w * 0.52f, h * 0.52f),
                    style = strokeStyle
                )
                // Sun rays
                drawLine(tint, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.30f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.22f, h * 0.28f), Offset(w * 0.32f, h * 0.38f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.78f, h * 0.28f), Offset(w * 0.68f, h * 0.38f), stroke, StrokeCap.Round)
            }

            SparkIconKind.HABIT_ROUTINE -> {
                // Crisp circular habit loop + checkmark inside
                drawCircle(
                    color = tint,
                    radius = w * 0.40f,
                    center = Offset(w * 0.50f, h * 0.50f),
                    style = strokeStyle
                )
                val check = Path().apply {
                    moveTo(w * 0.32f, h * 0.52f)
                    lineTo(w * 0.45f, h * 0.65f)
                    lineTo(w * 0.70f, h * 0.37f)
                }
                drawPath(check, color = tint, style = strokeStyle)
            }

            SparkIconKind.FOCUS_TIMER -> {
                // Target reticle / focus rings
                drawCircle(tint, radius = w * 0.41f, center = Offset(w * 0.50f, h * 0.50f), style = strokeStyle)
                drawCircle(tint, radius = w * 0.22f, center = Offset(w * 0.50f, h * 0.50f), style = strokeStyle)
                drawCircle(tint, radius = w * 0.07f, center = Offset(w * 0.50f, h * 0.50f))
            }

            SparkIconKind.CALENDAR_EVENT -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.10f, h * 0.20f),
                    size = Size(w * 0.80f, h * 0.70f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
                    style = strokeStyle
                )
                drawLine(tint, Offset(w * 0.10f, h * 0.42f), Offset(w * 0.90f, h * 0.42f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.32f, h * 0.10f), Offset(w * 0.32f, h * 0.26f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.68f, h * 0.10f), Offset(w * 0.68f, h * 0.26f), stroke, StrokeCap.Round)
                drawCircle(tint, radius = w * 0.08f, center = Offset(w * 0.64f, h * 0.66f))
            }

            SparkIconKind.WEEKLY_CHART -> {
                // Three crisp ascending bar columns
                drawLine(tint, Offset(w * 0.14f, h * 0.84f), Offset(w * 0.86f, h * 0.84f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.26f, h * 0.84f), Offset(w * 0.26f, h * 0.56f), stroke * 1.35f, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.50f, h * 0.84f), Offset(w * 0.50f, h * 0.36f), stroke * 1.35f, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.74f, h * 0.84f), Offset(w * 0.74f, h * 0.20f), stroke * 1.35f, StrokeCap.Round)
            }

            SparkIconKind.MOON_SLEEP -> {
                // Crisp crescent moon + small star dot
                val moon = Path().apply {
                    moveTo(w * 0.64f, h * 0.16f)
                    cubicTo(w * 0.36f, h * 0.18f, w * 0.20f, h * 0.40f, w * 0.24f, h * 0.64f)
                    cubicTo(w * 0.28f, h * 0.84f, w * 0.54f, h * 0.92f, w * 0.78f, h * 0.76f)
                    cubicTo(w * 0.54f, h * 0.78f, w * 0.40f, h * 0.58f, w * 0.46f, h * 0.36f)
                    cubicTo(w * 0.49f, h * 0.26f, w * 0.56f, h * 0.20f, w * 0.64f, h * 0.16f)
                    close()
                }
                drawPath(moon, color = tint, style = strokeStyle)
                drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.78f, h * 0.28f))
            }

            SparkIconKind.RECEIPT_DOC -> {
                // Receipt slip with zig-zag bottom & lines
                val slip = Path().apply {
                    moveTo(w * 0.20f, h * 0.12f)
                    lineTo(w * 0.80f, h * 0.12f)
                    lineTo(w * 0.80f, h * 0.86f)
                    lineTo(w * 0.65f, h * 0.76f)
                    lineTo(w * 0.50f, h * 0.86f)
                    lineTo(w * 0.35f, h * 0.76f)
                    lineTo(w * 0.20f, h * 0.86f)
                    close()
                }
                drawPath(slip, color = tint, style = strokeStyle)
                drawLine(tint, Offset(w * 0.34f, h * 0.36f), Offset(w * 0.66f, h * 0.36f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.34f, h * 0.54f), Offset(w * 0.58f, h * 0.54f), stroke, StrokeCap.Round)
            }

            SparkIconKind.CLOCK_TRIGGER -> {
                drawCircle(tint, radius = w * 0.40f, center = Offset(w * 0.50f, h * 0.50f), style = strokeStyle)
                drawLine(tint, Offset(w * 0.50f, h * 0.50f), Offset(w * 0.50f, h * 0.28f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.50f, h * 0.50f), Offset(w * 0.68f, h * 0.58f), stroke, StrokeCap.Round)
            }

            SparkIconKind.NOTEBOOK_PAGE -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.18f, h * 0.12f),
                    size = Size(w * 0.64f, h * 0.76f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = strokeStyle
                )
                drawLine(tint, Offset(w * 0.34f, h * 0.36f), Offset(w * 0.66f, h * 0.36f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.34f, h * 0.54f), Offset(w * 0.66f, h * 0.54f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.34f, h * 0.70f), Offset(w * 0.52f, h * 0.70f), stroke, StrokeCap.Round)
            }

            SparkIconKind.BELL_ALERT -> {
                val bell = Path().apply {
                    moveTo(w * 0.50f, h * 0.16f)
                    cubicTo(w * 0.28f, h * 0.16f, w * 0.24f, h * 0.36f, w * 0.24f, h * 0.56f)
                    lineTo(w * 0.14f, h * 0.72f)
                    lineTo(w * 0.86f, h * 0.72f)
                    lineTo(w * 0.76f, h * 0.56f)
                    cubicTo(w * 0.76f, h * 0.36f, w * 0.72f, h * 0.16f, w * 0.50f, h * 0.16f)
                    close()
                }
                drawPath(bell, color = tint, style = strokeStyle)
                drawLine(tint, Offset(w * 0.42f, h * 0.84f), Offset(w * 0.58f, h * 0.84f), stroke, StrokeCap.Round)
            }

            SparkIconKind.GEMINI_AGENT -> {
                // Crisp 4-point star
                val star = Path().apply {
                    moveTo(w * 0.50f, h * 0.10f)
                    quadraticTo(w * 0.54f, h * 0.46f, w * 0.90f, h * 0.50f)
                    quadraticTo(w * 0.54f, h * 0.54f, w * 0.50f, h * 0.90f)
                    quadraticTo(w * 0.46f, h * 0.54f, w * 0.10f, h * 0.50f)
                    quadraticTo(w * 0.46f, h * 0.46f, w * 0.50f, h * 0.10f)
                    close()
                }
                drawPath(star, color = tint, style = strokeStyle)
            }

            SparkIconKind.LIST_BRIEF -> {
                // Crisp agenda checklist (3 bullet dots + lines)
                listOf(0.26f, 0.50f, 0.74f).forEach { y ->
                    drawCircle(tint, radius = w * 0.06f, center = Offset(w * 0.18f, h * y))
                    drawLine(tint, Offset(w * 0.34f, h * y), Offset(w * 0.86f, h * y), stroke, StrokeCap.Round)
                }
            }

            SparkIconKind.SILENT_DND -> {
                // Do-not-disturb circle with horizontal bar
                drawCircle(tint, radius = w * 0.38f, center = Offset(w * 0.50f, h * 0.50f), style = strokeStyle)
                drawLine(tint, Offset(w * 0.30f, h * 0.50f), Offset(w * 0.70f, h * 0.50f), stroke * 1.15f, StrokeCap.Round)
            }

            SparkIconKind.VIDEO_MEET -> {
                // Video meeting camera frame + lens cone
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.10f, h * 0.26f),
                    size = Size(w * 0.54f, h * 0.48f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = strokeStyle
                )
                val cone = Path().apply {
                    moveTo(w * 0.64f, h * 0.42f)
                    lineTo(w * 0.88f, h * 0.28f)
                    lineTo(w * 0.88f, h * 0.72f)
                    lineTo(w * 0.64f, h * 0.58f)
                    close()
                }
                drawPath(cone, color = tint, style = strokeStyle)
            }

            SparkIconKind.TROPHY_STAR -> {
                // Crisp ribbon / summary badge
                drawCircle(tint, radius = w * 0.26f, center = Offset(w * 0.50f, h * 0.38f), style = strokeStyle)
                val ribbons = Path().apply {
                    moveTo(w * 0.34f, h * 0.60f)
                    lineTo(w * 0.28f, h * 0.88f)
                    lineTo(w * 0.50f, h * 0.78f)
                    lineTo(w * 0.72f, h * 0.88f)
                    lineTo(w * 0.66f, h * 0.60f)
                }
                drawPath(ribbons, color = tint, style = strokeStyle)
            }

            SparkIconKind.WALLET_VAULT -> {
                // Clean wallet / expense ledger
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.10f, h * 0.22f),
                    size = Size(w * 0.80f, h * 0.56f),
                    cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
                    style = strokeStyle
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.58f, h * 0.42f),
                    size = Size(w * 0.32f, h * 0.18f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = strokeStyle
                )
            }

            SparkIconKind.BED_REST -> {
                // Minimal bed / wind-down icon
                drawLine(tint, Offset(w * 0.12f, h * 0.30f), Offset(w * 0.12f, h * 0.76f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.12f, h * 0.62f), Offset(w * 0.88f, h * 0.62f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.88f, h * 0.50f), Offset(w * 0.88f, h * 0.76f), stroke, StrokeCap.Round)
                drawCircle(tint, radius = w * 0.09f, center = Offset(w * 0.30f, h * 0.48f), style = strokeStyle)
                drawLine(tint, Offset(w * 0.46f, h * 0.50f), Offset(w * 0.88f, h * 0.50f), stroke, StrokeCap.Round)
            }
        }
    }
}
