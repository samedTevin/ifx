package com.samedtevin.ifx.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Guided App Tour stop definition linking each explanation directly to a live tab/feature.
 */
data class AppTourStep(
    val index: Int,
    val targetTab: HomeNavTab,
    val category: String,
    val title: String,
    val description: String,
    val showSampleSparksOnHome: Boolean = false
)

object AppTourCatalog {
    val steps: List<AppTourStep> = listOf(
        AppTourStep(
            index = 0,
            targetTab = HomeNavTab.HOME,
            category = "HOME",
            title = "Your Sparks at a glance",
            description = "Switch any Spark on or off with one tap, browse curated categories, or tap + New Spark to draft a routine with Gemini.",
            showSampleSparksOnHome = true
        ),
        AppTourStep(
            index = 1,
            targetTab = HomeNavTab.BUILDER,
            category = "BUILDER",
            title = "Edit visually & dry-run",
            description = "Fine-tune Triggers, Conditions, and Actions on the interactive canvas and dry-run with sample inputs before going live."
        ),
        AppTourStep(
            index = 2,
            targetTab = HomeNavTab.NOTEBOOK,
            category = "NOTEBOOK",
            title = "Auto-saved smart notes",
            description = "Every deadline summary, morning brief, and weekly digest saved by your Sparks lands here automatically."
        ),
        AppTourStep(
            index = 3,
            targetTab = HomeNavTab.HISTORY,
            category = "HISTORY",
            title = "Track runs & customize",
            description = "Inspect every Spark execution trace in History, and tap your avatar at the top-left anytime for profile, permissions, or settings."
        )
    )
}

/**
 * First-time newcomer invitation dialog: "Welcome, [Name]! Would you like a quick tour?"
 * Uses IFX's own `ic_ifx_squircle` brand mark, Electric Lime name highlight, and asymmetric stop pills.
 */
