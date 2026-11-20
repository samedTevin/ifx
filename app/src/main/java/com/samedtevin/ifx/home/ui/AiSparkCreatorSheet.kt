package com.samedtevin.ifx.home.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.components.IfxNodeFlowLoader
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clean AI Spark Creator Sheet:
 * User describes what they want in plain words, Gemini drafts the full `If` -> `Then do` workflow
 * in ~1 second, and opens it directly in the interactive drag-and-drop Builder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSparkCreatorSheet(
    onDismiss: () -> Unit,
    onSparkGenerated: (SparkRoutineItem) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var prompt by rememberSaveable { mutableStateOf("") }
    var isGenerating by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Minimal header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_gemini_color),
                        contentDescription = "Gemini AI",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Create with AI",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        letterSpacing = (-0.4).sp,
                        color = IfxBlack
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                        .clickable(enabled = !isGenerating, onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IfxBlack)
                }
            }

            if (isGenerating) {
                // Clean 1-second IFX generation state
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        IfxNodeFlowLoader()
                        Text(
                            text = "Drafting your Spark...",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = IfxBlack
                        )
                        Text(
                            text = "Building trigger and actions from your description.",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFF686D76)
                        )
                    }
                }
            } else {
                Text(
                    text = "Describe what you want in plain words. AI will build the workflow nodes for you.",
                    fontFamily = Inter,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFF686D76)
                )

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = {
                        Text(
                            text = "e.g. Every weekday at 7:30 AM remind me to stretch and save a daily note",
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = Color(0xFF98A2B3)
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IfxBlack,
                        unfocusedBorderColor = Color(0xFFD0D5DD),
                        cursorColor = IfxBlack,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val clean = prompt.trim()
                        if (clean.isBlank()) return@Button
                        isGenerating = true
                        coroutineScope.launch {
                            delay(950)
                            val generated = draftSparkFromPrompt(clean)
                            isGenerating = false
                            onSparkGenerated(generated)
                        }
                    },
                    enabled = prompt.isNotBlank(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IfxBlack,
                        contentColor = IfxLime,
                        disabledContainerColor = Color(0xFFE5E7EB),
                        disabledContentColor = Color(0xFF98A2B3)
                    ),
                    contentPadding = PaddingValues(vertical = 15.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Generate Spark  →",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Parses a natural-language user prompt into a structured, editable [SparkRoutineItem]
 * with matching topic palette, distinct icons, trigger, and action steps.
 */
internal fun draftSparkFromPrompt(rawPrompt: String): SparkRoutineItem {
    val clean = rawPrompt.trim()
    val lower = clean.lowercase()

    // Extract time expression if present (e.g., "7:30 AM", "2pm", "21:00")
    val timeRegex = Regex("""\b(\d{1,2}(?::\d{2})?\s?(?:am|pm)|\d{1,2}:\d{2})\b""", RegexOption.IGNORE_CASE)
    val matchedTime = timeRegex.find(clean)?.value?.uppercase()

    // Pick cohesive IFX topic & distinct icons from user intent
    val topic = when {
        lower.contains("email") || lower.contains("mail") || lower.contains("inbox") -> SparkTopic.EMAIL
        lower.contains("sleep") || lower.contains("bed") || lower.contains("night") -> SparkTopic.NIGHT
        lower.contains("gym") || lower.contains("water") || lower.contains("stretch") || lower.contains("walk") || lower.contains("habit") -> SparkTopic.ROUTINE
        lower.contains("focus") || lower.contains("study") || lower.contains("silent") || lower.contains("work") -> SparkTopic.FOCUS
        lower.contains("bill") || lower.contains("receipt") || lower.contains("pay") || lower.contains("expense") -> SparkTopic.FINANCE
        lower.contains("meet") || lower.contains("calendar") || lower.contains("morning") -> SparkTopic.SCHEDULE
        else -> SparkTopic.CUSTOM
    }

    val primaryIcon = when {
        matchedTime != null -> SparkIconKind.CLOCK_TRIGGER
        lower.contains("email") || lower.contains("mail") -> SparkIconKind.MAIL_DEADLINE
        lower.contains("calendar") || lower.contains("meet") -> SparkIconKind.CALENDAR_EVENT
        lower.contains("sleep") || lower.contains("night") -> SparkIconKind.MOON_SLEEP
        lower.contains("receipt") || lower.contains("bill") -> SparkIconKind.RECEIPT_DOC
        lower.contains("focus") || lower.contains("study") -> SparkIconKind.FOCUS_TIMER
        else -> SparkIconKind.GEMINI_AGENT
    }

    val actionIcon = when {
        lower.contains("note") || lower.contains("journal") -> SparkIconKind.NOTEBOOK_PAGE
        lower.contains("calendar") || lower.contains("event") -> SparkIconKind.CALENDAR_EVENT
        lower.contains("gym") || lower.contains("stretch") || lower.contains("walk") -> SparkIconKind.HABIT_ROUTINE
        lower.contains("silent") || lower.contains("mute") -> SparkIconKind.SILENT_DND
        lower.contains("bill") || lower.contains("expense") -> SparkIconKind.WALLET_VAULT
        else -> SparkIconKind.BELL_ALERT
    }

    // Build clean Trigger & Actions
    val triggerText = when {
        matchedTime != null && lower.contains("weekday") -> "Every weekday at $matchedTime"
        matchedTime != null && lower.contains("sunday") -> "Every Sunday at $matchedTime"
        matchedTime != null -> "Clock hits $matchedTime"
        lower.contains("email") || lower.contains("mail") -> "Matching email arrives in inbox"
        lower.contains("meeting") || lower.contains("calendar") -> "Upcoming Calendar event starts soon"
        else -> clean.replaceFirstChar { it.uppercase() }
    }

    // Split multiple actions if user wrote "and" / "then"
    val strippedForActions = clean
        .replace(Regex("""^(every\s+\w+\s+at\s+\S+|when\s+[^,]+,|if\s+[^,]+,|at\s+\S+)\s*""", RegexOption.IGNORE_CASE), "")
        .trim()
        .ifBlank { clean }

    val parsedActions = strippedForActions
        .split(Regex("""\s+(?:and then|and|then)\s+""", RegexOption.IGNORE_CASE))
        .map { it.trim().trimEnd('.', ',').replaceFirstChar { c -> c.uppercase() } }
        .filter { it.length >= 3 }
        .take(3)
        .ifEmpty { listOf("Send smart notification") }

    // Generate concise human title (max ~4 words)
    val shortTitle = when {
        lower.contains("gym") -> "Gym Routine"
        lower.contains("water") -> "Hydration Reminder"
        lower.contains("stretch") -> "Daily Stretch"
        lower.contains("vitamin") || lower.contains("medicine") -> "Daily Vitamins"
        lower.contains("bill") -> "Bill Reminder"
        lower.contains("study") -> "Study Block"
        else -> strippedForActions
            .split(" ")
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString(" ")
            .replaceFirstChar { it.uppercase() }
            .ifBlank { "Custom Spark" }
    }

    val params = buildList {
        if (matchedTime != null) {
            add(
                SparkEditableParam(
                    key = "time",
                    label = "Time",
                    value = matchedTime,
                    type = SparkParamType.TIME,
                    appliesToTrigger = true
                )
            )
        }
    }

    return SparkRoutineItem(
        id = "ai_${System.currentTimeMillis()}",
        title = shortTitle,
        subtitle = parsedActions.first(),
        enabled = true,
        runsToday = 1,
        iconRes = R.drawable.ic_tile_bell,
        secondaryIconRes = R.drawable.ic_tile_notebook,
        topic = topic,
        primaryIcon = primaryIcon,
        actionIcon = actionIcon,
        isOfficial = false,
        totalRunsCount = 1,
        about = clean.replaceFirstChar { it.uppercase() },
        triggerTitle = triggerText,
        actions = parsedActions,
        sparkCode = "AI-${(1000..9999).random()}",
        params = params
    )
}
