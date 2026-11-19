package com.samedtevin.ifx.home.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.R
import com.samedtevin.ifx.data.UserPreferencesDataStore
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter
import com.samedtevin.ifx.welcome.SavedAccountInfo
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SparkParamType {
    TIME,
    DAY,
    KEYWORD,
    ACTIVITY,
    DURATION,
    DESTINATION
}

/**
 * Editable parameter for an IFX Official Spark so users can customize inputs (e.g., time 10:00 AM -> 2:00 PM,
 * routine "Go to school" -> "Go to gym") while keeping the core automation flow reliable & engine-ready.
 */
data class SparkEditableParam(
    val key: String,
    val label: String,
    val value: String,
    val type: SparkParamType,
    val appliesToTrigger: Boolean = true,
    val presets: List<String> = emptyList()
) : Serializable

/**
 * Data model representing a customizable Spark automation card + its flowchart & live countable state.
 */
data class SparkRoutineItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val enabled: Boolean = true,
    val runsToday: Int = 1,
    val iconRes: Int = R.drawable.ic_tile_mail,
    val secondaryIconRes: Int = R.drawable.ic_tile_cal,
    val topic: SparkTopic = SparkTopic.CUSTOM,
    val primaryIcon: SparkIconKind = SparkIconKind.GEMINI_AGENT,
    val actionIcon: SparkIconKind = SparkIconKind.NOTEBOOK_PAGE,
    val isOfficial: Boolean = false,
    val activeUsersCount: Int = 1,
    val totalRunsCount: Int = 0,
    val about: String = "Runs automatically when your trigger matches.",
    val triggerTitle: String = "New event arrives",
    val triggerTemplate: String = "",
    val actions: List<String> = listOf("Run automated action"),
    val actionTemplates: List<String> = emptyList(),
    val sparkCode: String = "IFX-1042",
    val params: List<SparkEditableParam> = emptyList()
) : Serializable {

    private fun interpolate(template: String): String {
        var result = template
        params.forEach { param ->
            result = result.replace("{${param.key}}", param.value)
        }
        return result
    }

    fun resolvedTriggerText(): String {
        return if (triggerTemplate.isNotBlank()) interpolate(triggerTemplate) else triggerTitle
    }

    fun resolvedActions(): List<String> {
        return if (actionTemplates.isNotEmpty()) {
            actionTemplates.map { interpolate(it) }
        } else {
            actions
        }
    }

    fun withSyncedSubtitle(): SparkRoutineItem {
        if (!isOfficial || params.isEmpty()) return this
        val summary = params.joinToString(" · ") { it.value }
        return copy(subtitle = summary)
    }
}

/**
 * Data model representing an interactive notification item in the Notification Center.
 */
data class SparkNotificationItem(
    val id: String,
    val sparkTitle: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val iconRes: Int = R.drawable.ic_tile_mail
) : Serializable

enum class HomeNavTab(val label: String) {
    HOME("Home"),
    PROJECTS("Projects"),
    BUILDER("Builder"),
    NOTEBOOK("Notebook"),
    HISTORY("History")
}

/**
 * 8 foundational Sparks created by IFX. Each carries structured [SparkEditableParam] keys
 * ready to bind directly to background workers/schedulers later. Users can customize inputs
 * (time, routine, keyword, etc.), while fully editing only Sparks they create themselves.
 */
