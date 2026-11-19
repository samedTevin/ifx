package com.samedtevin.ifx.onboarding.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Third Onboarding Page: "Describe it, let [Gemini] help"
 * Features Electric Lime background, top headline with boxed black Gemini badge,
 * composer card using the official `ic_gemini_color` SVG logo, and 3 cascading numbered steps (01, 02, 03).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingThirdPageContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IfxLime)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 62.dp, bottom = 114.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // 1. Top Headline: "Describe it, / let [Gemini] help" + Subtitle
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Describe it,",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Black,
                    fontSize = 39.sp,
                    lineHeight = 43.sp,
                    letterSpacing = (-1.3).sp,
                    color = IfxBlack
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "let ",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 39.sp,
                        lineHeight = 43.sp,
                        letterSpacing = (-1.3).sp,
                        color = IfxBlack
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(IfxBlack)
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Gemini",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Black,
                            fontSize = 37.sp,
                            lineHeight = 41.sp,
                            letterSpacing = (-1.2).sp,
                            color = IfxLime
                        )
                    }
                    Text(
                        text = " help",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 39.sp,
                        lineHeight = 43.sp,
                        letterSpacing = (-1.3).sp,
                        color = IfxBlack
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Say it in a sentence. The flow builds itself.",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = IfxBlack.copy(alpha = 0.78f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. White Gemini Prompt Composer Card (uses existing official ic_gemini_color SVG logo)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(26.dp), spotColor = Color(0x28000000))
                    .background(Color.White, RoundedCornerShape(26.dp))
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_gemini_color),
                            contentDescription = "Gemini",
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "Gemini",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp,
                            color = IfxBlack
                        )
                    }

                    // Highlighted prompt sentence
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "When a",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = IfxBlack
                        )
                        HighlightChip("deadline email")
                        Text(
                            text = "arrives,",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = IfxBlack
                        )
                        Text(
                            text = "add it to",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = IfxBlack
                        )
                        HighlightChip("Calendar")
                        Text(
                            text = "and save a",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = IfxBlack
                        )
                        HighlightChip("note")
                        Text(
                            text = ".|",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = IfxBlack
                        )
                    }

                    // Bottom row: Crisp Vector Microphone + Lime Arrow Circle Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clean Canvas Microphone Icon
                        Canvas(modifier = Modifier.size(22.dp)) {
                            val stroke = 1.8.dp.toPx()
                            val micColor = Color(0xFF6B7280)
                            val w = size.width
                            val h = size.height
                            drawRoundRect(
                                color = micColor,
                                topLeft = Offset(w * 0.34f, h * 0.10f),
                                size = Size(w * 0.32f, h * 0.48f),
                                cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
                                style = Stroke(width = stroke)
                            )
                            drawArc(
                                color = micColor,
                                startAngle = 0f,
                                sweepAngle = 180f,
                                useCenter = false,
                                topLeft = Offset(w * 0.20f, h * 0.34f),
                                size = Size(w * 0.60f, h * 0.38f),
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                            drawLine(
                                color = micColor,
                                start = Offset(w * 0.50f, h * 0.72f),
                                end = Offset(w * 0.50f, h * 0.90f),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = micColor,
                                start = Offset(w * 0.34f, h * 0.90f),
                                end = Offset(w * 0.66f, h * 0.90f),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(IfxLime)
                                .border(1.4.dp, IfxBlack, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val stroke = 2.dp.toPx()
                                val w = size.width
                                val h = size.height
                                drawLine(
                                    color = IfxBlack,
                                    start = Offset(w * 0.12f, h * 0.5f),
                                    end = Offset(w * 0.84f, h * 0.5f),
                                    strokeWidth = stroke,
                                    cap = StrokeCap.Round
                                )
                                val arrow = Path().apply {
                                    moveTo(w * 0.52f, h * 0.22f)
                                    lineTo(w * 0.85f, h * 0.5f)
                                    lineTo(w * 0.52f, h * 0.78f)
                                }
                                drawPath(
                                    path = arrow,
                                    color = IfxBlack,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Three Cascading Numbered Steps (01 -> 02 -> 03) Connected by Crisp L-Shaped Flow Edges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(216.dp)
            ) {
                // Continuous L-shaped orthogonal flow edges connecting Card 01 -> Card 02 -> Card 03
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 2.2.dp.toPx()
                    val bendR = 10.dp.toPx()

                    // Edge 1: Bottom of Card 01 (y=54dp) -> vertical down -> rounded bend -> into Card 02 left edge (y=107dp)
                    val e1StartX = 22.dp.toPx()
                    val e1StartY = 52.dp.toPx()
                    val e1MidY = 107.dp.toPx()
                    val e1EndX = 38.dp.toPx()

                    val path1 = Path().apply {
                        moveTo(e1StartX, e1StartY)
                        lineTo(e1StartX, e1MidY - bendR)
                        quadraticTo(e1StartX, e1MidY, e1StartX + bendR, e1MidY)
                        lineTo(e1EndX, e1MidY)
                    }
                    drawPath(
                        path = path1,
                        color = IfxBlack,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Edge 2: Bottom of Card 02 (y=130dp) -> vertical down -> rounded bend -> into Card 03 left edge (y=183dp)
                    val e2StartX = 58.dp.toPx()
                    val e2StartY = 128.dp.toPx()
                    val e2MidY = 183.dp.toPx()
                    val e2EndX = 76.dp.toPx()

                    val path2 = Path().apply {
                        moveTo(e2StartX, e2StartY)
                        lineTo(e2StartX, e2MidY - bendR)
                        quadraticTo(e2StartX, e2MidY, e2StartX + bendR, e2MidY)
                        lineTo(e2EndX, e2MidY)
                    }
                    drawPath(
                        path = path2,
                        color = IfxBlack,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // Step 01: TRIGGER (Top-left, y = 0..62dp)
                CascadeStepPill(
                    number = "01",
                    tag = "TRIGGER",
                    title = "New email arrives",
                    isDark = true,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth(0.78f)
                        .height(62.dp)
                )

                // Step 02: CONDITION (Shifted right, y = 76..138dp, center y = 107dp)
                CascadeStepPill(
                    number = "02",
                    tag = "CONDITION",
                    title = "Contains 'deadline'",
                    isDark = false,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 76.dp, start = 34.dp)
                        .fillMaxWidth(0.88f)
                        .height(62.dp)
                )

                // Step 03: ACTION (Shifted further right, y = 152..214dp, center y = 183dp)
                CascadeStepPill(
                    number = "03",
                    tag = "ACTION",
                    title = "Calendar + Note",
                    isDark = true,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 152.dp, start = 72.dp)
                        .fillMaxWidth()
                        .height(62.dp)
                )
            }
        }
    }
}

@Composable
private fun CascadeStepPill(
    number: String,
    tag: String,
    title: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(31.dp), spotColor = Color(0x28000000))
            .clip(RoundedCornerShape(31.dp))
            .background(if (isDark) IfxBlack else Color.White)
            .then(
                if (!isDark) Modifier.border(1.4.dp, IfxBlack, RoundedCornerShape(31.dp))
                else Modifier
            )
            .padding(horizontal = 13.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isDark) IfxLime else IfxBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = if (isDark) IfxBlack else IfxLime
                )
            }

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tag,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    letterSpacing = 0.8.sp,
                    color = if (isDark) IfxLime else Color(0xFF4B5563),
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = if (isDark) Color.White else IfxBlack,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isDark) IfxLime else IfxBlack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "Ready",
                tint = if (isDark) IfxBlack else IfxLime,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun HighlightChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(IfxLime)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 16.5.sp,
            color = IfxBlack
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingThirdPagePreview() {
    IFXTheme {
        OnboardingThirdPageContent()
    }
}