@Composable
fun AppTourInviteDialog(
    userName: String,
    onStartTour: () -> Unit,
    onSkip: () -> Unit
) {
    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 18.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Top Row: IFX Brand Mark (replacing generic star icon) + Soft Circular Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_ifx_squircle),
                        contentDescription = "ifx",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    Surface(
                        onClick = onSkip,
                        shape = CircleShape,
                        color = Color(0xFFF3F5F0),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(13.dp)) {
                                val stroke = 2.dp.toPx()
                                drawLine(
                                    color = IfxBlack,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = stroke,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = IfxBlack,
                                    start = Offset(size.width, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = stroke,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Stacked Headline: "Welcome," + "[Name]!" with Electric Lime highlight
                Text(
                    text = "Welcome,",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-1.1).sp,
                    color = IfxBlack
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(14.dp))
                            .background(IfxLime)
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = userName.ifBlank { "Friend" },
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 34.sp,
                            lineHeight = 38.sp,
                            letterSpacing = (-1.1).sp,
                            color = IfxBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "!",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        lineHeight = 38.sp,
                        letterSpacing = (-1.0).sp,
                        color = IfxBlack,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Would you like a quick tour?",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.5.sp,
                    lineHeight = 24.sp,
                    letterSpacing = (-0.2).sp,
                    color = Color(0xFF4B525E)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "4 STOPS · ABOUT 30 SECONDS",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.1.sp,
                    color = Color(0xFF525866)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2x2 Asymmetric Leaf-Corner Tour Stop Pills
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TourStopPill(number = "1", label = "Home", isLime = true)
                        TourStopPill(number = "2", label = "Builder", isLime = false)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TourStopPill(number = "3", label = "Notebook", isLime = true)
                        TourStopPill(number = "4", label = "History", isLime = false)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary CTA: Solid Black Capsule with Electric Lime text & crisp vector arrow
                Button(
                    onClick = onStartTour,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IfxBlack,
                        contentColor = IfxLime
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Start the tour",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp,
                        color = IfxLime
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Canvas(modifier = Modifier.size(15.dp)) {
                        val stroke = 2.2.dp.toPx()
                        val cy = size.height / 2f
                        drawLine(
                            color = IfxLime,
                            start = Offset(1.dp.toPx(), cy),
                            end = Offset(size.width - 1.5.dp.toPx(), cy),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        val head = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width * 0.56f, size.height * 0.22f)
                            lineTo(size.width - 1.5.dp.toPx(), cy)
                            lineTo(size.width * 0.56f, size.height * 0.78f)
                        }
                        drawPath(
                            path = head,
                            color = IfxLime,
                            style = Stroke(
                                width = stroke,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Maybe later",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = IfxBlack,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Replay it anytime from Settings.",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.5.sp,
                    color = Color(0xFF686D76),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TourStopPill(
    number: String,
    label: String,
    isLime: Boolean,
    modifier: Modifier = Modifier
) {
    val pillBg = if (isLime) IfxLime else IfxBlack
    val badgeBg = if (isLime) IfxBlack else IfxLime
    val badgeText = if (isLime) IfxLime else IfxBlack
    val labelColor = if (isLime) IfxBlack else Color.White

    Surface(
        shape = RoundedCornerShape(
            topStart = 22.dp,
            topEnd = 22.dp,
            bottomEnd = 22.dp,
            bottomStart = 6.dp
        ),
        color = pillBg,
        modifier = modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.5.sp,
                    color = badgeText
                )
            }
            Text(
                text = label,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                letterSpacing = (-0.2).sp,
                color = labelColor
            )
        }
    }
}

/**
 * Floating Coachmark Card displayed during the active Guided Tour.
 * Sits cleanly above the bottom navigation bar while the screen behind switches
 * to each real tab (`Home` -> `Builder` -> `Notebook` -> `History`).
 */
@Composable
fun AppTourCoachmarkCard(
    step: AppTourStep,
    totalSteps: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFirst = step.index == 0
    val isLast = step.index == totalSteps - 1

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = IfxBlack,
        border = BorderStroke(1.5.dp, IfxLime),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Top bar: Asymmetric leaf step badge + Skip link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomEnd = 12.dp,
                                    bottomStart = 4.dp
                                )
                            )
                            .background(IfxLime)
                            .padding(horizontal = 10.dp, vertical = 3.5.dp)
                    ) {
                        Text(
                            text = "${step.index + 1} / $totalSteps",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = IfxBlack
                        )
                    }
                    Text(
                        text = step.category,
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp,
                        letterSpacing = 1.sp,
                        color = IfxLime
                    )
                }

                Text(
                    text = "Skip tour",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textDecoration = TextDecoration.Underline,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated step title & description
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tourStepContent"
            ) { current ->
                Column {
                    Text(
                        text = current.title,
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        letterSpacing = (-0.4).sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = current.description,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        lineHeight = 19.5.sp,
                        color = Color(0xFFD1D5DB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compact step segments progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalSteps) {
                    val isDoneOrCurrent = i <= step.index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (isDoneOrCurrent) IfxLime else Color(0xFF2B2F36))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Controls: Back + Next / Finish
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isFirst) {
                    Button(
                        onClick = onPrevious,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F2228),
                            contentColor = Color.White
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = "Back",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IfxLime,
                        contentColor = IfxBlack
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(46.dp)
                ) {
                    Text(
                        text = if (isLast) "Finish tour" else "Next",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.5.sp,
                        color = IfxBlack
                    )
                    if (!isLast) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Canvas(modifier = Modifier.size(13.dp)) {
                            val stroke = 2.dp.toPx()
                            val cy = size.height / 2f
                            drawLine(
                                color = IfxBlack,
                                start = Offset(1.dp.toPx(), cy),
                                end = Offset(size.width - 1.dp.toPx(), cy),
                                strokeWidth = stroke,
                                cap = StrokeCap.Round
                            )
                            val head = androidx.compose.ui.graphics.Path().apply {
                                moveTo(size.width * 0.56f, size.height * 0.22f)
                                lineTo(size.width - 1.dp.toPx(), cy)
                                lineTo(size.width * 0.56f, size.height * 0.78f)
                            }
                            drawPath(
                                path = head,
                                color = IfxBlack,
                                style = Stroke(
                                    width = stroke,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