private fun defaultPreferenceSparks(): List<SparkRoutineItem> = listOf(
    SparkRoutineItem(
        id = "deadline_email",
        title = "Deadline Email",
        subtitle = "deadline · 1 day before",
        enabled = true,
        runsToday = 3,
        iconRes = R.drawable.ic_tile_mail,
        secondaryIconRes = R.drawable.ic_tile_cal,
        topic = SparkTopic.EMAIL,
        primaryIcon = SparkIconKind.MAIL_DEADLINE,
        actionIcon = SparkIconKind.CALENDAR_EVENT,
        isOfficial = true,
        activeUsersCount = 1428,
        about = "Watches your inbox for deadline emails and saves them to Calendar and Notebook automatically.",
        triggerTitle = "Email arrives containing deadline",
        triggerTemplate = "Email arrives containing \"{keyword}\"",
        actions = listOf("Add to Calendar & save a note"),
        actionTemplates = listOf(
            "Add Calendar reminder ({lead_time})",
            "Save summary to Notebook"
        ),
        sparkCode = "IFX-DL01",
        params = listOf(
            SparkEditableParam(
                key = "keyword",
                label = "Keyword",
                value = "deadline",
                type = SparkParamType.KEYWORD,
                appliesToTrigger = true,
                presets = listOf("deadline", "due date", "exam", "submission")
            ),
            SparkEditableParam(
                key = "lead_time",
                label = "Reminder",
                value = "1 day before",
                type = SparkParamType.DURATION,
                appliesToTrigger = false,
                presets = listOf("1 hour before", "1 day before", "3 days before")
            )
        )
    ),
    SparkRoutineItem(
        id = "morning_briefing",
        title = "Morning Briefing",
        subtitle = "08:00 AM · Weekdays",
        enabled = true,
        runsToday = 1,
        iconRes = R.drawable.ic_tile_bell,
        secondaryIconRes = R.drawable.ic_tile_cal,
        topic = SparkTopic.SCHEDULE,
        primaryIcon = SparkIconKind.SUN_BRIEFING,
        actionIcon = SparkIconKind.BELL_ALERT,
        isOfficial = true,
        activeUsersCount = 2914,
        about = "Sends one short briefing notification with your upcoming events and priorities.",
        triggerTitle = "Clock hits 08:00 AM",
        triggerTemplate = "Clock hits {time} ({repeat})",
        actions = listOf("Send daily briefing notification"),
        actionTemplates = listOf("Send daily briefing notification"),
        sparkCode = "IFX-MB02",
        params = listOf(
            SparkEditableParam(
                key = "time",
                label = "Time",
                value = "08:00 AM",
                type = SparkParamType.TIME,
                appliesToTrigger = true,
                presets = listOf("07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM")
            ),
            SparkEditableParam(
                key = "repeat",
                label = "Repeat",
                value = "Weekdays",
                type = SparkParamType.DAY,
                appliesToTrigger = true,
                presets = listOf("Daily", "Weekdays", "Weekends")
            )
        )
    ),
    SparkRoutineItem(
        id = "routine_reminder",
        title = "Routine Reminder",
        subtitle = "10:00 AM · Go to school",
        enabled = true,
        runsToday = 1,
        iconRes = R.drawable.ic_tile_bell,
        secondaryIconRes = R.drawable.ic_tile_notebook,
        topic = SparkTopic.ROUTINE,
        primaryIcon = SparkIconKind.CLOCK_TRIGGER,
        actionIcon = SparkIconKind.HABIT_ROUTINE,
        isOfficial = true,
        activeUsersCount = 3650,
        about = "Nudges you on schedule for any daily habit — switch the time or activity anytime.",
        triggerTitle = "Clock hits 10:00 AM",
        triggerTemplate = "Clock hits {time}",
        actions = listOf("Remind: Go to school"),
        actionTemplates = listOf("Send reminder: \"{activity}\""),
        sparkCode = "IFX-RR03",
        params = listOf(
            SparkEditableParam(
                key = "time",
                label = "Time",
                value = "10:00 AM",
                type = SparkParamType.TIME,
                appliesToTrigger = true,
                presets = listOf("08:00 AM", "10:00 AM", "02:00 PM", "06:00 PM", "08:00 PM")
            ),
            SparkEditableParam(
                key = "activity",
                label = "Activity",
                value = "Go to school",
                type = SparkParamType.ACTIVITY,
                appliesToTrigger = false,
                presets = listOf("Go to school", "Go to gym", "Drink water", "Walk outside", "Read 20 pages")
            )
        )
    ),
    SparkRoutineItem(
        id = "focus_session",
        title = "Focus Session",
        subtitle = "02:00 PM · 45 min · Deep Work",
        enabled = false,
        runsToday = 1,
        iconRes = R.drawable.ic_tile_cal,
        secondaryIconRes = R.drawable.ic_tile_bell,
        topic = SparkTopic.FOCUS,
        primaryIcon = SparkIconKind.FOCUS_TIMER,
        actionIcon = SparkIconKind.BELL_ALERT,
        isOfficial = true,
        activeUsersCount = 1892,
        about = "Starts a distraction-free block at your preferred time and notifies you when it ends.",
        triggerTitle = "Clock hits 02:00 PM",
        triggerTemplate = "Clock hits {time}",
        actions = listOf("Start 45 min Deep Work session"),
        actionTemplates = listOf("Start {duration} \"{label}\" session"),
        sparkCode = "IFX-FS04",
        params = listOf(
            SparkEditableParam(
                key = "time",
                label = "Start time",
                value = "02:00 PM",
                type = SparkParamType.TIME,
                appliesToTrigger = true,
                presets = listOf("09:00 AM", "02:00 PM", "04:00 PM", "08:00 PM")
            ),
            SparkEditableParam(
                key = "duration",
                label = "Duration",
                value = "45 min",
                type = SparkParamType.DURATION,
                appliesToTrigger = false,
                presets = listOf("25 min", "45 min", "60 min", "90 min")
            ),
            SparkEditableParam(
                key = "label",
                label = "Session",
                value = "Deep Work",
                type = SparkParamType.ACTIVITY,
                appliesToTrigger = false,
                presets = listOf("Deep Work", "Study", "Coding", "Reading")
            )
        )
    ),
    SparkRoutineItem(
        id = "meeting_headsup",
        title = "Meeting Heads-Up",
        subtitle = "15 min before · Primary",
        enabled = false,
        runsToday = 2,
        iconRes = R.drawable.ic_tile_cal,
        secondaryIconRes = R.drawable.ic_tile_notebook,
        topic = SparkTopic.SCHEDULE,
        primaryIcon = SparkIconKind.CALENDAR_EVENT,
        actionIcon = SparkIconKind.NOTEBOOK_PAGE,
        isOfficial = true,
        activeUsersCount = 1105,
        about = "Alerts you before Calendar events start and opens a quick note to capture takeaways.",
        triggerTitle = "Calendar event starts in 15 min",
        triggerTemplate = "{calendar} event starts {lead_time}",
        actions = listOf("Send heads-up & open prep note"),
        actionTemplates = listOf("Send heads-up & open prep note"),
        sparkCode = "IFX-MH05",
        params = listOf(
            SparkEditableParam(
                key = "lead_time",
                label = "Alert before",
                value = "15 min before",
                type = SparkParamType.DURATION,
                appliesToTrigger = true,
                presets = listOf("5 min before", "15 min before", "30 min before")
            ),
            SparkEditableParam(
                key = "calendar",
                label = "Calendar",
                value = "Primary",
                type = SparkParamType.DESTINATION,
                appliesToTrigger = true,
                presets = listOf("Primary", "Work", "University")
            )
        )
    ),
    SparkRoutineItem(
        id = "weekly_review",
        title = "Weekly Review",
        subtitle = "Sunday · 07:00 PM",
        enabled = false,
        runsToday = 1,
        iconRes = R.drawable.ic_tile_notebook,
        secondaryIconRes = R.drawable.ic_tile_bell,
        topic = SparkTopic.REVIEW,
        primaryIcon = SparkIconKind.WEEKLY_CHART,
        actionIcon = SparkIconKind.NOTEBOOK_PAGE,
        isOfficial = true,
        activeUsersCount = 946,
        about = "Compiles your completed tasks and saves a clean weekly summary in your Notebook.",
        triggerTitle = "Every Sunday at 07:00 PM",
        triggerTemplate = "Every {day} at {time}",
        actions = listOf("Save weekly summary to Notebook"),
        actionTemplates = listOf("Save weekly summary to Notebook"),
        sparkCode = "IFX-WR06",
        params = listOf(
            SparkEditableParam(
                key = "day",
                label = "Day",
                value = "Sunday",
                type = SparkParamType.DAY,
                appliesToTrigger = true,
                presets = listOf("Friday", "Saturday", "Sunday")
            ),
            SparkEditableParam(
                key = "time",
                label = "Time",
                value = "07:00 PM",
                type = SparkParamType.TIME,
                appliesToTrigger = true,
                presets = listOf("05:00 PM", "07:00 PM", "09:00 PM")
            )
        )
    ),
    SparkRoutineItem(
        id = "night_winddown",
        title = "Night Wind-Down",
        subtitle = "11:00 PM · Prepare for sleep",
        enabled = false,
        runsToday = 1,
        iconRes = R.drawable.ic_tile_bell,
        secondaryIconRes = R.drawable.ic_tile_notebook,
        topic = SparkTopic.NIGHT,
        primaryIcon = SparkIconKind.MOON_SLEEP,
        actionIcon = SparkIconKind.BELL_ALERT,
        isOfficial = true,
        activeUsersCount = 2318,
        about = "Reminds you to wind down on time so tomorrow starts rested and organized.",
        triggerTitle = "Clock hits 11:00 PM",
        triggerTemplate = "Clock hits {time}",
        actions = listOf("Remind: Prepare for sleep"),
        actionTemplates = listOf("Remind: \"{activity}\""),
        sparkCode = "IFX-NW07",
        params = listOf(
            SparkEditableParam(
                key = "time",
                label = "Bedtime",
                value = "11:00 PM",
                type = SparkParamType.TIME,
                appliesToTrigger = true,
                presets = listOf("10:00 PM", "10:30 PM", "11:00 PM", "11:30 PM")
            ),
            SparkEditableParam(
                key = "activity",
                label = "Reminder",
                value = "Prepare for sleep",
                type = SparkParamType.ACTIVITY,
                appliesToTrigger = false,
                presets = listOf("Prepare for sleep", "Plan tomorrow", "Charge phone", "Read in bed")
            )
        )
    ),
    SparkRoutineItem(
        id = "receipt_tracker",
        title = "Receipt Tracker",
        subtitle = "receipt · Expenses",
        enabled = false,
        runsToday = 1,
        iconRes = R.drawable.ic_tile_mail,
        secondaryIconRes = R.drawable.ic_tile_notebook,
        topic = SparkTopic.FINANCE,
        primaryIcon = SparkIconKind.RECEIPT_DOC,
        actionIcon = SparkIconKind.NOTEBOOK_PAGE,
        isOfficial = true,
        activeUsersCount = 784,
        about = "Catches purchase confirmations in your inbox and logs them neatly in your Notebook.",
        triggerTitle = "Email arrives containing receipt",
        triggerTemplate = "Email arrives containing \"{keyword}\"",
        actions = listOf("Save entry to Expenses Notebook"),
        actionTemplates = listOf("Log entry in \"{folder}\" Notebook"),
        sparkCode = "IFX-RT08",
        params = listOf(
            SparkEditableParam(
                key = "keyword",
                label = "Keyword",
                value = "receipt",
                type = SparkParamType.KEYWORD,
                appliesToTrigger = true,
                presets = listOf("receipt", "invoice", "order", "payment")
            ),
            SparkEditableParam(
                key = "folder",
                label = "Notebook",
                value = "Expenses",
                type = SparkParamType.DESTINATION,
                appliesToTrigger = false,
                presets = listOf("Expenses", "Subscriptions", "Personal")
            )
        )
    )
)

