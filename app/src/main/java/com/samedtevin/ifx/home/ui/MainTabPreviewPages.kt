package com.samedtevin.ifx.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Projects tab overview shown when the user (or Guided App Tour) visits the Projects tab.
 */
@Composable
fun ProjectsTabSection(
    onCreateSparkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Projects",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.8).sp,
            color = IfxBlack
        )
        Text(
            text = "Group related Sparks so you can run or pause workflows together.",
            fontFamily = Inter,
            fontSize = 14.sp,
            color = Color(0xFF686D76)
        )

        Spacer(modifier = Modifier.height(4.dp))

        ProjectOverviewCard(
            title = "Study & Deadlines",
            sparksCount = "2 active Sparks",
            detail = "Deadline Email · Weekly Review",
            highlighted = true,
            iconRes = R.drawable.ic_tile_mail,
            onClick = onCreateSparkClick
        )
        ProjectOverviewCard(
            title = "Daily Routine",
            sparksCount = "2 active Sparks",
            detail = "Morning Briefing · Routine Reminder",
            highlighted = false,
            iconRes = R.drawable.ic_tile_bell,
            onClick = onCreateSparkClick
        )
    }
}

@Composable
private fun ProjectOverviewCard(
    title: String,
    sparksCount: String,
    detail: String,
    highlighted: Boolean,
    iconRes: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = if (highlighted) IfxLime else Color.White,
        border = BorderStroke(if (highlighted) 1.5.dp else 1.dp, if (highlighted) IfxBlack else Color(0xFFE4E7EC)),
        shadowElevation = if (highlighted) 8.dp else 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.5.sp,
                    color = IfxBlack
                )
                Text(
                    text = detail,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = if (highlighted) IfxBlack.copy(alpha = 0.78f) else Color(0xFF686D76)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (highlighted) IfxBlack else Color(0xFFF3F4F6))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = sparksCount,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (highlighted) IfxLime else IfxBlack
                )
            }
        }
    }
}

/**
 * Visual Builder tab:
 * - When navigated here from Spark Detail, opens [SparkBuilderEditor] in either IFX parameter-only mode
 *   or full-flow user edit mode depending on [SparkRoutineItem.isOfficial].
 * - When opened directly from bottom navigation, lists Sparks so the user can customize/edit or create a new one.
 */
@Composable
fun BuilderTabSection(
    sparks: List<SparkRoutineItem>,
    editingSpark: SparkRoutineItem?,
    onSelectSparkToEdit: (SparkRoutineItem) -> Unit,
    onSaveSpark: (SparkRoutineItem) -> Unit,
    onDeleteSpark: (SparkRoutineItem) -> Unit,
    onBackToDetail: (SparkRoutineItem) -> Unit,
    onOpenBuilderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (editingSpark != null) {
        SparkBuilderEditor(
            spark = editingSpark,
            onSaveSpark = onSaveSpark,
            onDeleteSpark = onDeleteSpark,
            onBackToDetail = onBackToDetail,
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Visual Builder",
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                letterSpacing = (-0.8).sp,
                color = IfxBlack
            )
            Text(
                text = "Customize IFX Sparks or build and fully edit your own flows.",
                fontFamily = Inter,
                fontSize = 14.sp,
                color = Color(0xFF686D76)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onOpenBuilderClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IfxLime,
                    contentColor = IfxBlack
                ),
                border = BorderStroke(1.5.dp, IfxBlack),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "+ Create Your Own Spark",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            sparks.forEach { spark ->
                val modeLabel = if (spark.isOfficial) "BY IFX · CUSTOMIZE INPUTS" else "BY YOU · FULL BUILDER"
                BuilderNodePreviewCard(
                    iconKind = spark.primaryIcon,
                    accent = spark.topic.tileIcon,
                    tag = modeLabel,
                    title = spark.title,
                    isDark = !spark.isOfficial,
                    onClick = { onSelectSparkToEdit(spark) }
                )
            }
        }
    }
}

@Composable
private fun BuilderNodePreviewCard(
    iconKind: SparkIconKind,
    accent: Color,
    tag: String,
    title: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isDark) IfxBlack else Color.White
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, IfxBlack),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SparkGlyphTile(
                iconKind = iconKind,
                size = 38.dp,
                cornerRadius = 11.dp,
                containerColor = if (isDark) Color(0xFF1A1C21) else IfxBlack,
                iconTint = accent,
                borderColor = if (isDark) Color.White.copy(alpha = 0.16f) else IfxBlack,
                borderWidth = 1.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = tag,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    color = if (isDark) IfxLime else Color(0xFF686D76)
                )
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDark) Color.White else IfxBlack
                )
            }
        }
    }
}

/**
 * Notebook tab overview shown when the user (or Guided App Tour) visits the Notebook tab.
 */
@Composable
fun NotebookTabSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Notebook",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.8).sp,
            color = IfxBlack
        )
        Text(
            text = "Smart notes automatically saved by your Sparks.",
            fontFamily = Inter,
            fontSize = 14.sp,
            color = Color(0xFF686D76)
        )

        Spacer(modifier = Modifier.height(4.dp))

        NotebookEntryCard(
            title = "Q4 Design Review Deadline",
            source = "Saved by Deadline Email · 12m ago",
            preview = "Deliver final onboarding screens and navigation map by Friday 5:00 PM."
        )
        NotebookEntryCard(
            title = "Sunday Weekly Review",
            source = "Saved by Weekly Review · Yesterday",
            preview = "9 tasks completed, 3 upcoming deadlines synced to Calendar."
        )
    }
}

@Composable
private fun NotebookEntryCard(
    title: String,
    source: String,
    preview: String
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, Color(0xFFE4E7EC)),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = IfxBlack
            )
            Text(
                text = source,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color(0xFF686D76)
            )
            Text(
                text = preview,
                fontFamily = Inter,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                color = Color(0xFF374151)
            )
        }
    }
}

/**
 * History tab overview shown when the user (or Guided App Tour) visits the History tab.
 */
@Composable
fun HistoryTabSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "History",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.8).sp,
            color = IfxBlack
        )
        Text(
            text = "Step-by-step run traces for every Spark execution.",
            fontFamily = Inter,
            fontSize = 14.sp,
            color = Color(0xFF686D76)
        )

        Spacer(modifier = Modifier.height(4.dp))

        HistoryRunCard(
            sparkName = "Deadline Email",
            timestamp = "12m ago · 0.4s",
            status = "PASSED"
        )
        HistoryRunCard(
            sparkName = "Morning Briefing",
            timestamp = "2h ago · 0.3s",
            status = "PASSED"
        )
        HistoryRunCard(
            sparkName = "Routine Reminder",
            timestamp = "Today · 10:00 AM",
            status = "PASSED"
        )
    }
}

@Composable
private fun HistoryRunCard(
    sparkName: String,
    timestamp: String,
    status: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, Color(0xFFE4E7EC)),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(IfxLime)
                        .border(1.2.dp, IfxBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = status,
                        tint = IfxBlack,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = sparkName,
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = IfxBlack
                    )
                    Text(
                        text = timestamp,
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = Color(0xFF686D76)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(IfxBlack)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = status,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = IfxLime
                )
            }
        }
    }
}
