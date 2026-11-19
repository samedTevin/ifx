package com.samedtevin.ifx.onboarding.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.IfxMuted
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Second Onboarding Page: "Create your flow, [visually]"
 * Keeps the existing full-bleed light grid background (`ic_bg_grid_light`)
 * and demonstrates the visual node builder with Trigger, Condition, branched Actions, and + Add step.
 */
@Composable
fun OnboardingSecondPageContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Existing Full-Bleed Light Grid Background (preserved)
        Image(
            painter = painterResource(id = R.drawable.ic_bg_grid_light),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

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
            // 2. Display Headline: "Create your / flow, [visually]" + Subtitle
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Create your",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Black,
                    fontSize = 39.sp,
                    lineHeight = 43.sp,
                    letterSpacing = (-1.2).sp,
                    color = IfxBlack
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "flow, ",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 39.sp,
                        lineHeight = 43.sp,
                        letterSpacing = (-1.2).sp,
                        color = IfxBlack
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(IfxLime)
                            .padding(horizontal = 10.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "visually",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp,
                            lineHeight = 42.sp,
                            letterSpacing = (-1.2).sp,
                            color = IfxBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Connect triggers, conditions and actions.",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF4B5563)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Visual Flow Builder Diagram
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trigger Node (Black with Lime Mail Circle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.76f)
                        .align(Alignment.Start)
                        .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x33000000))
                        .background(IfxBlack, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(IfxLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_auth_mail),
                            contentDescription = "Trigger mail",
                            tint = IfxBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "TRIGGER",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = IfxLime
                        )
                        Text(
                            text = "New email arrives",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = Color.White
                        )
                    }
                }

                // Plus Connector between Trigger and Condition
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .width(40.dp)
                        .align(Alignment.Start)
                        .padding(start = 52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = IfxBlack,
                            start = Offset(size.width / 2f, 0f),
                            end = Offset(size.width / 2f, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(IfxBlack)
                            .border(2.dp, IfxLime, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = IfxLime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Condition Node (Electric Lime with Black Diamond Circle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .align(Alignment.Start)
                        .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x28C6FF3F))
                        .background(IfxLime, RoundedCornerShape(24.dp))
                        .border(1.5.dp, IfxBlack, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(IfxBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            val halfW = size.width / 2f
                            val halfH = size.height / 2f
                            val diamond = Path().apply {
                                moveTo(halfW, 0f)
                                lineTo(size.width, halfH)
                                lineTo(halfW, size.height)
                                lineTo(0f, halfH)
                                close()
                            }
                            drawPath(
                                path = diamond,
                                color = IfxLime,
                                style = Stroke(width = 1.8.dp.toPx(), join = StrokeJoin.Round)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "CONDITION",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = IfxBlack
                        )
                        Text(
                            text = "If it contains 'deadline'",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = IfxBlack
                        )
                    }
                }

                // Two Downward Branching Arrows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    repeat(2) { idx ->
                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            val centerX = size.width * if (idx == 0) 0.50f else 0.50f
                            val bottomY = size.height - 3.dp.toPx()

                            drawLine(
                                color = IfxBlack,
                                start = Offset(centerX, 0f),
                                end = Offset(centerX, bottomY),
                                strokeWidth = 2.dp.toPx()
                            )

                            val arrowPath = Path().apply {
                                moveTo(centerX, bottomY + 3.dp.toPx())
                                lineTo(centerX - 6.dp.toPx(), bottomY - 8.dp.toPx())
                                lineTo(centerX + 6.dp.toPx(), bottomY - 8.dp.toPx())
                                close()
                            }
                            drawPath(arrowPath, color = IfxLime)
                            drawPath(arrowPath, color = IfxBlack, style = Stroke(1.5.dp.toPx()))
                        }
                    }
                }

                // Side-by-Side Action Cards (Black with top-left Lime icon circle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionFlowCard(
                        iconRes = R.drawable.ic_calendar_outline,
                        title = "Add to Calendar",
                        modifier = Modifier.weight(1f)
                    )
                    ActionFlowCard(
                        iconRes = R.drawable.ic_tile_notebook,
                        title = "Save to Notebook",
                        useTileImage = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dashed "+ Add step" Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                        .dashedBorder(
                            color = Color(0xFFB0B5BC),
                            strokeWidth = 1.5.dp,
                            dashLength = 7.dp,
                            gapLength = 5.dp,
                            cornerRadius = 24.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = IfxMuted
                        )
                        Text(
                            text = "Add step",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = IfxMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionFlowCard(
    iconRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    useTileImage: Boolean = false
) {
    Column(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0x33000000))
            .background(IfxBlack, RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(IfxLime),
            contentAlignment = Alignment.Center
        ) {
            if (useTileImage) {
                Canvas(modifier = Modifier.size(15.dp)) {
                    val stroke = 1.6.dp.toPx()
                    val w = size.width
                    val h = size.height
                    drawRoundRect(
                        color = IfxBlack,
                        topLeft = Offset(w * 0.14f, h * 0.08f),
                        size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.84f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
                        style = Stroke(width = stroke)
                    )
                    drawLine(
                        color = IfxBlack,
                        start = Offset(w * 0.30f, h * 0.36f),
                        end = Offset(w * 0.70f, h * 0.36f),
                        strokeWidth = stroke
                    )
                    drawLine(
                        color = IfxBlack,
                        start = Offset(w * 0.30f, h * 0.60f),
                        end = Offset(w * 0.60f, h * 0.60f),
                        strokeWidth = stroke
                    )
                }
            } else {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = IfxBlack,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "ACTION",
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = IfxLime
            )
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                lineHeight = 19.sp,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingSecondPagePreview() {
    IFXTheme {
        OnboardingSecondPageContent()
    }
}