private fun defaultNotifications(): List<SparkNotificationItem> = listOf(
    SparkNotificationItem(
        id = "notif_1",
        sparkTitle = "Deadline Email",
        message = "Detected 'Q4 Design Review' due Friday at 5:00 PM — added to Calendar & Notebook.",
        timestamp = "12m ago",
        isRead = false,
        iconRes = R.drawable.ic_tile_mail
    ),
    SparkNotificationItem(
        id = "notif_2",
        sparkTitle = "Morning Briefing",
        message = "Delivered your 8:00 AM daily briefing with 3 priority tasks and schedule overview.",
        timestamp = "2h ago",
        isRead = false,
        iconRes = R.drawable.ic_tile_bell
    ),
    SparkNotificationItem(
        id = "notif_3",
        sparkTitle = "Weekly Review",
        message = "Compiled 9 completed action items into your Sunday Weekly Review note.",
        timestamp = "Yesterday",
        isRead = true,
        iconRes = R.drawable.ic_tile_notebook
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    account: SavedAccountInfo? = null,
    initialEmptyState: Boolean = true,
    onAccountUpdated: (SavedAccountInfo) -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSwitchAccountClick: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val isGuest = account == null || account.provider.equals("Guest", ignoreCase = true)

    // Two-state home flow: State 1 = Empty State, State 2 = Specialized Sparks State
    var isEmptyState by rememberSaveable { mutableStateOf(initialEmptyState) }
    var sparks by rememberSaveable { mutableStateOf(defaultPreferenceSparks()) }
    var selectedSparkId by rememberSaveable { mutableStateOf<String?>("deadline_email") }
    var detailSparkId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingSparkId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAllSparks by rememberSaveable { mutableStateOf(false) }
    var geminiSuggestionAdded by rememberSaveable { mutableStateOf(false) }

    // Interactive Notifications state (supports Read/Unread toggle, Mark all read, Show All vs Unread filter)
    var notifications by rememberSaveable { mutableStateOf(defaultNotifications()) }
    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }

    var routinePrompt by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf(HomeNavTab.HOME) }

    // Modal sheets for Profile/Photo customization and New Spark / Preference customization
    var showProfileSheet by rememberSaveable { mutableStateOf(false) }
    var showSparkBuilderSheet by rememberSaveable { mutableStateOf(false) }

    // Guided App Tour state (shown once to newcomers; skipped/completed persisted in DataStore)
    var showTourInviteDialog by rememberSaveable { mutableStateOf(false) }
    var isTourActive by rememberSaveable { mutableStateOf(false) }
    var tourStepIndex by rememberSaveable { mutableIntStateOf(0) }
    var wasEmptyBeforeTour by rememberSaveable { mutableStateOf(initialEmptyState) }

    LaunchedEffect(Unit) {
        val prefs = UserPreferencesDataStore.readPreferences(context)
        if (!prefs.hasSeenAppTour) {
            showTourInviteDialog = true
        }
    }

    // Automatically direct user to each tab & state as they step through the Guided App Tour
    LaunchedEffect(isTourActive, tourStepIndex) {
        if (isTourActive) {
            val step = AppTourCatalog.steps.getOrNull(tourStepIndex) ?: return@LaunchedEffect
            selectedTab = step.targetTab
            if (step.targetTab == HomeNavTab.HOME) {
                isEmptyState = !step.showSampleSparksOnHome && wasEmptyBeforeTour
            }
        }
    }

    // Dynamic profile photo state: uses account.photoUri or studio avatar preset, falling back to initials placeholder
    var customPhotoUri by rememberSaveable(account?.photoUri) { mutableStateOf(account?.photoUri) }
    var studioAvatarIndex by rememberSaveable { mutableIntStateOf(0) }
    var customDisplayName by rememberSaveable(account?.fullName) {
        mutableStateOf(
            when {
                isGuest -> "Alex"
                account.firstName.isNotBlank() -> account.firstName
                else -> "Alex"
            }
        )
    }

    val currentInitials = remember(customDisplayName, account?.initials) {
        if (account != null && account.initials.isNotBlank() && customDisplayName == account.firstName) {
            account.initials
        } else {
            SavedAccountInfo.computeInitials(customDisplayName)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            val uriStr = uri.toString()
            customPhotoUri = uriStr
            studioAvatarIndex = 0
            coroutineScope.launch {
                UserPreferencesDataStore.updateProfilePhoto(context, uriStr)
            }
            if (account != null) {
                onAccountUpdated(account.copy(photoUri = uriStr))
            }
            Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
        }
    }

    val formattedDate = remember {
        SimpleDateFormat("EEEE, d MMM", Locale.ENGLISH).format(Date())
    }

    val activeSparksCount = sparks.count { it.enabled }
    val totalRunsToday = sparks.filter { it.enabled }.sumOf { it.runsToday }
    val unreadNotificationsCount = notifications.count { !it.isRead }

    fun toggleSparkEnabled(sparkId: String, targetEnabled: Boolean) {
        sparks = sparks.map { item ->
            if (item.id == sparkId && item.enabled != targetEnabled) {
                val updatedUsers = if (item.isOfficial) {
                    (item.activeUsersCount + if (targetEnabled) 1 else -1).coerceAtLeast(0)
                } else {
                    item.activeUsersCount
                }
                val updatedRuns = if (!item.isOfficial && targetEnabled) {
                    item.totalRunsCount + 1
                } else {
                    item.totalRunsCount
                }
                item.copy(
                    enabled = targetEnabled,
                    activeUsersCount = updatedUsers,
                    totalRunsCount = updatedRuns
                )
            } else {
                item
            }
        }
    }

    fun activateSpecializedState(preferredSparkId: String? = null, customPromptText: String? = null) {
        if (!customPromptText.isNullOrBlank()) {
            val cleanPrompt = customPromptText.trim()
            val newId = "custom_${System.currentTimeMillis()}"
            val iconForPrompt = when {
                cleanPrompt.contains("mail", ignoreCase = true) || cleanPrompt.contains("email", ignoreCase = true) ->
                    R.drawable.ic_tile_mail
                cleanPrompt.contains("note", ignoreCase = true) || cleanPrompt.contains("review", ignoreCase = true) ->
                    R.drawable.ic_tile_notebook
                cleanPrompt.contains("cal", ignoreCase = true) || cleanPrompt.contains("meet", ignoreCase = true) ->
                    R.drawable.ic_tile_cal
                else -> R.drawable.ic_tile_bell
            }
            val newSpark = SparkRoutineItem(
                id = newId,
                title = cleanPrompt.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                subtitle = "Custom Spark · by You",
                enabled = true,
                runsToday = 1,
                iconRes = iconForPrompt,
                secondaryIconRes = R.drawable.ic_tile_notebook,
                isOfficial = false,
                totalRunsCount = 1,
                about = "Created by you: \"$cleanPrompt\". Open in Builder anytime to edit every step.",
                triggerTitle = "When \"$cleanPrompt\" triggers",
                actions = listOf(
                    "Run your routine action",
                    "Save log to History"
                ),
                sparkCode = "USR-${(1000..9999).random()}"
            )
            sparks = listOf(newSpark) + sparks
            selectedSparkId = newId
            notifications = listOf(
                SparkNotificationItem(
                    id = "notif_${System.currentTimeMillis()}",
                    sparkTitle = newSpark.title,
                    message = "New Spark '${newSpark.title}' is now active and watching.",
                    timestamp = "Just now",
                    isRead = false,
                    iconRes = iconForPrompt
                )
            ) + notifications
        } else if (preferredSparkId != null) {
            toggleSparkEnabled(preferredSparkId, true)
            selectedSparkId = preferredSparkId
        }
        isEmptyState = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Full-Screen Architectural Square Grid Background (matching uploaded grid screenshot)
        IfxSquareGridBackground(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val activeDetailSpark = detailSparkId?.let { id -> sparks.firstOrNull { it.id == id } }
                val activeBuilderSpark = if (selectedTab == HomeNavTab.BUILDER) {
                    editingSparkId?.let { id -> sparks.firstOrNull { it.id == id } }
                } else {
                    null
                }

                if (activeDetailSpark != null) {
                    // Solid-background editorial Spark Detail page (NO grid background)
                    SparkDetailScreen(
                        spark = activeDetailSpark,
                        onBackClick = { detailSparkId = null },
                        onToggleEnabled = { isEnabled ->
                            toggleSparkEnabled(activeDetailSpark.id, isEnabled)
                        },
                        onEditInBuilderClick = {
                            editingSparkId = activeDetailSpark.id
                            detailSparkId = null
                            selectedTab = HomeNavTab.BUILDER
                        }
                    )
                } else if (activeBuilderSpark != null) {
                    // Interactive Drag-and-Drop Flowchart Canvas directly on IFX's existing grid background
                    SparkBuilderEditor(
                        spark = activeBuilderSpark,
                        onSaveSpark = { updated ->
                            sparks = sparks.map { item ->
                                if (item.id == updated.id) updated else item
                            }
                            selectedSparkId = updated.id
                            editingSparkId = null
                            selectedTab = HomeNavTab.HOME
                            detailSparkId = updated.id
                        },
                        onDeleteSpark = { target ->
                            val remaining = sparks.filterNot { it.id == target.id }
                            sparks = remaining
                            editingSparkId = null
                            if (selectedSparkId == target.id) {
                                selectedSparkId = remaining.firstOrNull()?.id
                            }
                            if (remaining.isEmpty()) {
                                isEmptyState = true
                            }
                            selectedTab = HomeNavTab.HOME
                        },
                        onBackToDetail = { target ->
                            editingSparkId = null
                            selectedTab = HomeNavTab.HOME
                            detailSparkId = target.id
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp)
                            .padding(
                                top = 10.dp,
                                bottom = if (isTourActive) 220.dp else 84.dp
                            )
                    ) {
                    // 1. Shared Top Bar: Dynamic Profile Photo (with Initials Placeholder) + Notification Bell
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileAvatarButton(
                            photoUri = customPhotoUri,
                            studioAvatarIndex = studioAvatarIndex,
                            initialsPlaceholder = currentInitials.ifBlank { "AM" },
                            onClick = { showProfileSheet = true }
                        )

                        NotificationBellButton(
                            unreadCount = unreadNotificationsCount,
                            onClick = { showNotificationsSheet = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Switch smoothly between the 5 main tab pages (also driven live by the Guided App Tour)
                    Crossfade(
                        targetState = selectedTab,
                        animationSpec = tween(durationMillis = 220),
                        label = "mainTabCrossfade"
                    ) { activeTab ->
                        when (activeTab) {
                            HomeNavTab.HOME -> {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // 2. Date & Greeting Header
                                    Text(
                                        text = formattedDate,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.5.sp,
                                        color = Color(0xFF686D76)
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "Hi, $customDisplayName",
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 34.sp,
                                        lineHeight = 38.sp,
                                        letterSpacing = (-1).sp,
                                        color = IfxBlack
                                    )

                                    // Redesigned Creative Telemetry Strip for "Sparks watching · runs today"
                                    AnimatedVisibility(
                                        visible = !isEmptyState,
                                        enter = fadeIn(tween(220)) + expandVertically(tween(220)),
                                        exit = fadeOut(tween(180)) + shrinkVertically(tween(180))
                                    ) {
                                        SparksWatchingTelemetryBar(
                                            activeSparksCount = activeSparksCount,
                                            totalRunsToday = totalRunsToday,
                                            onClick = { showNotificationsSheet = true },
                                            modifier = Modifier.padding(top = 10.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 3. 3D Elevated Routine Command Pill with official Gemini SVG Logo
                                    RoutinePromptBar(
                                        value = routinePrompt,
                                        onValueChange = { routinePrompt = it },
                                        onSubmit = {
                                            keyboardController?.hide()
                                            if (routinePrompt.isNotBlank()) {
                                                val submitted = routinePrompt
                                                routinePrompt = ""
                                                activateSpecializedState(customPromptText = submitted)
                                                Toast.makeText(context, "Spark created: $submitted", Toast.LENGTH_SHORT).show()
                                            } else {
                                                showSparkBuilderSheet = true
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // 4. Crossfade between State 1 (Empty State) and State 2 (Specialized User Sparks State)
                                    Crossfade(
                                        targetState = isEmptyState,
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                        label = "homeEmptyVsSpecializedState"
                                    ) { empty ->
                                        if (empty) {
                                            HomeEmptyStateSection(
                                                onPresetChipClick = { presetId, presetTitle ->
                                                    activateSpecializedState(preferredSparkId = presetId)
                                                    Toast.makeText(
                                                        context,
                                                        "$presetTitle activated in Your Sparks",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        } else {
                                            HomeSpecializedSparksSection(
                                                sparks = sparks,
                                                selectedSparkId = selectedSparkId,
                                                showAllSparks = showAllSparks,
                                                showGeminiSuggestion = !geminiSuggestionAdded,
                                                onSparkClick = { clickedSpark ->
                                                    selectedSparkId = clickedSpark.id
                                                    detailSparkId = clickedSpark.id
                                                },
                                                onSparkToggle = { toggledSpark, isEnabled ->
                                                    toggleSparkEnabled(toggledSpark.id, isEnabled)
                                                },
                                                onSeeAllClick = {
                                                    showAllSparks = !showAllSparks
                                                },
                                                onAddGeminiSuggestion = {
                                                    val suggestedId = "gemini_deadline_notes"
                                                    val newSpark = SparkRoutineItem(
                                                        id = suggestedId,
                                                        title = "Deadline Notes Sync",
                                                        subtitle = "Saves deadline emails as Notebook notes",
                                                        enabled = true,
                                                        runsToday = 1,
                                                        iconRes = R.drawable.ic_tile_cal,
                                                        secondaryIconRes = R.drawable.ic_tile_notebook,
                                                        isOfficial = false,
                                                        totalRunsCount = 1,
                                                        about = "Drafted from Gemini's suggestion to save deadline emails into Notebook notes.",
                                                        triggerTitle = "Deadline email arrives in inbox",
                                                        actions = listOf("Save formatted note to Notebook"),
                                                        sparkCode = "USR-GM09"
                                                    )
                                                    sparks = listOf(newSpark) + sparks
                                                    selectedSparkId = suggestedId
                                                    geminiSuggestionAdded = true
                                                    notifications = listOf(
                                                        SparkNotificationItem(
                                                            id = "notif_gemini_${System.currentTimeMillis()}",
                                                            sparkTitle = "Deadline Notes Sync",
                                                            message = "Added Gemini-suggested routine to Your Sparks.",
                                                            timestamp = "Just now",
                                                            isRead = false,
                                                            iconRes = R.drawable.ic_tile_cal
                                                        )
                                                    ) + notifications
                                                    Toast.makeText(
                                                        context,
                                                        "Added Gemini suggestion to Your Sparks!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            HomeNavTab.PROJECTS -> {
                                ProjectsTabSection(
                                    onCreateSparkClick = { showSparkBuilderSheet = true }
                                )
                            }
                            HomeNavTab.BUILDER -> {
                                val sparkBeingEdited = editingSparkId?.let { id -> sparks.firstOrNull { it.id == id } }
                                BuilderTabSection(
                                    sparks = sparks,
                                    editingSpark = sparkBeingEdited,
                                    onSelectSparkToEdit = { chosen ->
                                        editingSparkId = chosen.id
                                    },
                                    onSaveSpark = { updated ->
                                        sparks = sparks.map { item ->
                                            if (item.id == updated.id) updated else item
                                        }
                                        selectedSparkId = updated.id
                                        editingSparkId = null
                                        selectedTab = HomeNavTab.HOME
                                        detailSparkId = updated.id
                                    },
                                    onDeleteSpark = { target ->
                                        val remaining = sparks.filterNot { it.id == target.id }
                                        sparks = remaining
                                        editingSparkId = null
                                        if (selectedSparkId == target.id) {
                                            selectedSparkId = remaining.firstOrNull()?.id
                                        }
                                        if (remaining.isEmpty()) {
                                            isEmptyState = true
                                        }
                                        selectedTab = HomeNavTab.HOME
                                    },
                                    onBackToDetail = { target ->
                                        editingSparkId = null
                                        selectedTab = HomeNavTab.HOME
                                        detailSparkId = target.id
                                    },
                                    onOpenBuilderClick = { showSparkBuilderSheet = true }
                                )
                            }
                            HomeNavTab.NOTEBOOK -> {
                                NotebookTabSection()
                            }
                            HomeNavTab.HISTORY -> {
                                HistoryTabSection()
                            }
                        }
                    }
                }

                // 5. 3D Floating Action Button ("+ New Spark") — hidden while active tour coachmark occupies bottom slot
                if (!isTourActive && selectedTab == HomeNavTab.HOME) {
                    NewSparkFab(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 22.dp, bottom = 18.dp),
                        onClick = {
                            if (isEmptyState) {
                                activateSpecializedState(preferredSparkId = "deadline_email")
                            } else {
                                showSparkBuilderSheet = true
                            }
                        }
                    )
                }

                // Active Guided App Tour Coachmark Overlay (directs user through each page)
                if (isTourActive) {
                    val activeStep = AppTourCatalog.steps.getOrElse(tourStepIndex) { AppTourCatalog.steps.first() }
                    AppTourCoachmarkCard(
                        step = activeStep,
                        totalSteps = AppTourCatalog.steps.size,
                        onPrevious = {
                            if (tourStepIndex > 0) {
                                tourStepIndex--
                            }
                        },
                        onNext = {
                            if (tourStepIndex < AppTourCatalog.steps.lastIndex) {
                                tourStepIndex++
                            } else {
                                isTourActive = false
                                selectedTab = HomeNavTab.HOME
                                isEmptyState = wasEmptyBeforeTour
                                coroutineScope.launch {
                                    UserPreferencesDataStore.setAppTourCompleted(context, true)
                                }
                                Toast.makeText(context, "Tour complete! You're ready to automate.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSkip = {
                            isTourActive = false
                            selectedTab = HomeNavTab.HOME
                            isEmptyState = wasEmptyBeforeTour
                            coroutineScope.launch {
                                UserPreferencesDataStore.setAppTourSkipped(context, true)
                            }
                            Toast.makeText(context, "Tour skipped. You can replay it anytime from Profile.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
                }
            }

            // 6. Full-Width Bottom Navigation Bar
            IfxBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    detailSparkId = null
                    selectedTab = tab
                }
            )
        }
    }

    // Newcomer Guided App Tour Invite Dialog ("Would you like a quick tour?")
    if (showTourInviteDialog) {
        AppTourInviteDialog(
            userName = customDisplayName,
            onStartTour = {
                showTourInviteDialog = false
                wasEmptyBeforeTour = isEmptyState
                tourStepIndex = 0
                isTourActive = true
            },
            onSkip = {
                showTourInviteDialog = false
                coroutineScope.launch {
                    UserPreferencesDataStore.setAppTourSkipped(context, true)
                }
            }
        )
    }

    // Interactive Notifications Center Modal Sheet (Read / Unread / Show All / Mark All Read)
    if (showNotificationsSheet) {
        NotificationsCenterSheet(
            notifications = notifications,
            onDismiss = { showNotificationsSheet = false },
            onToggleRead = { notifId ->
                notifications = notifications.map { item ->
                    if (item.id == notifId) item.copy(isRead = !item.isRead) else item
                }
            },
            onMarkAllRead = {
                notifications = notifications.map { it.copy(isRead = true) }
            },
            onClearAll = {
                notifications = emptyList()
            },
            onRestoreSampleNotifications = {
                notifications = defaultNotifications()
            }
        )
    }

    // Profile Photo & User Preferences Customization Modal Sheet
    if (showProfileSheet) {
        ProfileAndPreferencesSheet(
            displayName = customDisplayName,
            email = if (isGuest) "Local guest profile" else account.email,
            provider = if (isGuest) "Guest" else account.provider,
            initials = currentInitials.ifBlank { "AM" },
            photoUri = customPhotoUri,
            studioAvatarIndex = studioAvatarIndex,
            isEmptyState = isEmptyState,
            isGuest = isGuest,
            onDismiss = { showProfileSheet = false },
            onPickFromGallery = {
                photoPickerLauncher.launch("image/*")
            },
            onSelectStudioAvatar = { presetIndex ->
                studioAvatarIndex = presetIndex
                customPhotoUri = null
                coroutineScope.launch {
                    UserPreferencesDataStore.updateProfilePhoto(context, null)
                }
            },
            onUseInitialsPlaceholder = {
                studioAvatarIndex = 0
                customPhotoUri = null
                coroutineScope.launch {
                    UserPreferencesDataStore.updateProfilePhoto(context, null)
                }
                if (account != null) {
                    onAccountUpdated(account.copy(photoUri = null))
                }
                Toast.makeText(context, "Using initials placeholder ($currentInitials)", Toast.LENGTH_SHORT).show()
            },
            onUpdateDisplayName = { newName ->
                val clean = newName.trim()
                if (clean.isNotBlank()) {
                    customDisplayName = clean
                    coroutineScope.launch {
                        UserPreferencesDataStore.updateDisplayName(context, clean)
                    }
                    if (account != null) {
                        onAccountUpdated(
                            account.copy(
                                fullName = clean,
                                initials = SavedAccountInfo.computeInitials(clean)
                            )
                        )
                    }
                }
            },
            onToggleEmptyState = { targetEmpty ->
                isEmptyState = targetEmpty
                showProfileSheet = false
            },
            onReplayAppTour = {
                showProfileSheet = false
                wasEmptyBeforeTour = isEmptyState
                tourStepIndex = 0
                isTourActive = true
                coroutineScope.launch {
                    UserPreferencesDataStore.resetAppTour(context)
                }
            },
            onSwitchAccountClick = {
                showProfileSheet = false
                onSwitchAccountClick()
            },
            onLogoutClick = {
                showProfileSheet = false
                onLogoutClick()
            },
            onSignInClick = {
                showProfileSheet = false
                onSignInClick()
            }
        )
    }

    // New Spark & User Preference Customizer Modal Sheet
    if (showSparkBuilderSheet) {
        SparkCustomizationSheet(
            isEmptyState = isEmptyState,
            onDismiss = { showSparkBuilderSheet = false },
            onAddCustomSpark = { title, subtitle, chosenIconRes ->
                val newId = "spark_${System.currentTimeMillis()}"
                val cleanTitle = title.trim()
                val cleanSub = subtitle.trim().ifBlank { "Runs automatically in background" }
                val created = SparkRoutineItem(
                    id = newId,
                    title = cleanTitle,
                    subtitle = cleanSub,
                    enabled = true,
                    runsToday = 1,
                    iconRes = chosenIconRes,
                    secondaryIconRes = R.drawable.ic_tile_notebook,
                    isOfficial = false,
                    totalRunsCount = 1,
                    about = "$cleanTitle — $cleanSub. Open in Builder anytime to edit the full flowchart.",
                    triggerTitle = "Trigger for $cleanTitle",
                    actions = listOf(cleanSub),
                    sparkCode = "USR-${(1000..9999).random()}"
                )
                sparks = listOf(created) + sparks
                selectedSparkId = newId
                isEmptyState = false
                showSparkBuilderSheet = false
                Toast.makeText(context, "Added \"$title\"", Toast.LENGTH_SHORT).show()
            },
            onResetToEmptyState = {
                isEmptyState = true
                showSparkBuilderSheet = false
            },
            onRestoreDefaults = {
                sparks = defaultPreferenceSparks()
                selectedSparkId = "deadline_email"
                geminiSuggestionAdded = false
                isEmptyState = false
                showSparkBuilderSheet = false
            }
        )
    }
}

/**
 * Full-page architectural square grid background matching the uploaded grid reference image.
 */
@Composable
private fun IfxSquareGridBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cellSize = 52.dp.toPx()
        val gridLineColor = Color(0xFFE9ECEF)
        val strokeWidth = 1.dp.toPx()

        var x = cellSize * 0.7f
        while (x < size.width) {
            drawLine(
                color = gridLineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth
            )
            x += cellSize
        }

        var y = cellSize * 0.5f
        while (y < size.height) {
            drawLine(
                color = gridLineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
            y += cellSize
        }
    }
}

/**
 * Creative 3D Telemetry Bar replacing the plain "3 Sparks watching · 12 runs today" text.
 * Features a solid IfxBlack status badge with an Electric Lime indicator, active Spark counter,
 * and today's automation run count in a tactile segmented pill card.
 */
@Composable
private fun SparksWatchingTelemetryBar(
    activeSparksCount: Int,
    totalRunsToday: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E6EC)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // High-contrast Black + Electric Lime Active Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(IfxBlack)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (activeSparksCount > 0) IfxLime else Color(0xFF9CA3AF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$activeSparksCount WATCHING",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.4.sp,
                        color = if (activeSparksCount > 0) IfxLime else Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = if (activeSparksCount == 1) "Spark active" else "Sparks active",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = IfxBlack
                )
            }

            // Right Segment: Runs Today Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF4F6F8))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "$totalRunsToday",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.5.sp,
                    color = IfxBlack
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "runs today",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.5.sp,
                    color = Color(0xFF686D76)
                )
            }
        }
    }
}

/**
 * Dynamic Profile Avatar Button:
 * - Renders user's custom photo if `photoUri` or `studioAvatarIndex` is set.
 * - Falls back to the signature `IfxBlack` circle with `IfxLime` initials placeholder ("AM")
 *   when no profile photo is set.
 */
@Composable
private fun ProfileAvatarButton(
    photoUri: String?,
    studioAvatarIndex: Int,
    initialsPlaceholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loadedBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = photoUri) {
        value = if (photoUri.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(photoUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = IfxBlack,
        shadowElevation = 6.dp,
        border = BorderStroke(
            width = if (loadedBitmap != null || studioAvatarIndex > 0) 2.dp else 0.dp,
            color = IfxBlack
        ),
        modifier = modifier.size(46.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                loadedBitmap != null -> {
                    Image(
                        bitmap = loadedBitmap!!,
                        contentDescription = "User profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                studioAvatarIndex > 0 -> {
                    StudioAvatarIllustration(
                        presetIndex = studioAvatarIndex,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Text(
                        text = initialsPlaceholder.take(2).uppercase(),
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.5.sp,
                        letterSpacing = (-0.3).sp,
                        color = IfxLime
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioAvatarIllustration(
    presetIndex: Int,
    modifier: Modifier = Modifier
) {
    val bgColor = when (presetIndex) {
        1 -> IfxLime
        2 -> Color(0xFFDBEAFE)
        else -> Color(0xFFFDE68A)
    }
    val fgColor = IfxBlack
    Canvas(modifier = modifier.background(bgColor)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        drawCircle(
            color = fgColor,
            radius = size.minDimension * 0.19f,
            center = Offset(cx, cy - size.height * 0.08f)
        )
        drawArc(
            color = fgColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - size.width * 0.34f, cy + size.height * 0.14f),
            size = Size(size.width * 0.68f, size.height * 0.56f)
        )
    }
}

/**
 * Top-Right 3D Notification Bell Button using the clean `ic_notification_bell` outline vector
 * and displaying an Electric Lime unread badge when unread notifications exist.
 */
@Composable
private fun NotificationBellButton(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 5.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E6EC)),
        modifier = modifier.size(46.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_notification_bell),
                contentDescription = "Notifications",
                modifier = Modifier.size(21.dp)
            )

            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 9.dp)
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(IfxLime)
                        .border(1.5.dp, IfxBlack, CircleShape)
                )
            }
        }
    }
}

/**
 * 3D Black Pill Routine Command Bar ("Say or type a routine to automate...")
 * with the official `ic_gemini_color` SVG drawable on the left and Electric Lime arrow button on the right.
 */
@Composable
private fun RoutinePromptBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(36.dp),
        color = IfxBlack,
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_gemini_color),
                contentDescription = "Gemini",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Say or type a routine to\nautomate...",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFFB0B5BC)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(IfxLime),
                    maxLines = 2,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSubmit() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                onClick = onSubmit,
                shape = CircleShape,
                color = IfxLime,
                shadowElevation = 4.dp,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val stroke = 2.4.dp.toPx()
                        val w = size.width
                        val h = size.height
                        drawLine(
                            color = IfxBlack,
                            start = Offset(w * 0.15f, h * 0.5f),
                            end = Offset(w * 0.82f, h * 0.5f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        val arrowPath = Path().apply {
                            moveTo(w * 0.52f, h * 0.22f)
                            lineTo(w * 0.85f, h * 0.5f)
                            lineTo(w * 0.52f, h * 0.78f)
                        }
                        drawPath(
                            path = arrowPath,
                            color = IfxBlack,
                            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }
        }
    }
}

/**
 * State 1: Empty State ("Create your first Spark") matching Photo 2.
 */
@Composable
private fun HomeEmptyStateSection(
    onPresetChipClick: (presetId: String, presetTitle: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 34.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmptySparkSchematicIllustration(
            modifier = Modifier.size(width = 160.dp, height = 132.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Create your first Spark",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 23.sp,
            letterSpacing = (-0.4).sp,
            color = IfxBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Say a routine in plain words and Gemini drafts\nit for you.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = Color(0xFF686D76),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PresetRoutineChip(
                label = "Deadline Email",
                onClick = { onPresetChipClick("deadline_email", "Deadline Email") }
            )
            PresetRoutineChip(
                label = "Morning Briefing",
                onClick = { onPresetChipClick("morning_briefing", "Morning Briefing") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        PresetRoutineChip(
            label = "Weekly Review",
            onClick = { onPresetChipClick("weekly_review", "Weekly Review") }
        )
    }
}

@Composable
private fun PresetRoutineChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFF8E95A0)),
        modifier = modifier.height(44.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = IfxBlack
            )
        }
    }
}

@Composable
private fun EmptySparkSchematicIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeColor = Color(0xFFC5C9D0)
        val strokeWidth = 3.dp.toPx()

        val startCircleCenter = Offset(size.width * 0.20f, size.height * 0.14f)
        val circleRadius = 11.dp.toPx()
        val cornerX = size.width * 0.72f
        val cornerY = startCircleCenter.y
        val diamondCenterY = size.height * 0.50f
        val diamondHalf = 19.dp.toPx()
        val crossCenterY = size.height * 0.86f
        val crossHalf = 9.dp.toPx()

        drawCircle(
            color = strokeColor,
            radius = circleRadius,
            center = startCircleCenter
        )

        val connectorPath = Path().apply {
            moveTo(startCircleCenter.x + circleRadius, cornerY)
            lineTo(cornerX, cornerY)
            lineTo(cornerX, diamondCenterY - diamondHalf)
        }
        drawPath(
            path = connectorPath,
            color = strokeColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val diamondPath = Path().apply {
            moveTo(cornerX, diamondCenterY - diamondHalf)
            lineTo(cornerX + diamondHalf, diamondCenterY)
            lineTo(cornerX, diamondCenterY + diamondHalf)
            lineTo(cornerX - diamondHalf, diamondCenterY)
            close()
        }
        drawPath(
            path = diamondPath,
            color = strokeColor,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )

        val bottomLineEnd = crossCenterY - 2.dp.toPx()
        drawLine(
            color = strokeColor,
            start = Offset(cornerX, diamondCenterY + diamondHalf),
            end = Offset(cornerX, bottomLineEnd),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = strokeColor,
            start = Offset(cornerX - crossHalf, crossCenterY - crossHalf),
            end = Offset(cornerX + crossHalf, crossCenterY + crossHalf),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = strokeColor,
            start = Offset(cornerX + crossHalf, crossCenterY - crossHalf),
            end = Offset(cornerX - crossHalf, crossCenterY + crossHalf),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * State 2: Specialized User Preferences State ("Your Sparks") with individual 3D elevated cards,
 * clean semantic IFX tile icons (`ic_tile_mail`, `ic_tile_bell`, `ic_tile_notebook`, `ic_tile_cal`),
 * and Electric Lime selection state.
 */
@Composable
private fun HomeSpecializedSparksSection(
    sparks: List<SparkRoutineItem>,
    selectedSparkId: String?,
    showAllSparks: Boolean,
    showGeminiSuggestion: Boolean,
    onSparkClick: (SparkRoutineItem) -> Unit,
    onSparkToggle: (SparkRoutineItem, Boolean) -> Unit,
    onSeeAllClick: () -> Unit,
    onAddGeminiSuggestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedSparks = if (showAllSparks || sparks.size <= 4) sparks else sparks.take(4)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Your Sparks",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.4).sp,
                    color = IfxBlack
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(IfxLime)
                        .border(1.dp, IfxBlack.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 11.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${sparks.size}",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = IfxBlack
                    )
                }
            }

            if (sparks.size > 4) {
                Text(
                    text = if (showAllSparks) "Show less" else "See all (${sparks.size})",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                    color = Color(0xFF686D76),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onSeeAllClick)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            displayedSparks.forEach { spark ->
                val isSelected = spark.id == selectedSparkId
                Spark3DCard(
                    spark = spark,
                    isSelected = isSelected,
                    onClick = { onSparkClick(spark) },
                    onToggle = { enabled -> onSparkToggle(spark, enabled) }
                )
            }
        }

        AnimatedVisibility(
            visible = showGeminiSuggestion,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_gemini_color),
                    contentDescription = "Gemini",
                    modifier = Modifier.size(21.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                val suggestionText = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = IfxBlack)) {
                        append("Gemini suggests ")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color(0xFF686D76))) {
                        append("saving deadline emails\nas notes")
                    }
                }

                Text(
                    text = suggestionText,
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Add",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.5.sp,
                    color = IfxBlack,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAddGeminiSuggestion)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Individual 3D Elevated Card for a Spark routine:
 * - Uses clean IFX semantic icon tiles (`ic_tile_mail`, `ic_tile_bell`, `ic_tile_notebook`, `ic_tile_cal`).
 * - Clicking any Spark card transitions its surface to **Electric Lime (`#C6FF3F`)** with a crisp
 *   `2.dp` `IfxBlack` border and elevated 3D shadow.
 */
@Composable
private fun Spark3DCard(
    spark: SparkRoutineItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardContainerColor by animateColorAsState(
        targetValue = if (isSelected) IfxLime else Color.White,
        animationSpec = tween(durationMillis = 220),
        label = "sparkCardBgColor"
    )
    val cardBorderColor by animateColorAsState(
        targetValue = if (isSelected) IfxBlack else Color(0xFFE4E7EC),
        animationSpec = tween(durationMillis = 220),
        label = "sparkCardBorderColor"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 6.dp,
        animationSpec = tween(durationMillis = 220),
        label = "sparkCardElevation"
    )

    val subtitleColor = if (isSelected) {
        IfxBlack.copy(alpha = 0.78f)
    } else {
        Color(0xFF686D76)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = cardContainerColor,
        shadowElevation = cardElevation,
        tonalElevation = if (isSelected) 4.dp else 1.dp,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = cardBorderColor
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distinct Topic-Specific IFX Vector Glyph Tile
            SparkGlyphTile(
                iconKind = spark.primaryIcon,
                size = 50.dp,
                cornerRadius = 16.dp,
                containerColor = spark.topic.tileBg,
                iconTint = spark.topic.tileIcon,
                borderColor = IfxBlack.copy(alpha = 0.16f),
                borderWidth = 1.2.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = spark.title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.3).sp,
                    color = IfxBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = spark.subtitle,
                    fontFamily = Inter,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    color = subtitleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = if (spark.isOfficial) "by IFX" else "by You",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = if (isSelected) IfxBlack.copy(alpha = 0.7f) else Color(0xFF8C929D)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IfxSparkToggleSwitch(
                checked = spark.enabled,
                isCardSelected = isSelected,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun IfxSparkToggleSwitch(
    checked: Boolean,
    isCardSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = when {
            checked && isCardSelected -> Color.White
            checked -> IfxLime
            else -> Color.White
        },
        animationSpec = tween(180),
        label = "switchTrackColor"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            checked && isCardSelected -> IfxBlack
            checked -> IfxLime
            else -> Color(0xFF9CA3AF)
        },
        animationSpec = tween(180),
        label = "switchBorderColor"
    )

    Box(
        modifier = modifier
            .width(56.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 4.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(IfxBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Enabled",
                    tint = IfxLime,
                    modifier = Modifier.size(13.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF757A82))
            )
        }
    }
}

@Composable
private fun NewSparkFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = IfxLime,
        shadowElevation = 10.dp,
        border = BorderStroke(1.5.dp, IfxBlack),
        modifier = modifier.height(58.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(18.dp)) {
                val stroke = 2.5.dp.toPx()
                drawLine(
                    color = IfxBlack,
                    start = Offset(size.width / 2f, 0f),
                    end = Offset(size.width / 2f, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = IfxBlack,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "New Spark",
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.5.sp,
                letterSpacing = (-0.2).sp,
                color = IfxBlack
            )
        }
    }
}

@Composable
private fun IfxBottomNavigationBar(
    selectedTab: HomeNavTab,
    onTabSelected: (HomeNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = IfxBlack,
        shadowElevation = 14.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeNavTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = IfxLime),
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(if (isSelected) IfxLime else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        BottomNavIcon(
                            tab = tab,
                            tint = if (isSelected) IfxBlack else Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tab.label,
                        fontFamily = Inter,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavIcon(
    tab: HomeNavTab,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.9.dp.toPx()

        when (tab) {
            HomeNavTab.HOME -> {
                val housePath = Path().apply {
                    moveTo(w * 0.12f, h * 0.46f)
                    lineTo(w * 0.50f, h * 0.12f)
                    lineTo(w * 0.88f, h * 0.46f)
                    lineTo(w * 0.88f, h * 0.88f)
                    lineTo(w * 0.12f, h * 0.88f)
                    close()
                }
                drawPath(
                    path = housePath,
                    color = tint,
                    style = Stroke(width = stroke, join = StrokeJoin.Round)
                )
                val doorPath = Path().apply {
                    moveTo(w * 0.38f, h * 0.88f)
                    lineTo(w * 0.38f, h * 0.58f)
                    lineTo(w * 0.62f, h * 0.58f)
                    lineTo(w * 0.62f, h * 0.88f)
                }
                drawPath(
                    path = doorPath,
                    color = tint,
                    style = Stroke(width = stroke, join = StrokeJoin.Round)
                )
            }
            HomeNavTab.PROJECTS -> {
                val folderPath = Path().apply {
                    moveTo(w * 0.10f, h * 0.24f)
                    lineTo(w * 0.40f, h * 0.24f)
                    lineTo(w * 0.48f, h * 0.36f)
                    lineTo(w * 0.90f, h * 0.36f)
                    lineTo(w * 0.90f, h * 0.84f)
                    lineTo(w * 0.10f, h * 0.84f)
                    close()
                }
                drawPath(
                    path = folderPath,
                    color = tint,
                    style = Stroke(width = stroke, join = StrokeJoin.Round)
                )
            }
            HomeNavTab.BUILDER -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.12f, h * 0.12f),
                    size = Size(w * 0.30f, h * 0.30f),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                    style = Stroke(width = stroke)
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.58f, h * 0.58f),
                    size = Size(w * 0.30f, h * 0.30f),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                    style = Stroke(width = stroke)
                )
                val connector = Path().apply {
                    moveTo(w * 0.27f, h * 0.42f)
                    lineTo(w * 0.27f, h * 0.73f)
                    lineTo(w * 0.58f, h * 0.73f)
                }
                drawPath(
                    path = connector,
                    color = tint,
                    style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            HomeNavTab.NOTEBOOK -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.18f, h * 0.10f),
                    size = Size(w * 0.64f, h * 0.80f),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = Stroke(width = stroke)
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.32f, h * 0.34f),
                    end = Offset(w * 0.68f, h * 0.34f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.32f, h * 0.52f),
                    end = Offset(w * 0.68f, h * 0.52f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.32f, h * 0.70f),
                    end = Offset(w * 0.54f, h * 0.70f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
            HomeNavTab.HISTORY -> {
                drawArc(
                    color = tint,
                    startAngle = -60f,
                    sweepAngle = 300f,
                    useCenter = false,
                    topLeft = Offset(w * 0.12f, h * 0.12f),
                    size = Size(w * 0.76f, h * 0.76f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.50f, h * 0.50f),
                    end = Offset(w * 0.50f, h * 0.30f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.50f, h * 0.50f),
                    end = Offset(w * 0.66f, h * 0.58f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Interactive Notification Center Sheet:
 * Supports filtering between "Show all" and "Unread", marking individual notifications
 * as read/unread by clicking them, and "Mark all read".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsCenterSheet(
    notifications: List<SparkNotificationItem>,
    onDismiss: () -> Unit,
    onToggleRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onRestoreSampleNotifications: () -> Unit
) {
    var showUnreadOnly by rememberSaveable { mutableStateOf(false) }
    val unreadCount = notifications.count { !it.isRead }
    val displayedNotifications = if (showUnreadOnly) {
        notifications.filter { !it.isRead }
    } else {
        notifications
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Notifications",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        color = IfxBlack
                    )
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(IfxLime)
                                .border(1.dp, IfxBlack.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 9.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$unreadCount new",
                                fontFamily = Inter,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = IfxBlack
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = IfxBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Pills ("Show all" / "Unread") + "Mark all read" action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { showUnreadOnly = false },
                        shape = RoundedCornerShape(12.dp),
                        color = if (!showUnreadOnly) IfxBlack else Color(0xFFF3F4F6)
                    ) {
                        Text(
                            text = "Show all (${notifications.size})",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = if (!showUnreadOnly) IfxLime else Color(0xFF4B5563),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }

                    Surface(
                        onClick = { showUnreadOnly = true },
                        shape = RoundedCornerShape(12.dp),
                        color = if (showUnreadOnly) IfxBlack else Color(0xFFF3F4F6)
                    ) {
                        Text(
                            text = "Unread ($unreadCount)",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = if (showUnreadOnly) IfxLime else Color(0xFF4B5563),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }

                if (unreadCount > 0) {
                    Text(
                        text = "Mark all read",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = IfxBlack,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onMarkAllRead)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (displayedNotifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (showUnreadOnly) "All caught up! No unread notifications." else "No notifications yet.",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = IfxBlack
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (notifications.isEmpty()) {
                        Surface(
                            onClick = onRestoreSampleNotifications,
                            shape = RoundedCornerShape(12.dp),
                            color = IfxLime,
                            border = BorderStroke(1.dp, IfxBlack)
                        ) {
                            Text(
                                text = "Load recent Spark runs",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = IfxBlack,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    displayedNotifications.forEach { item ->
                        Surface(
                            onClick = { onToggleRead(item.id) },
                            shape = RoundedCornerShape(18.dp),
                            color = if (item.isRead) Color(0xFFF9FAFB) else Color.White,
                            shadowElevation = if (item.isRead) 0.dp else 4.dp,
                            border = BorderStroke(
                                width = if (item.isRead) 1.dp else 1.5.dp,
                                color = if (item.isRead) Color(0xFFE5E7EB) else IfxBlack
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Image(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.sparkTitle,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.sparkTitle,
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.5.sp,
                                            color = IfxBlack
                                        )
                                        Text(
                                            text = item.timestamp,
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = Color(0xFF686D76)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.message,
                                        fontFamily = Inter,
                                        fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = if (item.isRead) Color(0xFF686D76) else IfxBlack
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (item.isRead) Color(0xFFE5E7EB) else IfxLime)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (item.isRead) "Read · Tap to mark unread" else "Unread · Tap to mark read",
                                                fontFamily = Inter,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = IfxBlack
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Clear all notifications",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = Color(0xFF686D76),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onClearAll)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileAndPreferencesSheet(
    displayName: String,
    email: String,
    provider: String,
    initials: String,
    photoUri: String?,
    studioAvatarIndex: Int,
    isEmptyState: Boolean,
    isGuest: Boolean,
    onDismiss: () -> Unit,
    onPickFromGallery: () -> Unit,
    onSelectStudioAvatar: (Int) -> Unit,
    onUseInitialsPlaceholder: () -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onToggleEmptyState: (Boolean) -> Unit,
    onReplayAppTour: () -> Unit,
    onSwitchAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    var editedName by rememberSaveable(displayName) { mutableStateOf(displayName) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile & Preferences",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp,
                    color = IfxBlack
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = IfxBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF6F7F2))
                    .border(1.dp, Color(0xFFE4E7EC), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatarButton(
                    photoUri = photoUri,
                    studioAvatarIndex = studioAvatarIndex,
                    initialsPlaceholder = initials,
                    onClick = onPickFromGallery,
                    modifier = Modifier.size(58.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = editedName.ifBlank { "Alex" },
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = IfxBlack
                    )
                    Text(
                        text = "$email · $provider",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color(0xFF686D76)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = onPickFromGallery,
                            shape = RoundedCornerShape(10.dp),
                            color = IfxBlack
                        ) {
                            Text(
                                text = "Upload photo",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = IfxLime,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        if (!photoUri.isNullOrBlank() || studioAvatarIndex > 0) {
                            Surface(
                                onClick = onUseInitialsPlaceholder,
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                            ) {
                                Text(
                                    text = "Use placeholder ($initials)",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = IfxBlack,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Avatar Style (Photo or Placeholder)",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = IfxBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isPlaceholderSelected = photoUri.isNullOrBlank() && studioAvatarIndex == 0
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IfxBlack)
                        .border(
                            width = if (isPlaceholderSelected) 2.5.dp else 1.dp,
                            color = if (isPlaceholderSelected) IfxLime else IfxBlack,
                            shape = CircleShape
                        )
                        .clickable { onUseInitialsPlaceholder() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = IfxLime
                    )
                }

                listOf(1, 2, 3).forEach { preset ->
                    val selected = studioAvatarIndex == preset && photoUri.isNullOrBlank()
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) IfxBlack else Color(0xFFD1D5DB),
                                shape = CircleShape
                            )
                            .clickable { onSelectStudioAvatar(preset) }
                    ) {
                        StudioAvatarIllustration(
                            presetIndex = preset,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = editedName,
                onValueChange = {
                    editedName = it
                    onUpdateDisplayName(it)
                },
                label = { Text("Greeting Name", fontFamily = Inter, fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IfxBlack,
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedLabelColor = IfxBlack,
                    cursorColor = IfxBlack
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Home Screen State",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = IfxBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { onToggleEmptyState(true) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isEmptyState) IfxLime else Color.White,
                    border = BorderStroke(1.5.dp, if (isEmptyState) IfxBlack else Color(0xFFE4E7EC)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "1. Empty State",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = IfxBlack
                        )
                    }
                }
                Surface(
                    onClick = { onToggleEmptyState(false) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (!isEmptyState) IfxLime else Color.White,
                    border = BorderStroke(1.5.dp, if (!isEmptyState) IfxBlack else Color(0xFFE4E7EC)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "2. Your Sparks",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = IfxBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Replay Guided App Tour action
            Surface(
                onClick = onReplayAppTour,
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF6F7F2),
                border = BorderStroke(1.2.dp, IfxBlack),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Replay Guided App Tour",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = IfxBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = if (isGuest) onSignInClick else onSwitchAccountClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IfxBlack,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isGuest) "Sign in" else "Switch account",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFDC2626)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFFFECACA)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isGuest) "Exit guest" else "Log out",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SparkCustomizationSheet(
    isEmptyState: Boolean,
    onDismiss: () -> Unit,
    onAddCustomSpark: (title: String, subtitle: String, iconRes: Int) -> Unit,
    onResetToEmptyState: () -> Unit,
    onRestoreDefaults: () -> Unit
) {
    var newTitle by rememberSaveable { mutableStateOf("") }
    var newSubtitle by rememberSaveable { mutableStateOf("") }
    var selectedIconIndex by rememberSaveable { mutableIntStateOf(0) }

    val iconOptions = listOf(
        R.drawable.ic_tile_mail to "Email",
        R.drawable.ic_tile_bell to "Alert",
        R.drawable.ic_tile_notebook to "Notes",
        R.drawable.ic_tile_cal to "Calendar"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customize Your Sparks",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp,
                    color = IfxBlack
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = IfxBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Choose Spark Icon",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = IfxBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                iconOptions.forEachIndexed { index, (iconRes, label) ->
                    val isSelected = selectedIconIndex == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) IfxLime else Color(0xFFF4F6F8))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) IfxBlack else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedIconIndex = index }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = label,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = IfxBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Spark Name (e.g. Flight Check-In)", fontFamily = Inter, fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IfxBlack,
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedLabelColor = IfxBlack,
                    cursorColor = IfxBlack
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = newSubtitle,
                onValueChange = { newSubtitle = it },
                label = { Text("Trigger & Action (e.g. Adds boarding pass to Calendar)", fontFamily = Inter, fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IfxBlack,
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedLabelColor = IfxBlack,
                    cursorColor = IfxBlack
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        onAddCustomSpark(
                            newTitle,
                            newSubtitle,
                            iconOptions[selectedIconIndex].first
                        )
                    }
                },
                enabled = newTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IfxLime,
                    contentColor = IfxBlack,
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF9CA3AF)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "+ Add Spark to Home",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRestoreDefaults,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IfxBlack,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "Load Default Sparks",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (!isEmptyState) {
                    Button(
                        onClick = onResetToEmptyState,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = IfxBlack
                        ),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Switch to Empty State",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Home Screen - State 1 (Empty)", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenEmptyPreview() {
    IFXTheme {
        HomeScreen(
            account = SavedAccountInfo(
                fullName = "Alex Morgan",
                email = "alex@gmail.com",
                provider = "Google",
                initials = "AM"
            ),
            initialEmptyState = true
        )
    }
}

@Preview(name = "Home Screen - State 2 (Specialized Sparks)", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenSpecializedPreview() {
    IFXTheme {
        HomeScreen(
            account = SavedAccountInfo(
                fullName = "Alex Morgan",
                email = "alex@gmail.com",
                provider = "Google",
                initials = "AM"
            ),
            initialEmptyState = false
        )
    }
}
