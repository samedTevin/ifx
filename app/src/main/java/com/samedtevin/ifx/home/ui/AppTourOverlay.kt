package com.samedtevin.ifx.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
            title = "Start with plain English",
            description = "Type or speak any routine in the black Gemini prompt bar at the top—ifx drafts the entire Spark for you.",
            showSampleSparksOnHome = false
        ),
        AppTourStep(
            index = 1,
            targetTab = HomeNavTab.HOME,
            category = "YOUR SPARKS",
            title = "Manage & create Sparks",
            description = "Tap any Spark card to select it, switch it on or off anytime, or tap + New Spark to create a new automation.",
            showSampleSparksOnHome = true
        ),
        AppTourStep(
            index = 2,
            targetTab = HomeNavTab.PROJECTS,
            category = "PROJECTS",
            title = "Organize by project",
            description = "Group related Sparks into Projects like Study, Work, or Travel so you can manage or pause entire workflows together."
        ),
        AppTourStep(
            index = 3,
            targetTab = HomeNavTab.BUILDER,
            category = "BUILDER",
            title = "Edit visually & dry-run",
            description = "Fine-tune Triggers, Conditions, and Actions step-by-step and test with mock data before your Spark goes live."
        ),
        AppTourStep(
            index = 4,
            targetTab = HomeNavTab.NOTEBOOK,
            category = "NOTEBOOK",
            title = "Auto-saved smart notes",
            description = "Every deadline summary, meeting brief, and weekly digest saved by your Sparks lands here automatically."
        ),
        AppTourStep(
            index = 5,
            targetTab = HomeNavTab.HISTORY,
            category = "HISTORY & PROFILE",
            title = "Track runs & customize profile",
            description = "Inspect every Spark run trace in History, and tap your avatar at the top-left anytime for profile photo, permissions, or settings."
        )
    )
}

/**
 * First-time newcomer invitation dialog: "Would you like to tour the app?"
 * Tapping "Skip" immediately sets `app_tour_skipped = true` in DataStore so it never shows again.
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
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, IfxBlack),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_ifx_squircle),
                        contentDescription = "ifx",
                        modifier = Modifier.size(40.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(IfxLime)
                            .border(1.dp, IfxBlack, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "QUICK TOUR · 30 SEC",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.7.sp,
                            color = IfxBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Welcome, $userName!\nWould you like a quick tour?",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    letterSpacing = (-0.5).sp,
                    color = IfxBlack
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We'll walk you through Home, Projects, Builder, Notebook, and History one by one so you know where everything lives. You can skip anytime.",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF4B5563)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onStartTour,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IfxLime,
                        contentColor = IfxBlack
                    ),
                    border = BorderStroke(1.5.dp, IfxBlack),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Start guided tour",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = IfxBlack
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onSkip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = IfxBlack
                    ),
                    border = BorderStroke(1.2.dp, Color(0xFFD1D5DB)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Skip, I'll explore on my own",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}

/**
 * Floating Coachmark Card displayed during the active Guided Tour.
 * Sits cleanly above the bottom navigation bar while the screen behind switches
 * to each real tab (`Home` -> `Projects` -> `Builder` -> `Notebook` -> `History`).
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
        shape = RoundedCornerShape(24.dp),
        color = IfxBlack,
        border = BorderStroke(1.5.dp, IfxLime),
        shadowElevation = 14.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Top bar: Step badge + Skip link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(IfxLime)
                            .padding(horizontal = 9.dp, vertical = 3.dp)
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
                        letterSpacing = 0.8.sp,
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
                        fontSize = 18.sp,
                        letterSpacing = (-0.3).sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = current.description,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFFD1D5DB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compact step dots progress bar
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
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(44.dp)
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
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                ) {
                    Text(
                        text = if (isLast) "Finish tour" else "Next  ->",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.5.sp,
                        color = IfxBlack
                    )
                }
            }
        }
    }
}
