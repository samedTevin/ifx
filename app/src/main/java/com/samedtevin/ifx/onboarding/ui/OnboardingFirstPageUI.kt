package com.samedtevin.ifx.onboarding.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
 * First Onboarding Page of the ifx App:
 * "If [X], then do [Y]." — Turn one sentence into a working automation.
 * Matches the bold, full-bleed 4-band layout tightly with zero wasted vertical whitespace.
 */
@Composable
fun OnboardingFirstPageScreen(
    onNext: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        OnboardingFirstPageContent(modifier = Modifier.fillMaxSize())

        IfxTopBar(
            isDark = false,
            showSkip = true,
            onSkip = onSkip,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            IfxPagerIndicator(
                pageCount = 4,
                currentPage = 0,
                isDark = false,
                modifier = Modifier.padding(start = 2.dp)
            )

            IfxPrimaryButton(
                text = "Next",
                backgroundColor = IfxBlack,
                textColor = IfxLime,
                onClick = onNext
            )
        }
    }
}

/**
 * Embedded version of Onboarding Page 1 hosted inside HorizontalPager.
 * Uses flexible band weighting so the 4 bold Lime/Black stripes fill the screen
 * cleanly with tight, proportional spacing above and below (matching the target photo).
 */
@Composable
fun OnboardingFirstPageContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 64.dp, bottom = 114.dp)
    ) {
        // 1. Bold Headline & Subtitle (tight spacing above bands)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "If ",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Black,
                    fontSize = 48.sp,
                    lineHeight = 52.sp,
                    letterSpacing = (-1.8).sp,
                    color = IfxBlack
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(IfxLime)
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "X",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 46.sp,
                        lineHeight = 50.sp,
                        color = IfxBlack
                    )
                }
                Text(
                    text = ",",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Black,
                    fontSize = 48.sp,
                    lineHeight = 52.sp,
                    color = IfxBlack
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "then do ",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Black,
                    fontSize = 48.sp,
                    lineHeight = 52.sp,
                    letterSpacing = (-1.8).sp,
                    color = IfxBlack
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(IfxLime)
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Y",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 46.sp,
                        lineHeight = 50.sp,
                        color = IfxBlack
                    )
                }
                Text(
                    text = ".",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Black,
                    fontSize = 48.sp,
                    lineHeight = 52.sp,
                    color = IfxBlack
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Turn one sentence into a working\nautomation.",
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 15.5.sp,
                lineHeight = 21.sp,
                color = Color(0xFF4B5563)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 2. Four Tall, Bold, Full-Width Alternating Lime / Black Bands filling the mid-canvas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // 01 Say it (Lime band)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(IfxLime)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IfxBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "01",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp,
                            color = IfxLime
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Say it",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp,
                        letterSpacing = (-1.2).sp,
                        color = IfxBlack
                    )
                }

                // Black speech bubble with 3 lime dots
                Row(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomStart = 24.dp,
                                bottomEnd = 6.dp
                            )
                        )
                        .background(IfxBlack)
                        .padding(horizontal = 22.dp, vertical = 17.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IfxLime)
                        )
                    }
                }
            }

            // 02 See it (Black band)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(IfxBlack)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IfxLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "02",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp,
                            color = IfxBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "See it",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp,
                        letterSpacing = (-1.2).sp,
                        color = Color.White
                    )
                }

                // White pill with crisp mini node-flow schematic (circle — diamond — square)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(width = 84.dp, height = 20.dp)) {
                        val cy = size.height / 2f
                        val strokeW = 1.6.dp.toPx()

                        drawLine(
                            color = IfxBlack,
                            start = Offset(16.dp.toPx(), cy),
                            end = Offset(34.dp.toPx(), cy),
                            strokeWidth = strokeW
                        )
                        drawLine(
                            color = IfxBlack,
                            start = Offset(50.dp.toPx(), cy),
                            end = Offset(66.dp.toPx(), cy),
                            strokeWidth = strokeW
                        )

                        val cCenter = Offset(9.dp.toPx(), cy)
                        drawCircle(color = IfxLime, radius = 7.5.dp.toPx(), center = cCenter)
                        drawCircle(
                            color = IfxBlack,
                            radius = 7.5.dp.toPx(),
                            center = cCenter,
                            style = Stroke(width = strokeW)
                        )

                        val dx = 42.dp.toPx()
                        val dHalf = 8.dp.toPx()
                        val diamond = Path().apply {
                            moveTo(dx, cy - dHalf)
                            lineTo(dx + dHalf, cy)
                            lineTo(dx, cy + dHalf)
                            lineTo(dx - dHalf, cy)
                            close()
                        }
                        drawPath(
                            path = diamond,
                            color = IfxBlack,
                            style = Stroke(width = strokeW, join = StrokeJoin.Round)
                        )

                        val sqSize = 15.dp.toPx()
                        drawRoundRect(
                            color = IfxBlack,
                            topLeft = Offset(67.dp.toPx(), cy - sqSize / 2f),
                            size = Size(sqSize, sqSize),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            // 03 Test it (Lime band)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(IfxLime)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IfxBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "03",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp,
                            color = IfxLime
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Test it",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp,
                        letterSpacing = (-1.2).sp,
                        color = IfxBlack
                    )
                }

                // Black pill with lime check + "0 errors"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(IfxBlack)
                        .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IfxLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "0 errors",
                            tint = IfxBlack,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "0 errors",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.5.sp,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // 04 Run it (Black band)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(IfxBlack)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IfxLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "04",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp,
                            color = IfxBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Run it",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp,
                        letterSpacing = (-1.2).sp,
                        color = Color.White
                    )
                }

                // Bold Lime Switch ON Pill
                Box(
                    modifier = Modifier
                        .width(84.dp)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(IfxLime)
                        .padding(4.5.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(39.dp)
                            .clip(CircleShape)
                            .background(IfxBlack)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingFirstPagePreview() {
    IFXTheme {
        OnboardingFirstPageScreen()
    }
}
