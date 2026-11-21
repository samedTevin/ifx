package com.samedtevin.ifx.home.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import java.text.NumberFormat
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
    val iconRes: Int = R.drawable.ic_tile_mail,
    val iconKind: SparkIconKind = SparkIconKind.BELL_ALERT
) : Serializable

enum class HomeNavTab(val label: String) {
    HOME("Home"),
    PROJECTS("Projects"),
    BUILDER("Builder"),
    NOTEBOOK("Notebook"),
    HISTORY("History")
}

enum class SparkCategory(
    val label: String,
    val recommendation: String,
    val iconKind: SparkIconKind
) {
    RECOMMENDED("Recommended", "Picked to automate your daily essentials", SparkIconKind.GEMINI_AGENT),
    MOST_POPULAR("Most Popular", "Most connected Sparks across IFX", SparkIconKind.WEEKLY_CHART),
    HEALTHY("Healthy", "Suggested for better rest, habits, and focus", SparkIconKind.HABIT_ROUTINE),
    PRODUCTIVITY("Productivity", "Keep deadlines, briefings, and meetings on track", SparkIconKind.CALENDAR_EVENT),
    FINANCE("Finance", "Track receipts and bill due dates automatically", SparkIconKind.RECEIPT_DOC),
    MY_SPARKS("My Sparks", "Custom workflows created by you or AI", SparkIconKind.CLOCK_TRIGGER),
    ALL("All", "Browse all official and custom Sparks", SparkIconKind.BELL_ALERT)
}

private fun filterSparksByCategory(
    sparks: List<SparkRoutineItem>,
    category: SparkCategory
): List<SparkRoutineItem> = when (category) {
    SparkCategory.RECOMMENDED -> {
        val featuredIds = setOf("deadline_email", "morning_briefing", "routine_reminder", "focus_session")
        sparks.filter { !it.isOfficial || it.id in featuredIds }
    }
    SparkCategory.MOST_POPULAR -> sparks.sortedByDescending { it.activeUsersCount }
    SparkCategory.HEALTHY -> sparks.filter {
        it.topic == SparkTopic.ROUTINE || it.topic == SparkTopic.NIGHT || it.topic == SparkTopic.FOCUS
    }
    SparkCategory.PRODUCTIVITY -> sparks.filter {
        it.topic == SparkTopic.EMAIL || it.topic == SparkTopic.SCHEDULE || it.topic == SparkTopic.REVIEW
    }
    SparkCategory.FINANCE -> sparks.filter { it.topic == SparkTopic.FINANCE }
    SparkCategory.MY_SPARKS -> sparks.filter { !it.isOfficial }
    SparkCategory.ALL -> sparks
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
        actionIcon = SparkIconKind.LIST_BRIEF,
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
        actionIcon = SparkIconKind.SILENT_DND,
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
        actionIcon = SparkIconKind.VIDEO_MEET,
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
        actionIcon = SparkIconKind.TROPHY_STAR,
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
        actionIcon = SparkIconKind.BED_REST,
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
        actionIcon = SparkIconKind.WALLET_VAULT,
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
    ),
    SparkRoutineItem(
        id = "bill_due_alert",
        title = "Bill Due Alert",
        subtitle = "bill due · 2 days before",
        enabled = false,
        runsToday = 1,
        iconRes = R.drawable.ic_tile_cal,
        secondaryIconRes = R.drawable.ic_tile_bell,
        topic = SparkTopic.FINANCE,
        primaryIcon = SparkIconKind.WALLET_VAULT,
        actionIcon = SparkIconKind.BELL_ALERT,
        isOfficial = true,
        activeUsersCount = 1265,
        about = "Spots upcoming bills in your inbox and reminds you before the due date.",
        triggerTitle = "Email arrives containing bill due",
        triggerTemplate = "Email arrives containing \"{keyword}\"",
        actions = listOf("Remind 2 days before due date"),
        actionTemplates = listOf("Send reminder ({lead_time})"),
        sparkCode = "IFX-BD09",
        params = listOf(
            SparkEditableParam(
                key = "keyword",
                label = "Keyword",
                value = "bill due",
                type = SparkParamType.KEYWORD,
                appliesToTrigger = true
            ),
            SparkEditableParam(
                key = "lead_time",
                label = "Reminder",
                value = "2 days before",
                type = SparkParamType.DURATION,
                appliesToTrigger = false
            )
        )
    )
)

