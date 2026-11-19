package com.samedtevin.ifx.home.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter
import java.text.NumberFormat
import java.util.Locale

private val DetailCanvasBg = Color(0xFF0A0A0C)
private val DetailFlowCardBg = Color(0xFF1A1B1F)
private val DetailFlowCardBorder = Color(0xFF2B2D34)
private val DetailMutedText = Color(0xFFA2A7B0)
private val DetailBodyText = Color(0xFFE3E5E9)
private val DetailSpineColor = Color(0xFF5D626E)

/**
 * Editorial Spark Detail Screen:
 * - Solid deep dark canvas (no grid)
 * - Topic-adaptive hero header grounded in IFX's monochrome + Electric Lime palette
 * - Distinct topic-specific vector icons ([SparkGlyphTile])
 * - Live countable user/run badge + tactile pill switch
 * - Minimal flowchart using IFX's own language: `If X` -> `Then do Y`
 * - Single primary CTA navigating to the interactive drag-and-drop Builder canvas
 */
@Composable
fun SparkDetailScreen(
    spark: SparkRoutineItem,
    onBackClick: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onEditInBuilderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val topic = spark.topic
    BackHandler(onBack = onBackClick)

    val formattedCount = remember(spark.activeUsersCount, spark.totalRunsCount, spark.enabled, spark.isOfficial) {
        if (spark.isOfficial) {
            NumberFormat.getIntegerInstance(Locale.US).format(spark.activeUsersCount)
        } else {
            val runs = spark.totalRunsCount
            if (runs == 1) "1 run" else "$runs runs"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DetailCanvasBg)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Topic-Adaptive Top Hero Header
        Surface(
            shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
            color = topic.heroBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        DetailBackChevron(color = topic.heroContent)
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                val shareBody = "${spark.title} — ${spark.about}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, spark.title)
                                    putExtra(Intent.EXTRA_TEXT, shareBody)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Spark"))
                            },
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        DetailShareIcon(color = topic.heroContent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Distinct Trigger + Action Vector Icon Pair
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SparkGlyphTile(
                        iconKind = spark.primaryIcon,
                        size = 48.dp,
                        cornerRadius = 15.dp,
                        containerColor = topic.tileBg,
                        iconTint = topic.tileIcon,
                        borderColor = topic.heroContent.copy(alpha = 0.22f),
                        borderWidth = 1.4.dp
                    )
                    SparkGlyphTile(
                        iconKind = spark.actionIcon,
                        size = 48.dp,
                        cornerRadius = 15.dp,
                        containerColor = topic.tileBg,
                        iconTint = topic.tileIcon,
                        borderColor = topic.heroContent.copy(alpha = 0.22f),
                        borderWidth = 1.4.dp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = spark.title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 31.sp,
                    lineHeight = 35.sp,
                    letterSpacing = (-0.9).sp,
                    color = topic.heroContent
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Creator + Unique Countable Metric
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = if (spark.isOfficial) "by IFX" else "by You",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = topic.heroContent
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DetailCountPersonIcon(color = topic.heroContent)
                        Text(
                            text = formattedCount,
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = topic.heroContent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 2. Signature Pill Toggle Bar
        SparkActivationPillToggle(
            enabled = spark.enabled,
            accentColor = topic.pillAccent,
            onToggle = { onToggleEnabled(!spark.enabled) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
        )

        Spacer(modifier = Modifier.height(26.dp))

        // 3. Concise "About this Spark" Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
        ) {
            Text(
                text = "About this Spark",
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = (-0.4).sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = spark.about,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = DetailBodyText
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Clean Flowchart Card using IFX Language ("If X" -> "Then do Y")
        SparkIfXThenDoYCard(
            spark = spark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        // 5. Single Primary CTA Button (navigates straight to Drag-and-Drop Builder Canvas)
        Button(
            onClick = onEditInBuilderClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = topic.pillAccent,
                contentColor = IfxBlack
            ),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
        ) {
            Text(
                text = if (spark.isOfficial) "Open in Builder  →" else "Edit in Builder  →",
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.5.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. Spark ID Footer
        Text(
            text = "ID ${spark.sparkCode}",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.6.sp,
            color = DetailMutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 26.dp)
        )
    }
}

@Composable
private fun SparkActivationPillToggle(
    enabled: Boolean,
    accentColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trackBg by animateColorAsState(
        targetValue = if (enabled) Color.White else DetailFlowCardBg,
        animationSpec = tween(220),
        label = "pillTrackBg"
    )
    val trackBorder by animateColorAsState(
        targetValue = if (enabled) Color.White else Color(0xFF343740),
        animationSpec = tween(220),
        label = "pillTrackBorder"
    )
    val labelColor by animateColorAsState(
        targetValue = if (enabled) IfxBlack else Color.White,
        animationSpec = tween(220),
        label = "pillLabelColor"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(62.dp)
            .clip(CircleShape)
            .background(trackBg)
            .border(1.5.dp, trackBorder, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (enabled) "Connected" else "Connect",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            letterSpacing = (-0.3).sp,
            color = labelColor
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .border(1.5.dp, IfxBlack.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(18.dp)) {
                    if (enabled) {
                        val path = Path().apply {
                            moveTo(size.width * 0.16f, size.height * 0.54f)
                            lineTo(size.width * 0.42f, size.height * 0.78f)
                            lineTo(size.width * 0.85f, size.height * 0.24f)
                        }
                        drawPath(
                            path = path,
                            color = IfxBlack,
                            style = Stroke(
                                width = 2.6.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    } else {
                        drawCircle(
                            color = IfxBlack,
                            radius = 4.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Minimal Flowchart Card speaking IFX's signature language: `If X` -> `Then do Y` (+ `Then do Z`).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SparkIfXThenDoYCard(
    spark: SparkRoutineItem,
    modifier: Modifier = Modifier
) {
    val resolvedTrigger = spark.resolvedTriggerText()
    val resolvedActions = spark.resolvedActions()
    val triggerParams = spark.params.filter { it.appliesToTrigger }
    val actionParams = spark.params.filter { !it.appliesToTrigger }
    val accent = spark.topic.pillAccent

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = DetailFlowCardBg,
        border = BorderStroke(1.dp, DetailFlowCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 22.dp)
        ) {
            // IF (Trigger)
            FlowchartTimelineRow(
                label = "If",
                iconKind = spark.primaryIcon,
                accentColor = accent,
                showConnectorBelow = resolvedActions.isNotEmpty()
            ) {
                Text(
                    text = resolvedTrigger,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    color = Color.White
                )
                if (triggerParams.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        triggerParams.forEach { param ->
                            DetailParamPill(label = param.label, value = param.value, accent = accent)
                        }
                    }
                }
            }

            // THEN DO (Actions)
            resolvedActions.forEachIndexed { index, actionText ->
                val isLast = index == resolvedActions.lastIndex
                FlowchartTimelineRow(
                    label = if (index == 0) "Then do" else "And do",
                    iconKind = spark.actionIcon,
                    accentColor = accent,
                    showConnectorBelow = !isLast
                ) {
                    Text(
                        text = actionText,
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        color = Color.White
                    )
                    if (index == 0 && actionParams.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            actionParams.forEach { param ->
                                DetailParamPill(label = param.label, value = param.value, accent = accent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowchartTimelineRow(
    label: String,
    iconKind: SparkIconKind,
    accentColor: Color,
    showConnectorBelow: Boolean,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left column: Bigger, uniform Onboarding-style bold highlighted "If" / "Then do" chips
        Box(
            modifier = Modifier
                .width(88.dp)
                .padding(top = 2.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp,
                    letterSpacing = (-0.3).sp,
                    color = IfxBlack
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Middle column: Distinct vector glyph tile + vertical spine
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(44.dp)
        ) {
            SparkGlyphTile(
                iconKind = iconKind,
                size = 42.dp,
                cornerRadius = 12.dp,
                containerColor = Color(0xFF0E0F12),
                iconTint = accentColor,
                borderColor = Color.White.copy(alpha = 0.16f),
                borderWidth = 1.2.dp
            )

            if (showConnectorBelow) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .weight(1f)
                        .background(DetailSpineColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right column: Clean 1-line statement + compact parameter pills (tight spacing between rows)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp, bottom = if (showConnectorBelow) 14.dp else 2.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun DetailParamPill(label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF121317))
            .border(1.dp, Color(0xFF3E424D), RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 11.5.sp,
            color = DetailMutedText
        )
        Text(
            text = value,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = accent
        )
    }
}

@Composable
private fun DetailCountPersonIcon(color: Color) {
    Canvas(modifier = Modifier.size(15.dp)) {
        val cx = size.width * 0.5f
        drawCircle(
            color = color,
            radius = size.width * 0.22f,
            center = Offset(cx, size.height * 0.28f)
        )
        val body = Path().apply {
            moveTo(size.width * 0.14f, size.height * 0.90f)
            cubicTo(
                size.width * 0.16f, size.height * 0.56f,
                size.width * 0.84f, size.height * 0.56f,
                size.width * 0.86f, size.height * 0.90f
            )
            close()
        }
        drawPath(path = body, color = color)
    }
}

@Composable
private fun DetailBackChevron(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = 2.8.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.64f, size.height * 0.16f)
            lineTo(size.width * 0.28f, size.height * 0.50f)
            lineTo(size.width * 0.64f, size.height * 0.84f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun DetailShareIcon(color: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = 2.2.dp.toPx()
        val w = size.width
        val h = size.height
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.60f),
            end = Offset(w * 0.5f, h * 0.14f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        val arrowHead = Path().apply {
            moveTo(w * 0.28f, h * 0.34f)
            lineTo(w * 0.5f, h * 0.12f)
            lineTo(w * 0.72f, h * 0.34f)
        }
        drawPath(
            path = arrowHead,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        val tray = Path().apply {
            moveTo(w * 0.16f, h * 0.52f)
            lineTo(w * 0.16f, h * 0.84f)
            lineTo(w * 0.84f, h * 0.84f)
            lineTo(w * 0.84f, h * 0.52f)
        }
        drawPath(
            path = tray,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
