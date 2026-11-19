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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Fourth Onboarding Page: "Test it [before] / it runs."
 * Preserves the existing full-bleed dark grid background (`ic_bg_grid_dark`)
 * with the crisp conversational dry-run flow:
 * - "Test it first" right-aligned white bubble
 * - 01/02/03 verified checklist card
 * - Electric Lime "All good · 0 errors. Ready to run." card
 * - "Run it ->" right-aligned white bubble
 */
@Composable
fun OnboardingFourthPageContent(
    modifier: Modifier = Modifier
) {
    val darkBubbleFill = Color(0xFF151811)
    val darkBubbleBorder = Color(0xFF282D21)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IfxBlack)
    ) {
        // Existing Full-Bleed Dark Grid Background (preserved)
        Image(
            painter = painterResource(id = R.drawable.ic_bg_grid_dark),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 64.dp, bottom = 116.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Headline: "Test it [before] / it runs." + Subtitle
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Test it ",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 40.sp,
                        lineHeight = 45.sp,
                        letterSpacing = (-1.3).sp,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(IfxLime)
                            .padding(horizontal = 10.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "before",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Black,
                            fontSize = 39.sp,
                            lineHeight = 44.sp,
                            letterSpacing = (-1.3).sp,
                            color = IfxBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "it runs.",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp,
                    lineHeight = 45.sp,
                    letterSpacing = (-1.3).sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "A safe dry-run. No surprises.",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.5.sp,
                    color = Color(0xFF9AA196)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Dry-Run Conversational Verification Stack
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Pill 1: Right-aligned crisp white "Test it first"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 5.dp
                                )
                            )
                            .background(Color.White)
                            .padding(horizontal = 22.dp, vertical = 13.dp)
                    ) {
                        Text(
                            text = "Test it first",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = IfxBlack
                        )
                    }
                }

                // Card 2: Left-aligned dark checklist with 01, 02, 03 indices
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .clip(
                            RoundedCornerShape(
                                topStart = 22.dp,
                                topEnd = 22.dp,
                                bottomStart = 5.dp,
                                bottomEnd = 22.dp
                            )
                        )
                        .background(darkBubbleFill)
                        .border(
                            width = 1.dp,
                            color = darkBubbleBorder,
                            shape = RoundedCornerShape(
                                topStart = 22.dp,
                                topEnd = 22.dp,
                                bottomStart = 5.dp,
                                bottomEnd = 22.dp
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    DryRunNumberedRow(title = "Trigger matched", number = "01")
                    HorizontalDivider(color = Color(0xFF23271D), thickness = 1.dp)
                    DryRunNumberedRow(title = "Condition is true", number = "02")
                    HorizontalDivider(color = Color(0xFF23271D), thickness = 1.dp)
                    DryRunNumberedRow(title = "Actions verified", number = "03")
                }

                // Card 3: Left-aligned bright Electric Lime "All good" Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .clip(
                            RoundedCornerShape(
                                topStart = 22.dp,
                                topEnd = 22.dp,
                                bottomStart = 5.dp,
                                bottomEnd = 22.dp
                            )
                        )
                        .background(IfxLime)
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(IfxBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "All good",
                            tint = IfxLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "All good",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.4).sp,
                            color = IfxBlack
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "0 errors. Ready to run.",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = IfxBlack.copy(alpha = 0.84f)
                        )
                    }
                }

                // Pill 4: Right-aligned crisp white "Run it ->"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 20.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 5.dp
                                )
                            )
                            .background(Color.White)
                            .padding(horizontal = 22.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Run it",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp,
                            color = IfxBlack
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Canvas(modifier = Modifier.size(15.dp)) {
                            val stroke = 2.dp.toPx()
                            val w = size.width
                            val h = size.height
                            drawLine(
                                color = IfxBlack,
                                start = Offset(w * 0.10f, h * 0.5f),
                                end = Offset(w * 0.85f, h * 0.5f),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                            val arrow = Path().apply {
                                moveTo(w * 0.52f, h * 0.22f)
                                lineTo(w * 0.86f, h * 0.5f)
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

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DryRunNumberedRow(
    title: String,
    number: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(IfxLime),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Verified",
                    tint = IfxBlack,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Text(
            text = number,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            color = Color(0xFF7B8375)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingFourthPagePreview() {
    IFXTheme {
        OnboardingFourthPageContent()
    }
}