private fun defaultNotifications(): List<SparkNotificationItem> = listOf(
    SparkNotificationItem(
        id = "notif_1",
        sparkTitle = "Deadline Email",
        message = "Added 'Q4 Design Review' (due Friday 5:00 PM) to Calendar and Notebook.",
        timestamp = "12m ago",
        isRead = false,
        iconRes = R.drawable.ic_tile_mail,
        iconKind = SparkIconKind.MAIL_DEADLINE
    ),
    SparkNotificationItem(
        id = "notif_2",
        sparkTitle = "Morning Briefing",
        message = "Sent your 8:00 AM daily agenda with 3 priority items.",
        timestamp = "2h ago",
        isRead = false,
        iconRes = R.drawable.ic_tile_bell,
        iconKind = SparkIconKind.SUN_BRIEFING
    ),
    SparkNotificationItem(
        id = "notif_3",
        sparkTitle = "Weekly Review",
        message = "Saved 9 completed items into your Sunday review note.",
        timestamp = "Yesterday",
        isRead = true,
        iconRes = R.drawable.ic_tile_notebook,
        iconKind = SparkIconKind.WEEKLY_CHART
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

    val isGuest = account == null || account.provider.equals("Guest", ignoreCase = true)

    // Two-state home flow: State 1 = Empty State, State 2 = Specialized Sparks State
    var isEmptyState by rememberSaveable { mutableStateOf(initialEmptyState) }
    var sparks by rememberSaveable { mutableStateOf(defaultPreferenceSparks()) }
    var selectedSparkId by rememberSaveable { mutableStateOf<String?>("deadline_email") }
    var detailSparkId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingSparkId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf(SparkCategory.RECOMMENDED) }
    var geminiSuggestionAdded by rememberSaveable { mutableStateOf(false) }

    // Interactive Notifications state
    var notifications by rememberSaveable { mutableStateOf(defaultNotifications()) }
    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }

    var selectedTab by rememberSaveable { mutableStateOf(HomeNavTab.HOME) }

    // Modal sheets for Profile/Photo customization and AI Spark Creator
    var showProfileSheet by rememberSaveable { mutableStateOf(false) }
    var showSparkBuilderSheet by rememberSaveable { mutableStateOf(false) }
    var showAiCreatorSheet by rememberSaveable { mutableStateOf(false) }

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
                val activeBuilderSpark = if (selectedTab == HomeNavTab.BUILDER && !isTourActive) {
                    editingSparkId?.let { id -> sparks.firstOrNull { it.id == id } }
                        ?: selectedSparkId?.let { id -> sparks.firstOrNull { it.id == id } }
                        ?: sparks.firstOrNull()
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
                    // Interactive Drag-and-Drop Workflow Canvas directly on IFX's existing grid background
                    SparkBuilderEditor(
                        spark = activeBuilderSpark,
                        allSparks = sparks,
                        onSelectSpark = { chosen ->
                            editingSparkId = chosen.id
                            selectedSparkId = chosen.id
                        },
                        onCreateWithAiClick = { showAiCreatorSheet = true },
                        onSaveSpark = { updated ->
                            sparks = sparks.map { item ->
                                if (item.id == updated.id) updated else item
                            }
                            selectedSparkId = updated.id
                            editingSparkId = updated.id
                        },
                        onDeleteSpark = { target ->
                            val remaining = sparks.filterNot { it.id == target.id }
                            sparks = remaining
                            editingSparkId = remaining.firstOrNull()?.id
                            if (selectedSparkId == target.id) {
                                selectedSparkId = remaining.firstOrNull()?.id
                            }
                            if (remaining.isEmpty()) {
                                isEmptyState = true
                                selectedTab = HomeNavTab.HOME
                            }
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
                                    // 2. Date & Greeting Header with Electric Lime Name Highlight
                                    Text(
                                        text = formattedDate,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.5.sp,
                                        color = Color(0xFF525866)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Hi,",
                                            fontFamily = Inter,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 36.sp,
                                            lineHeight = 40.sp,
                                            letterSpacing = (-1.2).sp,
                                            color = IfxBlack
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(IfxLime)
                                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = customDisplayName,
                                                fontFamily = Inter,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 36.sp,
                                                lineHeight = 40.sp,
                                                letterSpacing = (-1.2).sp,
                                                color = IfxBlack,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // 3. Swappable Category Box Strip (Recommended, Most Popular, Healthy, Productivity, Finance, My Sparks, All)
                                    if (!isEmptyState) {
                                        SparkCategoryMenuBar(
                                            sparks = sparks,
                                            selectedCategory = selectedCategory,
                                            onCategorySelected = { selectedCategory = it }
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }

                                    // 4. Crossfade between State 1 (Empty State) and State 2 (Specialized User Sparks State)
                                    Crossfade(
                                        targetState = isEmptyState,
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                        label = "homeEmptyVsSpecializedState"
                                    ) { empty ->
                                        if (empty) {
                                            HomeEmptyStateSection(
                                                onHeroCreateClick = { showAiCreatorSheet = true },
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
                                            val categorySparks = remember(sparks, selectedCategory) {
                                                filterSparksByCategory(sparks, selectedCategory)
                                            }
                                            HomeSpecializedSparksSection(
                                                categoryTitle = if (selectedCategory == SparkCategory.RECOMMENDED) {
                                                    "Picked for your day"
                                                } else {
                                                    selectedCategory.label
                                                },
                                                categoryRecommendation = if (selectedCategory == SparkCategory.RECOMMENDED) {
                                                    "Tap a Spark to switch it on or off."
                                                } else {
                                                    selectedCategory.recommendation
                                                },
                                                sparks = categorySparks,
                                                selectedSparkId = selectedSparkId,
                                                onSparkClick = { clickedSpark ->
                                                    selectedSparkId = clickedSpark.id
                                                    detailSparkId = clickedSpark.id
                                                },
                                                onSparkToggle = { toggledSpark, isEnabled ->
                                                    toggleSparkEnabled(toggledSpark.id, isEnabled)
                                                },
                                                onCreateCustomSparkClick = {
                                                    showAiCreatorSheet = true
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

                // 5. 3D Floating Action Button ("+ New Spark") -> Opens AI Spark Creator
                if (!isTourActive && selectedTab == HomeNavTab.HOME) {
                    NewSparkFab(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 22.dp, bottom = 18.dp),
                        onClick = {
                            if (isEmptyState) {
                                activateSpecializedState(preferredSparkId = "deadline_email")
                            } else {
                                showAiCreatorSheet = true
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

    // AI Spark Creator Sheet (describe what you want -> Gemini drafts workflow in ~1s -> opens in Builder)
    if (showAiCreatorSheet) {
        AiSparkCreatorSheet(
            onDismiss = { showAiCreatorSheet = false },
            onSparkGenerated = { generated ->
                sparks = listOf(generated) + sparks
                selectedSparkId = generated.id
                editingSparkId = generated.id
                isEmptyState = false
                showAiCreatorSheet = false
                detailSparkId = null
                selectedTab = HomeNavTab.BUILDER
                notifications = listOf(
                    SparkNotificationItem(
                        id = "notif_ai_${System.currentTimeMillis()}",
                        sparkTitle = generated.title,
                        message = "Created with AI and opened in Builder.",
                        timestamp = "Just now",
                        isRead = false,
                        iconKind = generated.primaryIcon
                    )
                ) + notifications
                Toast.makeText(context, "Created \"${generated.title}\" in Builder", Toast.LENGTH_SHORT).show()
            }
        )
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
 * Horizontal Scrollable Category Strip:
 * - Selected: Deep Ink Black capsule with crisp white label & circular Electric Lime count badge.
 * - Unselected: Crisp white capsule with bold black label & circular soft gray count badge.
 */
@Composable
private fun SparkCategoryMenuBar(
    sparks: List<SparkRoutineItem>,
    selectedCategory: SparkCategory,
    onCategorySelected: (SparkCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SparkCategory.entries.forEach { category ->
            val isSelected = category == selectedCategory
            val categoryCount = remember(sparks, category) {
                filterSparksByCategory(sparks, category).size
            }

            val pillBg by animateColorAsState(
                targetValue = if (isSelected) IfxBlack else Color.White,
                animationSpec = tween(220),
                label = "catPillBg"
            )
            val labelColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else IfxBlack,
                animationSpec = tween(220),
                label = "catPillLabel"
            )
            val badgeBg by animateColorAsState(
                targetValue = if (isSelected) IfxLime else Color(0xFFF1F3F5),
                animationSpec = tween(220),
                label = "catBadgeBg"
            )

            Surface(
                onClick = { onCategorySelected(category) },
                shape = CircleShape,
                color = pillBg,
                shadowElevation = if (isSelected) 3.dp else 0.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) IfxBlack else Color(0xFFE0E4EA)
                ),
                modifier = Modifier.height(46.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 18.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = category.label,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        letterSpacing = (-0.2).sp,
                        color = labelColor
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$categoryCount",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            color = IfxBlack
                        )
                    }
                }
            }
        }
    }
}

/**
 * Refined Profile Avatar Button:
 * - Displays the user's real photo cleanly cropped with a crisp hairline ring.
 * - Falls back to a bold IFX monogram (default: solid Ink Black with Electric Lime initials).
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

    val (toneBg, toneFg) = when (studioAvatarIndex) {
        1 -> IfxLime to IfxBlack
        2 -> Color(0xFFEAE6DC) to IfxBlack
        3 -> Color(0xFFDCE4EE) to IfxBlack
        else -> IfxBlack to IfxLime
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (loadedBitmap != null) Color.White else toneBg,
        shadowElevation = 2.dp,
        border = if (loadedBitmap != null || studioAvatarIndex != 0) {
            BorderStroke(1.2.dp, IfxBlack.copy(alpha = 0.16f))
        } else {
            null
        },
        modifier = modifier.size(46.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (loadedBitmap != null) {
                Image(
                    bitmap = loadedBitmap!!,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = initialsPlaceholder.take(2).uppercase(),
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.4.sp,
                    color = toneFg
                )
            }
        }
    }
}

/**
 * Refined Top-Right Notification Button:
 * Crisp white circle with 1.2.dp Ink Black border, outline vector bell, and Electric Lime unread dot perched on rim.
 */
@Composable
private fun NotificationBellButton(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(46.dp)) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 1.dp,
            border = BorderStroke(1.2.dp, IfxBlack),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SparkGlyphVector(
                    iconKind = SparkIconKind.BELL_ALERT,
                    tint = IfxBlack,
                    modifier = Modifier.size(21.dp)
                )
            }
        }

        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 1.dp, end = 1.dp)
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(IfxLime)
                    .border(1.4.dp, IfxBlack, CircleShape)
            )
        }
    }
}

/**
 * State 1: Empty State ("Create your first Spark") with dark hero schematic card
 * and stacked asymmetric leaf-corner quick-start template pills.
 */
@Composable
private fun HomeEmptyStateSection(
    onHeroCreateClick: () -> Unit,
    onPresetChipClick: (presetId: String, presetTitle: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Dark Hero Empty Card (tappable to launch AI Spark Creator)
        Surface(
            onClick = onHeroCreateClick,
            shape = RoundedCornerShape(28.dp),
            color = IfxBlack,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Subtle architectural dark-olive tonal arc in the top-right corner
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color(0xFF212614),
                        radius = 78.dp.toPx(),
                        center = Offset(size.width - 14.dp.toPx(), 18.dp.toPx())
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp)
                ) {
                    EmptySparkHeroSchematic(
                        modifier = Modifier
                            .width(196.dp)
                            .height(38.dp)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Create your\nfirst Spark",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        lineHeight = 34.sp,
                        letterSpacing = (-1.0).sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Say a routine in plain words and Gemini drafts it for you.",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        color = Color(0xFFB3B8C2)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "OR START FROM A TEMPLATE",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.5.sp,
            letterSpacing = 1.1.sp,
            color = Color(0xFF4B525E)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            TemplateLeafPill(
                label = "Deadline Email",
                iconKind = SparkIconKind.MAIL_DEADLINE,
                containerColor = IfxLime,
                labelColor = IfxBlack,
                iconCircleColor = IfxBlack,
                iconTint = IfxLime,
                border = null,
                onClick = { onPresetChipClick("deadline_email", "Deadline Email") }
            )
            TemplateLeafPill(
                label = "Morning Briefing",
                iconKind = SparkIconKind.SUN_BRIEFING,
                containerColor = IfxBlack,
                labelColor = Color.White,
                iconCircleColor = IfxLime,
                iconTint = IfxBlack,
                border = null,
                onClick = { onPresetChipClick("morning_briefing", "Morning Briefing") }
            )
            TemplateLeafPill(
                label = "Weekly Review",
                iconKind = SparkIconKind.CALENDAR_EVENT,
                containerColor = Color.White,
                labelColor = IfxBlack,
                iconCircleColor = IfxLime,
                iconTint = IfxBlack,
                border = BorderStroke(1.2.dp, IfxBlack),
                onClick = { onPresetChipClick("weekly_review", "Weekly Review") }
            )
        }
    }
}

@Composable
private fun TemplateLeafPill(
    label: String,
    iconKind: SparkIconKind,
    containerColor: Color,
    labelColor: Color,
    iconCircleColor: Color,
    iconTint: Color,
    border: BorderStroke?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 26.dp,
            bottomEnd = 26.dp,
            bottomStart = 6.dp
        ),
        color = containerColor,
        border = border,
        shadowElevation = if (containerColor == Color.White) 1.dp else 2.dp,
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 9.dp, end = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconCircleColor),
                contentAlignment = Alignment.Center
            ) {
                SparkGlyphVector(
                    iconKind = iconKind,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                text = label,
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.5.sp,
                letterSpacing = (-0.2).sp,
                color = labelColor
            )
        }
    }
}

@Composable
private fun EmptySparkHeroSchematic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cy = size.height / 2f
        val strokeWidth = 2.dp.toPx()

        // 1. Trigger Node: Solid Electric Lime Circle
        val circleRadius = 15.dp.toPx()
        val c1X = circleRadius + 1.dp.toPx()
        drawCircle(
            color = IfxLime,
            radius = circleRadius,
            center = Offset(c1X, cy)
        )

        // 2. Condition Node: Outlined Electric Lime Diamond
        val dR = 14.5.dp.toPx()
        val dX = size.width * 0.49f

        // Connector 1 -> 2
        drawLine(
            color = IfxLime,
            start = Offset(c1X + circleRadius, cy),
            end = Offset(dX - dR, cy),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        val diamond = Path().apply {
            moveTo(dX, cy - dR)
            lineTo(dX + dR, cy)
            lineTo(dX, cy + dR)
            lineTo(dX - dR, cy)
            close()
        }
        drawPath(
            path = diamond,
            color = IfxLime,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )

        // 3. Action Node: Dashed Electric Lime Rounded Square with '+'
        val sqSize = 29.dp.toPx()
        val sqX = size.width - sqSize - 2.dp.toPx()
        val sqY = cy - sqSize / 2f

        // Connector 2 -> 3
        drawLine(
            color = IfxLime,
            start = Offset(dX + dR, cy),
            end = Offset(sqX, cy),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawRoundRect(
            color = IfxLime,
            topLeft = Offset(sqX, sqY),
            size = Size(sqSize, sqSize),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(
                width = 1.7.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
            )
        )

        val plusHalf = 4.5.dp.toPx()
        val plusCx = sqX + sqSize / 2f
        drawLine(
            color = IfxLime,
            start = Offset(plusCx - plusHalf, cy),
            end = Offset(plusCx + plusHalf, cy),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = IfxLime,
            start = Offset(plusCx, cy - plusHalf),
            end = Offset(plusCx, cy + plusHalf),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * State 2: Populated Home Sparks Section with clean editorial header and high-contrast IFX cards.
 */
@Composable
private fun HomeSpecializedSparksSection(
    categoryTitle: String,
    categoryRecommendation: String,
    sparks: List<SparkRoutineItem>,
    selectedSparkId: String?,
    onSparkClick: (SparkRoutineItem) -> Unit,
    onSparkToggle: (SparkRoutineItem, Boolean) -> Unit,
    onCreateCustomSparkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = categoryTitle,
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.6).sp,
                color = IfxBlack
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = categoryRecommendation,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF525866)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sparks.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                border = BorderStroke(1.2.dp, Color(0xFFE4E7EC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No custom Sparks yet",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = IfxBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Build your own automation flow and edit every node freely.",
                        fontFamily = Inter,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = Color(0xFF686D76),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onCreateCustomSparkClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IfxBlack,
                            contentColor = IfxLime
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "+ Create Spark",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                sparks.forEachIndexed { index, spark ->
                    val isSelected = spark.id == selectedSparkId
                    Spark3DCard(
                        spark = spark,
                        cardIndex = index,
                        isSelected = isSelected,
                        onClick = { onSparkClick(spark) },
                        onToggle = { enabled -> onSparkToggle(spark, enabled) }
                    )
                }
            }
        }
    }
}

private enum class HomeCardVisualTone {
    LIME,
    DARK,
    LIGHT
}

/**
 * Editorial High-Contrast IFX Spark Card:
 * - Alternating bold Electric Lime, Deep Ink Black, and Crisp White card surfaces.
 * - Top row: Overlapping horizontal circular service icons + bold `N STEPS` capsule pill.
 * - Middle: ExtraBold title + concise 2-3 line summary.
 * - Bottom: Stacked `USED` / `RUNS` live counter with formatted number + wide tactile `Active`/`Start` pill.
 */
@Composable
private fun Spark3DCard(
    spark: SparkRoutineItem,
    cardIndex: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardTone = when {
        spark.topic == SparkTopic.NIGHT -> HomeCardVisualTone.DARK
        cardIndex % 3 == 0 -> HomeCardVisualTone.LIME
        cardIndex % 3 == 1 -> HomeCardVisualTone.DARK
        else -> HomeCardVisualTone.LIGHT
    }
    val isDarkCard = cardTone == HomeCardVisualTone.DARK

    val cardBg = when (cardTone) {
        HomeCardVisualTone.LIME -> IfxLime
        HomeCardVisualTone.DARK -> IfxBlack
        HomeCardVisualTone.LIGHT -> Color.White
    }
    val titleColor = if (isDarkCard) Color.White else IfxBlack
    val descColor = when (cardTone) {
        HomeCardVisualTone.LIME -> IfxBlack.copy(alpha = 0.78f)
        HomeCardVisualTone.DARK -> Color(0xFFB5BAC4)
        HomeCardVisualTone.LIGHT -> Color(0xFF4B525E)
    }
    val microLabelColor = when (cardTone) {
        HomeCardVisualTone.LIME -> IfxBlack.copy(alpha = 0.65f)
        HomeCardVisualTone.DARK -> Color(0xFF8C93A0)
        HomeCardVisualTone.LIGHT -> Color(0xFF686D76)
    }
    val badgeBg = when (cardTone) {
        HomeCardVisualTone.LIME -> Color.White
        HomeCardVisualTone.DARK -> IfxLime
        HomeCardVisualTone.LIGHT -> IfxLime
    }
    val stepsBg = if (isDarkCard) IfxLime else IfxBlack
    val stepsText = if (isDarkCard) IfxBlack else IfxLime

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            cardTone == HomeCardVisualTone.LIGHT && isSelected -> IfxBlack
            cardTone == HomeCardVisualTone.LIGHT -> IfxBlack.copy(alpha = 0.85f)
            isSelected && isDarkCard -> IfxLime
            isSelected -> IfxBlack
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "sparkCardBorderColor"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 3.dp,
        animationSpec = tween(durationMillis = 200),
        label = "sparkCardElevation"
    )

    val resolvedActionCount = remember(spark.actions, spark.actionTemplates) {
        spark.resolvedActions().size.coerceAtLeast(1)
    }
    val stepCount = 1 + resolvedActionCount
    val stepsLabel = if (stepCount == 1) "1 STEP" else "$stepCount STEPS"

    val stackIcons = remember(spark.primaryIcon, spark.actionIcon, resolvedActionCount) {
        val list = mutableListOf(spark.primaryIcon, spark.actionIcon)
        if (resolvedActionCount > 1) {
            list.add(SparkIconKind.NOTEBOOK_PAGE)
        }
        list
    }

    val counterLabel = if (spark.isOfficial) "USED" else "RUNS"
    val rawCounter = if (spark.isOfficial) spark.activeUsersCount else spark.totalRunsCount
    val formattedCounter = remember(rawCounter) {
        NumberFormat.getIntegerInstance(Locale.US).format(rawCounter)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = cardBg,
        shadowElevation = cardElevation,
        border = if (cardBorderColor != Color.Transparent) {
            BorderStroke(
                width = if (isSelected) 1.8.dp else 1.3.dp,
                color = cardBorderColor
            )
        } else {
            null
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Top Row: Overlapping horizontal circular service icons + bold N STEPS pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    stackIcons.forEach { glyph ->
                        SparkCircularIconBadge(
                            iconKind = glyph,
                            containerColor = badgeBg,
                            ringColor = cardBg
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(stepsBg)
                        .padding(horizontal = 13.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepsLabel,
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = stepsText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Middle: Bold Headline + Clean Summary
            Text(
                text = spark.title,
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 23.sp,
                lineHeight = 27.sp,
                letterSpacing = (-0.6).sp,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = spark.about,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = descColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Row: Live Countable Metric + Full-Width Active/Start Toggle Pill
            val counterScale = remember(spark.id) { Animatable(1f) }
            LaunchedEffect(rawCounter) {
                counterScale.snapTo(1.14f)
                counterScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.graphicsLayer(
                        scaleX = counterScale.value,
                        scaleY = counterScale.value
                    )
                ) {
                    Text(
                        text = counterLabel,
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.9.sp,
                        color = microLabelColor
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = formattedCounter,
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.6).sp,
                        color = titleColor,
                        maxLines = 1
                    )
                }

                SparkCardActionPill(
                    enabled = spark.enabled,
                    isDarkCard = isDarkCard,
                    onToggle = { targetEnabled -> onToggle(targetEnabled) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SparkCircularIconBadge(
    iconKind: SparkIconKind,
    containerColor: Color = Color.White,
    ringColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(
                width = 2.dp,
                color = ringColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        SparkGlyphVector(
            iconKind = iconKind,
            tint = IfxBlack,
            modifier = Modifier.size(19.dp)
        )
    }
}

/**
 * Signature IFX Automation Ignition Pill:
 * - Tapping "Start" fires the 1050ms IFX node-flow signal sweep (`● ━━ ◇ ━━ ✓`) across the pill,
 *   lighting each automation node sequentially before locking into the high-contrast "Active" toggle bar.
 */
@Composable
private fun SparkCardActionPill(
    enabled: Boolean,
    isDarkCard: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isIgniting by remember { mutableStateOf(false) }
    val ignitionProgress = remember { Animatable(0f) }

    val pillScale by animateFloatAsState(
        targetValue = if (isIgniting) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pillTapScale"
    )

    val pillBg by animateColorAsState(
        targetValue = when {
            isIgniting -> IfxBlack
            enabled && isDarkCard -> IfxLime
            enabled -> IfxBlack
            isDarkCard -> Color(0xFF1A1D22)
            else -> Color.White
        },
        animationSpec = tween(220),
        label = "sparkActionPillBg"
    )
    val pillContent by animateColorAsState(
        targetValue = when {
            isIgniting -> IfxLime
            enabled && isDarkCard -> IfxBlack
            enabled -> Color.White
            isDarkCard -> Color.White
            else -> IfxBlack
        },
        animationSpec = tween(220),
        label = "sparkActionPillContent"
    )
    val thumbBg by animateColorAsState(
        targetValue = when {
            enabled && isDarkCard -> IfxBlack
            enabled -> IfxLime
            isDarkCard -> Color(0xFF2B2F38)
            else -> Color(0xFFF1F3F5)
        },
        animationSpec = tween(220),
        label = "sparkActionThumbBg"
    )
    val thumbGlyph by animateColorAsState(
        targetValue = when {
            enabled && isDarkCard -> IfxLime
            enabled -> IfxBlack
            isDarkCard -> Color.White
            else -> IfxBlack
        },
        animationSpec = tween(220),
        label = "sparkActionThumbGlyph"
    )

    Surface(
        onClick = {
            if (isIgniting) return@Surface
            if (!enabled) {
                isIgniting = true
                coroutineScope.launch {
                    ignitionProgress.snapTo(0f)
                    ignitionProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 1050, easing = FastOutSlowInEasing)
                    )
                    isIgniting = false
                    onToggle(true)
                }
            } else {
                onToggle(false)
            }
        },
        shape = CircleShape,
        color = pillBg,
        shadowElevation = if (enabled || isIgniting) 2.dp else 0.dp,
        border = BorderStroke(
            width = 1.2.dp,
            color = when {
                isIgniting -> IfxLime.copy(alpha = 0.45f)
                enabled -> Color.Transparent
                isDarkCard -> Color.White.copy(alpha = 0.2f)
                else -> IfxBlack.copy(alpha = 0.22f)
            }
        ),
        modifier = modifier
            .height(50.dp)
            .graphicsLayer(scaleX = pillScale, scaleY = pillScale)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isIgniting) {
                // IFX Topic Animation: Live 3-node automation circuit (If ● ━━ ◇ ━━ ✓ Then do)
                val p = ignitionProgress.value
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cy = h / 2f

                    // Subtle electric lime sweep fill behind the nodes
                    drawRect(
                        color = IfxLime.copy(alpha = 0.16f),
                        topLeft = Offset.Zero,
                        size = Size(w * p, h)
                    )

                    val x1 = w * 0.24f
                    val x2 = w * 0.50f
                    val x3 = w * 0.76f
                    val trackStroke = 2.4.dp.toPx()

                    // Dim rail between nodes
                    drawLine(
                        color = Color.White.copy(alpha = 0.22f),
                        start = Offset(x1, cy),
                        end = Offset(x3, cy),
                        strokeWidth = trackStroke,
                        cap = StrokeCap.Round
                    )

                    // Lit Electric Lime signal traveling from Node 1 -> Node 3
                    val signalEnd = x1 + (x3 - x1) * ((p - 0.08f) / 0.80f).coerceIn(0f, 1f)
                    drawLine(
                        color = IfxLime,
                        start = Offset(x1, cy),
                        end = Offset(signalEnd, cy),
                        strokeWidth = trackStroke,
                        cap = StrokeCap.Round
                    )

                    // Node 1: Trigger Circle (lights up immediately)
                    val n1Lit = p >= 0.05f
                    drawCircle(
                        color = if (n1Lit) IfxLime else Color(0xFF3A3E48),
                        radius = 5.5.dp.toPx(),
                        center = Offset(x1, cy)
                    )

                    // Node 2: Condition Diamond (lights up at midpoint)
                    val n2Lit = p >= 0.45f
                    val dR = 6.dp.toPx()
                    val diamond = Path().apply {
                        moveTo(x2, cy - dR)
                        lineTo(x2 + dR, cy)
                        lineTo(x2, cy + dR)
                        lineTo(x2 - dR, cy)
                        close()
                    }
                    drawPath(
                        path = diamond,
                        color = if (n2Lit) IfxLime else Color(0xFF1F2229)
                    )
                    drawPath(
                        path = diamond,
                        color = if (n2Lit) IfxLime else Color.White.copy(alpha = 0.4f),
                        style = Stroke(width = 1.6.dp.toPx(), join = StrokeJoin.Round)
                    )

                    // Node 3: Action Check Ring (ignites at end)
                    val n3Lit = p >= 0.82f
                    drawCircle(
                        color = if (n3Lit) IfxLime else Color(0xFF2B2F38),
                        radius = 7.dp.toPx(),
                        center = Offset(x3, cy)
                    )
                    if (n3Lit) {
                        val check = Path().apply {
                            moveTo(x3 - 3.2.dp.toPx(), cy + 0.2.dp.toPx())
                            lineTo(x3 - 0.6.dp.toPx(), cy + 2.8.dp.toPx())
                            lineTo(x3 + 3.6.dp.toPx(), cy - 2.4.dp.toPx())
                        }
                        drawPath(
                            path = check,
                            color = IfxBlack,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (enabled) "Active" else "Start",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        letterSpacing = (-0.2).sp,
                        color = pillContent
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(thumbBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (enabled) {
                            Canvas(modifier = Modifier.size(14.dp)) {
                                val check = Path().apply {
                                    moveTo(size.width * 0.14f, size.height * 0.52f)
                                    lineTo(size.width * 0.42f, size.height * 0.80f)
                                    lineTo(size.width * 0.88f, size.height * 0.22f)
                                }
                                drawPath(
                                    path = check,
                                    color = thumbGlyph,
                                    style = Stroke(
                                        width = 2.2.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        } else {
                            Canvas(modifier = Modifier.size(13.dp)) {
                                val play = Path().apply {
                                    moveTo(size.width * 0.24f, size.height * 0.14f)
                                    lineTo(size.width * 0.88f, size.height * 0.50f)
                                    lineTo(size.width * 0.24f, size.height * 0.86f)
                                    close()
                                }
                                drawPath(
                                    path = play,
                                    color = thumbGlyph,
                                    style = Stroke(
                                        width = 1.9.dp.toPx(),
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
}

@Composable
private fun NewSparkFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = IfxLime,
        shadowElevation = 8.dp,
        border = BorderStroke(1.5.dp, IfxBlack),
        modifier = modifier.height(54.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                val stroke = 2.4.dp.toPx()
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
                fontSize = 15.5.sp,
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
 * Clean Editorial Notification Inbox Sheet:
 * - Minimal header + subtle "Mark all read" action
 * - Clean segmented filter track (`All` | `Unread`)
 * - Hairline-separated notification rows with bespoke vector icons and subtle unread dot (zero status-pill clutter)
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
                .padding(horizontal = 22.dp)
                .padding(bottom = 26.dp)
                .navigationBarsPadding()
        ) {
            // Minimal Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inbox",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 23.sp,
                    letterSpacing = (-0.5).sp,
                    color = IfxBlack
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (unreadCount > 0) {
                        Text(
                            text = "Mark all read",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF525866),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onMarkAllRead)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = IfxBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clean Segmented Track (`All` | `Unread`)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFFF3F4F6))
                    .padding(3.dp)
            ) {
                listOf(
                    false to "All (${notifications.size})",
                    true to "Unread ($unreadCount)"
                ).forEach { (unreadTab, label) ->
                    val active = showUnreadOnly == unreadTab
                    Surface(
                        onClick = { showUnreadOnly = unreadTab },
                        shape = RoundedCornerShape(10.dp),
                        color = if (active) Color.White else Color.Transparent,
                        shadowElevation = if (active) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = Inter,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (active) IfxBlack else Color(0xFF686D76)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (displayedNotifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (showUnreadOnly) "You're all caught up" else "No activity yet",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = IfxBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (showUnreadOnly) "Unread Spark runs will show up here." else "Recent runs from your active Sparks appear here.",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color(0xFF686D76),
                        textAlign = TextAlign.Center
                    )
                    if (notifications.isEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            onClick = onRestoreSampleNotifications,
                            shape = CircleShape,
                            color = IfxBlack
                        ) {
                            Text(
                                text = "Restore sample activity",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    displayedNotifications.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onToggleRead(item.id) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Subtle unread dot in left gutter
                            Box(
                                modifier = Modifier
                                    .padding(top = 15.dp)
                                    .width(12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (!item.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(IfxBlack)
                                    )
                                }
                            }

                            SparkGlyphTile(
                                iconKind = item.iconKind,
                                size = 38.dp,
                                cornerRadius = 11.dp,
                                containerColor = if (item.isRead) Color(0xFFF3F4F6) else IfxBlack,
                                iconTint = if (item.isRead) Color(0xFF686D76) else IfxLime,
                                borderColor = Color.Transparent,
                                borderWidth = 0.dp
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
                                        fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = if (item.isRead) Color(0xFF525866) else IfxBlack
                                    )
                                    Text(
                                        text = item.timestamp,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = Color(0xFF8C929D)
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = item.message,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = if (item.isRead) Color(0xFF8C929D) else Color(0xFF374151)
                                )
                            }
                        }

                        if (index < displayedNotifications.lastIndex) {
                            HorizontalDivider(
                                color = Color(0xFFF1F3F5),
                                modifier = Modifier.padding(start = 62.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Clear all",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = Color(0xFF8C929D),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onClearAll)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Refined Account & Profile Sheet:
 * Clean editorial identity header with photo picker overlay, tasteful monogram tone swatches,
 * compact grouped settings surface, and crisp account actions.
 */
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 26.dp)
                .navigationBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Account",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 23.sp,
                    letterSpacing = (-0.5).sp,
                    color = IfxBlack
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = IfxBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Centered Editorial Identity Block
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfileAvatarButton(
                        photoUri = photoUri,
                        studioAvatarIndex = studioAvatarIndex,
                        initialsPlaceholder = initials,
                        onClick = onPickFromGallery,
                        modifier = Modifier.size(72.dp)
                    )

                    // Small crisp edit-photo badge
                    Surface(
                        onClick = onPickFromGallery,
                        shape = CircleShape,
                        color = IfxBlack,
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "+",
                                fontFamily = Inter,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = IfxLime
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = editedName.ifBlank { "Alex" },
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    letterSpacing = (-0.3).sp,
                    color = IfxBlack
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$email  ·  $provider",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color(0xFF686D76)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Clean Monogram Swatches (Ink, Lime, Sand, Slate) + Photo actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val swatches = listOf(
                        0 to IfxBlack,
                        1 to IfxLime,
                        2 to Color(0xFFEAE6DC),
                        3 to Color(0xFFDCE4EE)
                    )
                    swatches.forEach { (idx, color) ->
                        val selected = photoUri.isNullOrBlank() && studioAvatarIndex == idx
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selected) 2.2.dp else 1.dp,
                                    color = if (selected) IfxBlack else Color(0xFFD0D5DD),
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (idx == 0) onUseInitialsPlaceholder() else onSelectStudioAvatar(idx)
                                }
                        )
                    }

                    if (!photoUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Remove photo",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = Color(0xFF686D76),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onUseInitialsPlaceholder)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Grouped Preferences Container
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF8F9FA),
                border = BorderStroke(1.dp, Color(0xFFEAECEF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = {
                            editedName = it
                            onUpdateDisplayName(it)
                        },
                        label = { Text("Display name", fontFamily = Inter, fontSize = 12.5.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(13.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IfxBlack,
                            unfocusedBorderColor = Color(0xFFD6DAE0),
                            focusedLabelColor = IfxBlack,
                            cursorColor = IfxBlack,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Home layout",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = IfxBlack
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFECEEF2))
                                .padding(2.dp)
                        ) {
                            listOf(true to "Empty", false to "Sparks").forEach { (emptyFlag, label) ->
                                val active = isEmptyState == emptyFlag
                                Surface(
                                    onClick = { onToggleEmptyState(emptyFlag) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (active) Color.White else Color.Transparent,
                                    shadowElevation = if (active) 1.dp else 0.dp
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = Inter,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (active) IfxBlack else Color(0xFF686D76),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEAECEF))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onReplayAppTour)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Replay app tour",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = IfxBlack
                        )
                        Text(
                            text = "›",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF8C929D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

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
                    shape = CircleShape,
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
                    border = BorderStroke(1.2.dp, Color(0xFFF3D1D1)),
                    shape = CircleShape,
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
