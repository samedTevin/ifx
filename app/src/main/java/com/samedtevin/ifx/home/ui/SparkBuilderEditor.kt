package com.samedtevin.ifx.home.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class IfxServiceApp(
    val displayName: String,
    val iconKind: SparkIconKind
) {
    NOTIFICATION("Notification", SparkIconKind.BELL_ALERT),
    CALENDAR("Calendar", SparkIconKind.CALENDAR_EVENT),
    NOTEBOOK("Notebook", SparkIconKind.NOTEBOOK_PAGE),
    GMAIL("Gmail", SparkIconKind.MAIL_DEADLINE),
    SCHEDULE("Schedule", SparkIconKind.CLOCK_TRIGGER),
    FOCUS("Focus / DND", SparkIconKind.SILENT_DND),
    GEMINI("Gemini AI", SparkIconKind.GEMINI_AGENT)
}

private data class IfxFlowNode(
    val id: String,
    val label: String,         // "If", "Then do", "And do", "Check"
    val roleTag: String,       // "Trigger", "Action", "Condition", "AI"
    val serviceName: String,   // "Gmail", "Calendar", "Notebook", "Schedule", "Notification", "Gemini AI"
    val title: String,
    val detailHint: String,
    val timingRule: String = "Immediately",
    val onFailureRule: String = "Retry once",
    val iconKind: SparkIconKind,
    val isTrigger: Boolean,
    val summaryValue: String = "",
    val isOfficialCore: Boolean = false,
    val isOriginalNode: Boolean = true
)

private data class IfxFlowEdge(
    val id: String,
    val fromId: String,
    val toId: String
)

private data class ValidationIssue(
    val nodeId: String,
    val nodeLabel: String,
    val message: String
)

/**
 * Clean, Professional IFX Workflow Builder:
 * - Official IFX Sparks (`spark.isOfficial == true`): locked flowchart structure — user can drag/arrange,
 *   dry-run, reset, and tap any node to edit its strictly typed inputs (no adding/deleting steps or edges).
 * - User / AI Sparks (`spark.isOfficial == false`): full builder — click any node OR edge to select it,
 *   then press keyboard Backspace/Delete or tap the on-screen `⌫` delete button to remove it;
 *   tap `+ Step` for the detailed IFX step composer; tap `Edit` (or tap a selected node) for the detailed Node Editor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SparkBuilderEditor(
    spark: SparkRoutineItem,
    allSparks: List<SparkRoutineItem>,
    onSelectSpark: (SparkRoutineItem) -> Unit,
    onCreateWithAiClick: () -> Unit,
    onSaveSpark: (SparkRoutineItem) -> Unit,
    onDeleteSpark: (SparkRoutineItem) -> Unit,
    onBackToDetail: (SparkRoutineItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val topic = spark.topic
    val canvasFocusRequester = remember { FocusRequester() }

    var draftParams by remember(spark.id) { mutableStateOf(spark.params) }
    var hasUnsavedChanges by remember(spark.id) { mutableStateOf(false) }

    fun serviceForIcon(icon: SparkIconKind): String = when (icon) {
        SparkIconKind.MAIL_DEADLINE, SparkIconKind.RECEIPT_DOC -> "Gmail"
        SparkIconKind.CALENDAR_EVENT, SparkIconKind.VIDEO_MEET -> "Calendar"
        SparkIconKind.NOTEBOOK_PAGE, SparkIconKind.WALLET_VAULT, SparkIconKind.WEEKLY_CHART, SparkIconKind.TROPHY_STAR -> "Notebook"
        SparkIconKind.CLOCK_TRIGGER, SparkIconKind.SUN_BRIEFING, SparkIconKind.MOON_SLEEP, SparkIconKind.HABIT_ROUTINE -> "Schedule"
        SparkIconKind.FOCUS_TIMER, SparkIconKind.SILENT_DND -> "Focus / DND"
        SparkIconKind.GEMINI_AGENT -> "Gemini AI"
        else -> "Notification"
    }

    fun buildOriginalNodes(source: SparkRoutineItem, params: List<SparkEditableParam>): List<IfxFlowNode> {
        val current = source.copy(params = params)
        val triggerSummary = params.filter { it.appliesToTrigger }.joinToString(" · ") { it.value }
        val actionSummary = params.filter { !it.appliesToTrigger }.joinToString(" · ") { it.value }

        val result = mutableListOf(
            IfxFlowNode(
                id = "if_trigger",
                label = "If",
                roleTag = "Trigger",
                serviceName = serviceForIcon(current.primaryIcon),
                title = current.resolvedTriggerText(),
                detailHint = "Watches automatically and starts the flow when matched.",
                timingRule = "Real-time",
                onFailureRule = "Log & retry",
                iconKind = current.primaryIcon,
                isTrigger = true,
                summaryValue = triggerSummary,
                isOfficialCore = current.isOfficial,
                isOriginalNode = true
            )
        )
        current.resolvedActions().forEachIndexed { idx, actionText ->
            val icon = if (idx == 0) current.actionIcon else SparkIconKind.NOTEBOOK_PAGE
            result.add(
                IfxFlowNode(
                    id = "do_action_$idx",
                    label = if (idx == 0) "Then do" else "And do",
                    roleTag = "Action",
                    serviceName = serviceForIcon(icon),
                    title = actionText,
                    detailHint = "Runs right after the previous step.",
                    timingRule = "Immediately",
                    onFailureRule = "Retry once",
                    iconKind = icon,
                    isTrigger = false,
                    summaryValue = if (idx == 0) actionSummary else "",
                    isOfficialCore = current.isOfficial,
                    isOriginalNode = true
                )
            )
        }
        return result
    }

    fun buildSequentialEdges(list: List<IfxFlowNode>): List<IfxFlowEdge> {
        return (0 until list.lastIndex).map { i ->
            IfxFlowEdge(
                id = "edge_${list[i].id}_${list[i + 1].id}",
                fromId = list[i].id,
                toId = list[i + 1].id
            )
        }
    }

    var nodes by remember(spark.id) { mutableStateOf(buildOriginalNodes(spark, spark.params)) }
    var edges by remember(spark.id) { mutableStateOf(buildSequentialEdges(nodes)) }

    fun commitNodes(next: List<IfxFlowNode>) {
        nodes = next
        hasUnsavedChanges = true
    }

    fun syncParamsIntoNodes(updatedParams: List<SparkEditableParam>) {
        val synced = spark.copy(params = updatedParams)
        val triggerText = synced.resolvedTriggerText()
        val actionTexts = synced.resolvedActions()
        val triggerSummary = updatedParams.filter { it.appliesToTrigger }.joinToString(" · ") { it.value }
        val actionSummary = updatedParams.filter { !it.appliesToTrigger }.joinToString(" · ") { it.value }

        nodes = nodes.map { node ->
            when {
                node.isTrigger && node.isOfficialCore -> node.copy(title = triggerText, summaryValue = triggerSummary)
                node.id == "do_action_0" && node.isOfficialCore && actionTexts.isNotEmpty() ->
                    node.copy(title = actionTexts.first(), summaryValue = actionSummary)
                else -> node
            }
        }
        hasUnsavedChanges = true
    }

    // Performance-critical layout/transform state (read inside offset{}, graphicsLayer{}, Canvas{} -> 0 drag recompositions!)
    val nodeOffsets = remember(spark.id) { mutableStateMapOf<String, Offset>() }
    val nodeSizes = remember(spark.id) { mutableStateMapOf<String, IntSize>() }
    var canvasPan by remember(spark.id) { mutableStateOf(Offset.Zero) }
    var canvasZoom by remember(spark.id) { mutableFloatStateOf(1f) }

    // Selection state (for Backspace deletion & editing)
    var focusedNodeId by remember(spark.id) { mutableStateOf<String?>(null) }
    var focusedEdgeId by remember(spark.id) { mutableStateOf<String?>(null) }
    var editingNodeId by remember(spark.id) { mutableStateOf<String?>(null) }
    var showSparkSwitcher by remember(spark.id) { mutableStateOf(false) }
    var showAddStepSheet by remember(spark.id) { mutableStateOf(false) }

    // Dry-run & validation state
    var isTestingFlow by remember(spark.id) { mutableStateOf(false) }
    var testedCount by remember(spark.id) { mutableIntStateOf(0) }
    var validationIssues by remember(spark.id) { mutableStateOf<List<ValidationIssue>>(emptyList()) }
    var showValidationDialog by remember(spark.id) { mutableStateOf(false) }
    var showDryRunSuccessDialog by remember(spark.id) { mutableStateOf(false) }

    val errorNodeIds = remember(validationIssues) { validationIssues.map { it.nodeId }.toSet() }

    fun deleteFocusedSelection() {
        if (spark.isOfficial) {
            Toast.makeText(context, "Official IFX Sparks only allow editing inputs", Toast.LENGTH_SHORT).show()
            return
        }
        val edgeId = focusedEdgeId
        if (edgeId != null) {
            edges = edges.filterNot { it.id == edgeId }
            focusedEdgeId = null
            hasUnsavedChanges = true
            Toast.makeText(context, "Edge removed", Toast.LENGTH_SHORT).show()
            return
        }
        val nodeId = focusedNodeId
        if (nodeId != null) {
            if (nodes.size <= 1) {
                Toast.makeText(context, "Keep at least 1 step in the workflow", Toast.LENGTH_SHORT).show()
                return
            }
            edges = edges.filterNot { it.fromId == nodeId || it.toId == nodeId }
            commitNodes(nodes.filterNot { it.id == nodeId })
            focusedNodeId = null
            if (editingNodeId == nodeId) editingNodeId = null
            Toast.makeText(context, "Node removed", Toast.LENGTH_SHORT).show()
        }
    }

    fun validateWorkflow(): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        nodes.forEach { node ->
            val cleanTitle = node.title.trim()
            if (cleanTitle.length < 3) {
                issues.add(ValidationIssue(node.id, node.label, "Step description is too short."))
            } else if (looksLikeGibberish(cleanTitle)) {
                issues.add(ValidationIssue(node.id, node.label, "\"$cleanTitle\" does not look like a valid step."))
            }
        }
        draftParams.forEach { param ->
            val targetNodeId = if (param.appliesToTrigger) "if_trigger" else "do_action_0"
            val err = validateParamValue(param)
            if (err != null) {
                issues.add(ValidationIssue(targetNodeId, param.label, err))
            }
        }
        return issues
    }

    LaunchedEffect(isTestingFlow) {
        if (isTestingFlow) {
            testedCount = 0
            for (i in 1..nodes.size) {
                delay(360)
                testedCount = i
            }
            delay(300)
            isTestingFlow = false
            showDryRunSuccessDialog = true
        }
    }

    // Request keyboard focus on canvas whenever a node or edge is selected (so Backspace works immediately!)
    LaunchedEffect(focusedNodeId, focusedEdgeId) {
        if (focusedNodeId != null || focusedEdgeId != null) {
            try {
                canvasFocusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }

    val nodeWidthDp = 264.dp
    val nodeWidthPx = with(density) { nodeWidthDp.toPx() }
    val fallbackHeightPx = with(density) { 120.dp.toPx() }
    val gapYPx = with(density) { 174.dp.toPx() }
    val maxEdgeDistPx = with(density) { 236.dp.toPx() }

    fun triggerDryRun() {
        val issues = validateWorkflow()
        validationIssues = issues
        if (issues.isNotEmpty()) {
            showValidationDialog = true
        } else {
            isTestingFlow = true
        }
    }

    fun saveWorkflow() {
        val issues = validateWorkflow()
        validationIssues = issues
        if (issues.isNotEmpty()) {
            showValidationDialog = true
            return
        }
        val triggerNode = nodes.firstOrNull { it.isTrigger }
        val actionNodes = nodes.filter { !it.isTrigger }
        val updated = spark.copy(
            triggerTitle = triggerNode?.title ?: spark.triggerTitle,
            actions = actionNodes.map { it.title }.ifEmpty { spark.actions },
            params = draftParams
        ).withSyncedSubtitle()
        hasUnsavedChanges = false
        onSaveSpark(updated)
        Toast.makeText(context, "Saved ${updated.title}", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .focusRequester(canvasFocusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key == Key.Backspace || keyEvent.key == Key.Delete) &&
                    editingNodeId == null &&
                    !showAddStepSheet &&
                    (focusedNodeId != null || focusedEdgeId != null)
                ) {
                    deleteFocusedSelection()
                    true
                } else {
                    false
                }
            }
    ) {
        // 1. Clean Minimal Top Header (Back · Spark Switcher · Save)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    onClick = { onBackToDetail(spark) },
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E6EC)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "←",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = IfxBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showSparkSwitcher = true }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = spark.title,
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.5.sp,
                            letterSpacing = (-0.4).sp,
                            color = IfxBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "▾",
                            fontSize = 14.sp,
                            color = Color(0xFF686D76)
                        )
                    }

                    DropdownMenu(
                        expanded = showSparkSwitcher,
                        onDismissRequest = { showSparkSwitcher = false },
                        containerColor = Color.White,
                        border = BorderStroke(1.2.dp, IfxBlack),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        allSparks.forEach { candidate ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        SparkGlyphTile(
                                            iconKind = candidate.primaryIcon,
                                            size = 26.dp,
                                            cornerRadius = 7.dp,
                                            containerColor = candidate.topic.tileBg,
                                            iconTint = candidate.topic.tileIcon,
                                            borderWidth = 0.dp
                                        )
                                        Text(
                                            text = candidate.title,
                                            fontFamily = Inter,
                                            fontWeight = if (candidate.id == spark.id) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = IfxBlack
                                        )
                                    }
                                },
                                onClick = {
                                    showSparkSwitcher = false
                                    onSelectSpark(candidate)
                                }
                            )
                        }
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_gemini_color),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Create with AI",
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = IfxBlack
                                    )
                                }
                            },
                            onClick = {
                                showSparkSwitcher = false
                                onCreateWithAiClick()
                            }
                        )
                        if (!spark.isOfficial) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Delete Spark",
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                },
                                onClick = {
                                    showSparkSwitcher = false
                                    onDeleteSpark(spark)
                                }
                            )
                        }
                    }
                }
            }

            if (hasUnsavedChanges) {
                Surface(
                    onClick = { saveWorkflow() },
                    shape = CircleShape,
                    color = IfxBlack
                ) {
                    Text(
                        text = "Save",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = IfxLime,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
                    )
                }
            } else {
                Text(
                    text = "Saved",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF8C929D),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // 2. Interactive Workflow Area (Rigid background pan + Bounded node drag + Select/Backspace delete)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(spark.id) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = true)
                        var dragged = false
                        var accum = Offset.Zero
                        val slop = viewConfiguration.touchSlop
                        drag(down.id) { change ->
                            val delta = change.positionChange()
                            accum += delta
                            if (!dragged && accum.getDistance() > slop) dragged = true
                            if (dragged) {
                                change.consume()
                                canvasPan = Offset(
                                    x = (canvasPan.x + delta.x).coerceIn(-480f, 480f),
                                    y = (canvasPan.y + delta.y).coerceIn(-680f, 480f)
                                )
                            }
                        }
                        if (!dragged) {
                            focusedNodeId = null
                            focusedEdgeId = null
                        }
                    }
                }
        ) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val canvasHeightPx = constraints.maxHeight.toFloat()
            val topStartYPx = with(density) { 64.dp.toPx() }

            fun resetToBeginning() {
                val hadAddedNodes = nodes.any { !it.isOriginalNode }
                draftParams = spark.params
                val restored = buildOriginalNodes(spark, spark.params)
                nodes = restored
                edges = buildSequentialEdges(restored)
                focusedNodeId = null
                focusedEdgeId = null
                validationIssues = emptyList()
                canvasPan = Offset.Zero
                canvasZoom = 1f
                hasUnsavedChanges = false
                val centerX = ((canvasWidthPx - nodeWidthPx) / 2f).coerceAtLeast(16f)
                nodeOffsets.clear()
                restored.forEachIndexed { idx, node ->
                    nodeOffsets[node.id] = Offset(centerX, topStartYPx + idx * gapYPx)
                }
                val msg = if (hadAddedNodes) "Removed added nodes & reset flow" else "Reset flowchart to beginning"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

            LaunchedEffect(spark.id, nodes.size, canvasWidthPx, canvasHeightPx) {
                val centerX = ((canvasWidthPx - nodeWidthPx) / 2f).coerceAtLeast(16f)
                nodes.forEachIndexed { idx, node ->
                    if (!nodeOffsets.containsKey(node.id)) {
                        nodeOffsets[node.id] = Offset(centerX, topStartYPx + idx * gapYPx)
                    }
                }
            }

            // World Container with top-left TransformOrigin (zero scroll/zoom distortion!)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0f, 0f)
                        translationX = canvasPan.x
                        translationY = canvasPan.y
                        scaleX = canvasZoom
                        scaleY = canvasZoom
                    }
            ) {
                // Edge-to-Edge Connector Canvas
                val edgePath = remember { Path() }
                val arrowPath = remember { Path() }
                val normalStroke = remember(density) {
                    Stroke(width = with(density) { 2.dp.toPx() }, cap = StrokeCap.Round, join = StrokeJoin.Round)
                }
                val selectedStroke = remember(density) {
                    Stroke(width = with(density) { 3.dp.toPx() }, cap = StrokeCap.Round, join = StrokeJoin.Round)
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val arrowLen = 8.dp.toPx()
                    val arrowSpread = 0.50f

                    edges.forEachIndexed { edgeIndex, edge ->
                        val srcPos = nodeOffsets[edge.fromId] ?: return@forEachIndexed
                        val dstPos = nodeOffsets[edge.toId] ?: return@forEachIndexed
                        val srcSize = nodeSizes[edge.fromId] ?: IntSize(nodeWidthPx.roundToInt(), fallbackHeightPx.roundToInt())
                        val dstSize = nodeSizes[edge.toId] ?: IntSize(nodeWidthPx.roundToInt(), fallbackHeightPx.roundToInt())

                        val srcCx = srcPos.x + srcSize.width * 0.5f
                        val srcCy = srcPos.y + srcSize.height * 0.5f
                        val dstCx = dstPos.x + dstSize.width * 0.5f
                        val dstCy = dstPos.y + dstSize.height * 0.5f
                        val dx = dstCx - srcCx
                        val dy = dstCy - srcCy

                        val isHoriz = abs(dx) > abs(dy) * 1.15f
                        val start: Offset
                        val end: Offset
                        val c1: Offset
                        val c2: Offset

                        if (isHoriz) {
                            val dir = if (dx >= 0f) 1f else -1f
                            start = Offset(if (dx >= 0f) srcPos.x + srcSize.width else srcPos.x, srcCy)
                            end = Offset(if (dx >= 0f) dstPos.x else dstPos.x + dstSize.width, dstCy)
                            val bend = (abs(end.x - start.x) * 0.42f).coerceIn(24f, 64f)
                            c1 = Offset(start.x + bend * dir, start.y)
                            c2 = Offset(end.x - bend * dir, end.y)
                        } else {
                            val dir = if (dy >= 0f) 1f else -1f
                            start = Offset(srcCx, if (dy >= 0f) srcPos.y + srcSize.height else srcPos.y)
                            end = Offset(dstCx, if (dy >= 0f) dstPos.y else dstPos.y + dstSize.height)
                            val bend = (abs(end.y - start.y) * 0.42f).coerceIn(20f, 56f)
                            c1 = Offset(start.x, start.y + bend * dir)
                            c2 = Offset(end.x, end.y - bend * dir)
                        }

                        val isEdgeSelected = edge.id == focusedEdgeId
                        val hasErr = edge.fromId in errorNodeIds || edge.toId in errorNodeIds
                        val edgeColor = when {
                            hasErr -> Color(0xFFDC2626)
                            isEdgeSelected -> IfxBlack
                            isTestingFlow && testedCount > edgeIndex -> IfxBlack
                            else -> Color(0xFF98A2B3)
                        }
                        val stroke = if (isEdgeSelected) selectedStroke else normalStroke

                        edgePath.reset()
                        edgePath.moveTo(start.x, start.y)
                        edgePath.cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
                        drawPath(path = edgePath, color = edgeColor, style = stroke)

                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = start)
                        drawCircle(color = edgeColor, radius = 4.dp.toPx(), center = start, style = Stroke(1.6.dp.toPx()))

                        val angle = atan2((end.y - c2.y).toDouble(), (end.x - c2.x).toDouble()).toFloat()
                        arrowPath.reset()
                        arrowPath.moveTo(end.x - arrowLen * cos(angle - arrowSpread), end.y - arrowLen * sin(angle - arrowSpread))
                        arrowPath.lineTo(end.x, end.y)
                        arrowPath.lineTo(end.x - arrowLen * cos(angle + arrowSpread), end.y - arrowLen * sin(angle + arrowSpread))
                        drawPath(path = arrowPath, color = edgeColor, style = stroke)
                    }
                }

                // Selectable Edge Midpoint Handle (User Sparks only: click edge to select -> press Backspace or `⌫`!)
                if (!spark.isOfficial) {
                    edges.forEach { edge ->
                        key(edge.id) {
                            val isEdgeSelected = edge.id == focusedEdgeId
                            val hitHalfPx = with(density) { 14.dp.toPx() }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .offset {
                                        val srcPos = nodeOffsets[edge.fromId] ?: Offset.Zero
                                        val dstPos = nodeOffsets[edge.toId] ?: Offset.Zero
                                        val srcSize = nodeSizes[edge.fromId] ?: IntSize(nodeWidthPx.roundToInt(), fallbackHeightPx.roundToInt())
                                        val dstSize = nodeSizes[edge.toId] ?: IntSize(nodeWidthPx.roundToInt(), fallbackHeightPx.roundToInt())
                                        val srcCx = srcPos.x + srcSize.width * 0.5f
                                        val srcCy = srcPos.y + srcSize.height * 0.5f
                                        val dstCx = dstPos.x + dstSize.width * 0.5f
                                        val dstCy = dstPos.y + dstSize.height * 0.5f
                                        val dx = dstCx - srcCx
                                        val dy = dstCy - srcCy
                                        val isHoriz = abs(dx) > abs(dy) * 1.15f
                                        val start = if (isHoriz) {
                                            Offset(if (dx >= 0f) srcPos.x + srcSize.width else srcPos.x, srcCy)
                                        } else {
                                            Offset(srcCx, if (dy >= 0f) srcPos.y + srcSize.height else srcPos.y)
                                        }
                                        val end = if (isHoriz) {
                                            Offset(if (dx >= 0f) dstPos.x else dstPos.x + dstSize.width, dstCy)
                                        } else {
                                            Offset(dstCx, if (dy >= 0f) dstPos.y else dstPos.y + dstSize.height)
                                        }
                                        IntOffset(((start.x + end.x) * 0.5f - hitHalfPx).roundToInt(), ((start.y + end.y) * 0.5f - hitHalfPx).roundToInt())
                                    }
                                    .clip(CircleShape)
                                    .clickable {
                                        focusedNodeId = null
                                        focusedEdgeId = if (isEdgeSelected) null else edge.id
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isEdgeSelected) 16.dp else 10.dp)
                                        .clip(CircleShape)
                                        .background(if (isEdgeSelected) IfxBlack else Color.White)
                                        .border(
                                            width = if (isEdgeSelected) 2.dp else 1.4.dp,
                                            color = if (isEdgeSelected) IfxLime else Color(0xFF98A2B3),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }

                // Draggable & Selectable Node Cards (NO "Tap to edit" clutter!)
                nodes.forEachIndexed { idx, node ->
                    key(node.id) {
                        val isSelected = focusedNodeId == node.id || editingNodeId == node.id
                        val isPassed = isTestingFlow && testedCount > idx
                        val hasError = node.id in errorNodeIds

                        IfxPhoto2StyleNodeCard(
                            node = node,
                            topic = topic,
                            isFirstStartNode = idx == 0,
                            isSelected = isSelected,
                            isPassed = isPassed,
                            hasError = hasError,
                            modifier = Modifier
                                .width(nodeWidthDp)
                                .offset {
                                    val p = nodeOffsets[node.id] ?: Offset(24f, topStartYPx + idx * gapYPx)
                                    IntOffset(p.x.roundToInt(), p.y.roundToInt())
                                }
                                .onGloballyPositioned { coords ->
                                    if (nodeSizes[node.id] != coords.size) {
                                        nodeSizes[node.id] = coords.size
                                    }
                                }
                                .pointerInput(node.id, canvasZoom, edges, spark.isOfficial) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)
                                        down.consume()
                                        var totalDrag = Offset.Zero
                                        var isDragging = false
                                        val slop = viewConfiguration.touchSlop

                                        drag(down.id) { change ->
                                            val delta = change.positionChange()
                                            totalDrag += delta
                                            if (!isDragging && totalDrag.getDistance() > slop) {
                                                isDragging = true
                                            }
                                            if (isDragging) {
                                                change.consume()
                                                val cur = nodeOffsets[node.id] ?: Offset(24f, topStartYPx + idx * gapYPx)
                                                var nextX = cur.x + delta.x / canvasZoom
                                                var nextY = cur.y + delta.y / canvasZoom

                                                val neighborIds = edges.mapNotNull { e ->
                                                    when {
                                                        e.fromId == node.id -> e.toId
                                                        e.toId == node.id -> e.fromId
                                                        else -> null
                                                    }
                                                }
                                                val mySize = nodeSizes[node.id] ?: IntSize(nodeWidthPx.roundToInt(), fallbackHeightPx.roundToInt())
                                                neighborIds.forEach { neighborId ->
                                                    val nPos = nodeOffsets[neighborId] ?: return@forEach
                                                    val nSize = nodeSizes[neighborId] ?: IntSize(nodeWidthPx.roundToInt(), fallbackHeightPx.roundToInt())
                                                    val nCx = nPos.x + nSize.width * 0.5f
                                                    val nCy = nPos.y + nSize.height * 0.5f
                                                    val myCx = nextX + mySize.width * 0.5f
                                                    val myCy = nextY + mySize.height * 0.5f
                                                    val dist = hypot((myCx - nCx).toDouble(), (myCy - nCy).toDouble()).toFloat()
                                                    if (dist > maxEdgeDistPx && dist > 0.001f) {
                                                        val ratio = maxEdgeDistPx / dist
                                                        nextX = nCx + (myCx - nCx) * ratio - mySize.width * 0.5f
                                                        nextY = nCy + (myCy - nCy) * ratio - mySize.height * 0.5f
                                                    }
                                                }

                                                nodeOffsets[node.id] = Offset(nextX, nextY)
                                            }
                                        }

                                        if (!isDragging) {
                                            focusedEdgeId = null
                                            if (spark.isOfficial) {
                                                // Official IFX Sparks: tap opens input editor directly
                                                focusedNodeId = node.id
                                                editingNodeId = node.id
                                            } else {
                                                // User Sparks: 1st tap selects (ready for Backspace or `⌫` / `Edit`); 2nd tap opens detailed editor!
                                                if (focusedNodeId == node.id) {
                                                    editingNodeId = node.id
                                                } else {
                                                    focusedNodeId = node.id
                                                }
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }
            }

            // 3. Clean Top-Left Reset Pill
            Surface(
                onClick = { resetToBeginning() },
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E6EC)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 4.dp)
            ) {
                Text(
                    text = "↺ Reset",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = IfxBlack,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // 4. Clean Top-Right Zoom Pill
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E6EC)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "−",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = IfxBlack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { canvasZoom = (canvasZoom - 0.12f).coerceAtLeast(0.60f) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Text(
                        text = "${(canvasZoom * 100).roundToInt()}%",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color(0xFF525866),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    Text(
                        text = "+",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = IfxBlack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { canvasZoom = (canvasZoom + 0.12f).coerceAtMost(1.24f) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // 5. Context-Aware Minimal Bottom Dock:
            //    - When a node or edge is selected (User Spark): shows `Edit` (for node) + `⌫ Delete` (matches keyboard Backspace!)
            //    - Otherwise: `AI Create` · (`+ Step` ONLY for User Sparks, hidden for Official IFX Sparks!) · `Dry-run`
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E6EC)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)
                ) {
                    val hasCustomSelection = !spark.isOfficial && (focusedNodeId != null || focusedEdgeId != null)

                    if (hasCustomSelection) {
                        if (focusedNodeId != null) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(IfxBlack)
                                    .clickable { editingNodeId = focusedNodeId }
                                    .padding(horizontal = 15.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Edit node",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = IfxLime
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2))
                                .clickable { deleteFocusedSelection() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (focusedEdgeId != null) "⌫ Delete edge" else "⌫ Delete",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color(0xFFDC2626)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    focusedNodeId = null
                                    focusedEdgeId = null
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Done",
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF525866)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(IfxBlack)
                                .clickable(onClick = onCreateWithAiClick)
                                .padding(horizontal = 13.dp, vertical = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_gemini_color),
                                contentDescription = "AI",
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "AI Create",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color.White
                            )
                        }

                        // Official IFX Sparks cannot add new steps — only User Sparks show `+ Step`
                        if (!spark.isOfficial) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showAddStepSheet = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "+ Step",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = IfxBlack
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isTestingFlow) IfxLime else Color(0xFFF2F4F7))
                                .clickable(enabled = !isTestingFlow) { triggerDryRun() }
                                .padding(horizontal = 13.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isTestingFlow) "Testing..." else "Dry-run",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = IfxBlack
                            )
                        }
                    }
                }
            }
        }
    }

    // 6. Detailed, Professional Node Editor Sheet
    val activeNode = editingNodeId?.let { id -> nodes.firstOrNull { it.id == id } }
    if (activeNode != null) {
        val outgoingEdge = edges.firstOrNull { it.fromId == activeNode.id }
        val connectedNextNode = outgoingEdge?.let { e -> nodes.firstOrNull { it.id == e.toId } }
        val connectableTargets = nodes.filter { it.id != activeNode.id }

        IfxDetailedNodeSheet(
            node = activeNode,
            topic = topic,
            isOfficialSpark = spark.isOfficial,
            allParams = draftParams,
            connectedNextNode = connectedNextNode,
            connectableTargets = connectableTargets,
            canDelete = !spark.isOfficial && nodes.size > 1,
            onDismiss = { editingNodeId = null },
            onUpdateNode = { updatedNode ->
                commitNodes(nodes.map { if (it.id == updatedNode.id) updatedNode else it })
                validationIssues = validationIssues.filterNot { it.nodeId == updatedNode.id }
            },
            onUpdateParam = { key, newValue ->
                val updated = draftParams.map { if (it.key == key) it.copy(value = newValue) else it }
                draftParams = updated
                syncParamsIntoNodes(updated)
                validationIssues = validationIssues.filterNot { it.nodeId == activeNode.id }
            },
            onSetNextNode = { targetId ->
                val cleaned = edges.filterNot { it.fromId == activeNode.id }
                edges = if (targetId != null) {
                    cleaned + IfxFlowEdge("edge_${activeNode.id}_$targetId", activeNode.id, targetId)
                } else {
                    cleaned
                }
                hasUnsavedChanges = true
            },
            onDeleteNode = {
                if (!spark.isOfficial && nodes.size > 1) {
                    val targetId = activeNode.id
                    editingNodeId = null
                    focusedNodeId = null
                    edges = edges.filterNot { it.fromId == targetId || it.toId == targetId }
                    commitNodes(nodes.filterNot { it.id == targetId })
                }
            }
        )
    }

    // 7. Detailed IFX "+ Step" Composer Sheet (User Sparks only!)
    if (showAddStepSheet && !spark.isOfficial) {
        IfxDetailedAddStepSheet(
            existingNodes = nodes,
            onDismiss = { showAddStepSheet = false },
            onAddStep = { roleLabel, roleTag, service, stepTitle, timing, onFailure, connectFromId ->
                val newId = "added_${System.currentTimeMillis()}"
                val newNode = IfxFlowNode(
                    id = newId,
                    label = roleLabel,
                    roleTag = roleTag,
                    serviceName = service.displayName,
                    title = stepTitle,
                    detailHint = "Runs via ${service.displayName} · $timing",
                    timingRule = timing,
                    onFailureRule = onFailure,
                    iconKind = service.iconKind,
                    isTrigger = false,
                    summaryValue = "${service.displayName} · $timing",
                    isOfficialCore = false,
                    isOriginalNode = false
                )
                val anchorNode = nodes.firstOrNull { it.id == connectFromId } ?: nodes.lastOrNull()
                val anchorPos = anchorNode?.let { nodeOffsets[it.id] } ?: Offset(48f, 220f)
                nodeOffsets[newId] = Offset(anchorPos.x, anchorPos.y + gapYPx)
                if (anchorNode != null) {
                    edges = edges + IfxFlowEdge(
                        id = "edge_${anchorNode.id}_$newId",
                        fromId = anchorNode.id,
                        toId = newId
                    )
                }
                commitNodes(nodes + newNode)
                focusedNodeId = newId
                showAddStepSheet = false
            }
        )
    }

    // 8. Dry-Run Validation Error Alert Dialog
    if (showValidationDialog && validationIssues.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showValidationDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Dry-run found issues",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    color = IfxBlack
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Fix these inputs before running or saving this Spark:",
                        fontFamily = Inter,
                        fontSize = 13.5.sp,
                        color = Color(0xFF525866)
                    )
                    validationIssues.forEach { issue ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("•", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            Text(
                                text = "${issue.nodeLabel}: ${issue.message}",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = IfxBlack
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showValidationDialog = false
                        editingNodeId = validationIssues.first().nodeId
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = IfxBlack, contentColor = IfxLime)
                ) {
                    Text("Fix node", fontFamily = Inter, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showValidationDialog = false }) {
                    Text("Dismiss", fontFamily = Inter, color = Color(0xFF525866))
                }
            }
        )
    }

    // 9. Dry-Run Success Dialog
    if (showDryRunSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showDryRunSuccessDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Dry-run passed",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    color = IfxBlack
                )
            },
            text = {
                Text(
                    text = "All ${nodes.size} steps and inputs are valid and ready to run.",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    color = Color(0xFF525866)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDryRunSuccessDialog = false },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = IfxBlack, contentColor = IfxLime)
                ) {
                    Text("Got it", fontFamily = Inter, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Clean Workflow Node Card (ZERO "Tap to edit" clutter):
 * - Floating `● Start` pill above the first node.
 * - Top row: Unique vector icon + label + crisp role/status tag.
 * - Middle: Soft `#F5F6F8` Parameter/Step inset box.
 * - Bottom: Clean service badge + value pill.
 */
@Composable
private fun IfxPhoto2StyleNodeCard(
    node: IfxFlowNode,
    topic: SparkTopic,
    isFirstStartNode: Boolean,
    isSelected: Boolean,
    isPassed: Boolean,
    hasError: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        hasError -> Color(0xFFDC2626)
        isPassed -> Color(0xFF16A34A)
        isSelected -> IfxBlack
        else -> Color(0xFFE2E6EC)
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        if (isFirstStartNode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .padding(start = 6.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1FCCF))
                    .border(1.dp, IfxBlack.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(IfxBlack)
                )
                Text(
                    text = "Start",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = IfxBlack
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = if (isSelected) 7.dp else 3.dp,
            border = BorderStroke(if (isSelected || isPassed || hasError) 2.dp else 1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        SparkGlyphTile(
                            iconKind = node.iconKind,
                            size = 30.dp,
                            cornerRadius = 9.dp,
                            containerColor = if (node.isTrigger) IfxBlack else topic.heroBg,
                            iconTint = if (node.isTrigger) IfxLime else topic.heroContent,
                            borderColor = IfxBlack.copy(alpha = 0.10f),
                            borderWidth = 1.dp
                        )
                        Text(
                            text = node.label,
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp,
                            color = IfxBlack
                        )
                    }

                    val tagText = when {
                        hasError -> "Fix input"
                        isPassed -> "✓ Passed"
                        else -> node.roleTag
                    }
                    val tagBg = when {
                        hasError -> Color(0xFFFEE2E2)
                        isPassed -> Color(0xFFDCFCE7)
                        !node.isOriginalNode -> Color(0xFFF2FCD8)
                        else -> Color(0xFFF3F4F6)
                    }
                    val tagFg = when {
                        hasError -> Color(0xFFDC2626)
                        isPassed -> Color(0xFF15803D)
                        else -> Color(0xFF474D57)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(tagBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tagText,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = tagFg
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color(0xFFF5F6F8))
                        .padding(horizontal = 11.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = node.title,
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp,
                        color = IfxBlack,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Clean footer metadata row: Service chip + live value (NO "Tap to edit")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .border(1.dp, Color(0xFFE2E6EC), RoundedCornerShape(7.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = node.serviceName,
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = IfxBlack
                        )
                    }

                    if (node.summaryValue.isNotBlank()) {
                        Text(
                            text = node.summaryValue,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = Color(0xFF525866),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detailed, Structured IFX "+ Step" Composer Sheet (User Sparks only):
 * Lets user choose Step Type (Action / Condition / AI), Service Integration (auto-sets icon),
 * Action description, Timing rule, On-Failure behavior, and which existing node it connects from.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IfxDetailedAddStepSheet(
    existingNodes: List<IfxFlowNode>,
    onDismiss: () -> Unit,
    onAddStep: (
        roleLabel: String,
        roleTag: String,
        service: IfxServiceApp,
        stepTitle: String,
        timing: String,
        onFailure: String,
        connectFromId: String
    ) -> Unit
) {
    var selectedTypeIndex by remember { mutableIntStateOf(0) }
    var selectedService by remember { mutableStateOf(IfxServiceApp.NOTIFICATION) }
    var stepTitle by remember { mutableStateOf("") }
    var selectedTiming by remember { mutableStateOf("Immediately") }
    var selectedFailure by remember { mutableStateOf("Retry once") }
    var connectFromId by remember(existingNodes) { mutableStateOf(existingNodes.lastOrNull()?.id ?: "") }
    var stepError by remember { mutableStateOf<String?>(null) }

    var serviceMenuOpen by remember { mutableStateOf(false) }
    var timingMenuOpen by remember { mutableStateOf(false) }
    var failureMenuOpen by remember { mutableStateOf(false) }
    var connectMenuOpen by remember { mutableStateOf(false) }

    val stepTypes = listOf(
        Triple("And do", "Action", "Run an app action"),
        Triple("Check", "Condition", "Filter before next step"),
        Triple("AI Step", "AI", "Summarize or extract with Gemini")
    )
    val timingOptions = listOf("Immediately", "Wait 5 min", "Wait 15 min", "Weekdays only", "Ask confirmation")
    val failureOptions = listOf("Retry once", "Skip & continue", "Notify me")

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
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "New Workflow Step",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = IfxBlack
                    )
                    Text(
                        text = stepTypes[selectedTypeIndex].third,
                        fontFamily = Inter,
                        fontSize = 12.5.sp,
                        color = Color(0xFF686D76),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IfxBlack)
                }
            }

            // 1. Step Type Segmented Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFFF3F4F6))
                    .padding(3.dp)
            ) {
                stepTypes.forEachIndexed { idx, (_, tag, _) ->
                    val active = selectedTypeIndex == idx
                    Surface(
                        onClick = {
                            selectedTypeIndex = idx
                            if (idx == 2) selectedService = IfxServiceApp.GEMINI
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (active) Color.White else Color.Transparent,
                        shadowElevation = if (active) 1.dp else 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tag,
                                fontFamily = Inter,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (active) IfxBlack else Color(0xFF686D76)
                            )
                        }
                    }
                }
            }

            // 2. Service Integration Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                SelectorInputRow(
                    label = "App / Service",
                    value = selectedService.displayName,
                    iconKind = selectedService.iconKind,
                    actionHint = "Select ▾",
                    onClick = { serviceMenuOpen = true }
                )
                DropdownMenu(
                    expanded = serviceMenuOpen,
                    onDismissRequest = { serviceMenuOpen = false },
                    containerColor = Color.White,
                    border = BorderStroke(1.2.dp, IfxBlack),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    IfxServiceApp.entries.forEach { app ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    SparkGlyphVector(app.iconKind, IfxBlack, Modifier.size(16.dp))
                                    Text(app.displayName, fontFamily = Inter, fontWeight = FontWeight.Medium, color = IfxBlack)
                                }
                            },
                            onClick = {
                                serviceMenuOpen = false
                                selectedService = app
                            }
                        )
                    }
                }
            }

            // 3. Step Action Description
            OutlinedTextField(
                value = stepTitle,
                onValueChange = {
                    stepTitle = it
                    stepError = null
                },
                label = { Text("What should this step do?", fontFamily = Inter) },
                placeholder = { Text("e.g. Save summary into Notebook", fontFamily = Inter, color = Color(0xFF98A2B3)) },
                isError = stepError != null,
                supportingText = stepError?.let { msg -> { Text(msg, fontFamily = Inter) } },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IfxBlack,
                    unfocusedBorderColor = Color(0xFFD0D5DD),
                    focusedLabelColor = IfxBlack,
                    cursorColor = IfxBlack
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Timing & On-Failure 2-Column Dropdowns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CompactDropdownSelector(
                        label = "Timing",
                        value = selectedTiming,
                        onClick = { timingMenuOpen = true }
                    )
                    DropdownMenu(
                        expanded = timingMenuOpen,
                        onDismissRequest = { timingMenuOpen = false },
                        containerColor = Color.White,
                        border = BorderStroke(1.2.dp, IfxBlack),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        timingOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, fontFamily = Inter, color = IfxBlack) },
                                onClick = {
                                    timingMenuOpen = false
                                    selectedTiming = opt
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    CompactDropdownSelector(
                        label = "On failure",
                        value = selectedFailure,
                        onClick = { failureMenuOpen = true }
                    )
                    DropdownMenu(
                        expanded = failureMenuOpen,
                        onDismissRequest = { failureMenuOpen = false },
                        containerColor = Color.White,
                        border = BorderStroke(1.2.dp, IfxBlack),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        failureOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, fontFamily = Inter, color = IfxBlack) },
                                onClick = {
                                    failureMenuOpen = false
                                    selectedFailure = opt
                                }
                            )
                        }
                    }
                }
            }

            // 5. Connect After Dropdown
            if (existingNodes.isNotEmpty()) {
                val anchorNode = existingNodes.firstOrNull { it.id == connectFromId } ?: existingNodes.last()
                Box(modifier = Modifier.fillMaxWidth()) {
                    CompactDropdownSelector(
                        label = "Connect after",
                        value = "${anchorNode.label}: ${anchorNode.title}",
                        onClick = { connectMenuOpen = true }
                    )
                    DropdownMenu(
                        expanded = connectMenuOpen,
                        onDismissRequest = { connectMenuOpen = false },
                        containerColor = Color.White,
                        border = BorderStroke(1.2.dp, IfxBlack),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        existingNodes.forEach { candidate ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${candidate.label}: ${candidate.title}",
                                        fontFamily = Inter,
                                        color = IfxBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                onClick = {
                                    connectMenuOpen = false
                                    connectFromId = candidate.id
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val clean = stepTitle.trim()
                    if (clean.length < 3 || looksLikeGibberish(clean)) {
                        stepError = "Enter a clear step description."
                        return@Button
                    }
                    val (roleLabel, roleTag, _) = stepTypes[selectedTypeIndex]
                    onAddStep(roleLabel, roleTag, selectedService, clean, selectedTiming, selectedFailure, connectFromId)
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = IfxBlack, contentColor = IfxLime),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add step to workflow", fontFamily = Inter, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
        }
    }
}

/**
 * Detailed, Structured Node Inspector Sheet:
 * - Overview strip (Service · Timing · Failure policy)
 * - Editable step statement (or locked official preview)
 * - Strictly typed parameters (`TimePickerDialog`, `DatePickerDialog`, numeric-only `Duration` + Unit dropdown,
 *   black-bordered `Destination` / `Day` dropdowns, validated text)
 * - For User Sparks: Service switcher, Timing & On-Failure rules, Next-step connection selector, and Remove button!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IfxDetailedNodeSheet(
    node: IfxFlowNode,
    topic: SparkTopic,
    isOfficialSpark: Boolean,
    allParams: List<SparkEditableParam>,
    connectedNextNode: IfxFlowNode?,
    connectableTargets: List<IfxFlowNode>,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onUpdateNode: (IfxFlowNode) -> Unit,
    onUpdateParam: (key: String, value: String) -> Unit,
    onSetNextNode: (String?) -> Unit,
    onDeleteNode: () -> Unit
) {
    val context = LocalContext.current
    var titleDraft by remember(node.id, node.title) { mutableStateOf(node.title) }
    var serviceMenuOpen by remember(node.id) { mutableStateOf(false) }
    var timingMenuOpen by remember(node.id) { mutableStateOf(false) }
    var failureMenuOpen by remember(node.id) { mutableStateOf(false) }
    var nextStepMenuOpen by remember(node.id) { mutableStateOf(false) }

    val nodeParams = remember(allParams, node.isTrigger) {
        val matching = allParams.filter { it.appliesToTrigger == node.isTrigger }
        if (matching.isNotEmpty()) matching else allParams
    }
    val timingOptions = listOf("Real-time", "Immediately", "Wait 5 min", "Wait 15 min", "Weekdays only")
    val failureOptions = listOf("Retry once", "Skip & continue", "Notify me", "Log & retry")

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = IfxBlack,
        unfocusedBorderColor = Color(0xFFD0D5DD),
        focusedLabelColor = IfxBlack,
        cursorColor = IfxBlack,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
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
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Detailed Header (weight(1f) keeps right-side ✕ button cleanly inside screen bounds!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SparkGlyphTile(
                        iconKind = node.iconKind,
                        size = 44.dp,
                        cornerRadius = 13.dp,
                        containerColor = IfxBlack,
                        iconTint = topic.tileIcon,
                        borderWidth = 0.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text(
                                text = node.label,
                                fontFamily = Inter,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = IfxBlack,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = node.roleTag,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF474D57)
                                )
                            }
                        }
                        Text(
                            text = node.detailHint,
                            fontFamily = Inter,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF686D76),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IfxBlack)
                }
            }

            // 2. Scannable Node Metadata Strip (Service · Timing · Failure policy)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF7F8FA),
                border = BorderStroke(1.dp, Color(0xFFEAECF0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NodeMetaColumn(label = "Service", value = node.serviceName, modifier = Modifier.weight(1f))
                    NodeMetaColumn(label = "Timing", value = node.timingRule, modifier = Modifier.weight(1f))
                    NodeMetaColumn(label = "On failure", value = node.onFailureRule, modifier = Modifier.weight(1f))
                }
            }

            // 3. Execution Statement
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Execution",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF525866)
                )
                if (isOfficialSpark && node.isOfficialCore) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF6F7F9),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = node.title,
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.5.sp,
                            color = IfxBlack,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                } else {
                    val titleErr = if (titleDraft.trim().length < 3 || looksLikeGibberish(titleDraft)) {
                        "Enter a clear step description"
                    } else null
                    OutlinedTextField(
                        value = titleDraft,
                        onValueChange = {
                            titleDraft = it
                            onUpdateNode(node.copy(title = it))
                        },
                        isError = titleErr != null,
                        supportingText = titleErr?.let { msg -> { Text(msg, fontFamily = Inter) } },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 4. Typed Inputs (Official IFX Sparks)
            if (nodeParams.isNotEmpty() && node.isOfficialCore) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Configuration",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF525866)
                    )

                    nodeParams.forEach { param ->
                        when (param.type) {
                            SparkParamType.TIME -> {
                                SelectorInputRow(
                                    label = param.label,
                                    value = param.value,
                                    iconKind = SparkIconKind.CLOCK_TRIGGER,
                                    actionHint = "Pick time",
                                    onClick = {
                                        val (h, m) = parseHourMinute(param.value)
                                        TimePickerDialog(
                                            context,
                                            { _, hour, min ->
                                                onUpdateParam(param.key, format12HourTime(hour, min))
                                            },
                                            h,
                                            m,
                                            false
                                        ).show()
                                    }
                                )
                            }

                            SparkParamType.DAY -> {
                                var expanded by remember(param.key) { mutableStateOf(false) }
                                val dayOptions = listOf(
                                    "Daily", "Weekdays", "Weekends",
                                    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    SelectorInputRow(
                                        label = param.label,
                                        value = param.value,
                                        iconKind = SparkIconKind.CALENDAR_EVENT,
                                        actionHint = "Select ▾",
                                        onClick = { expanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        containerColor = Color.White,
                                        border = BorderStroke(1.2.dp, IfxBlack),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        dayOptions.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option, fontFamily = Inter, fontWeight = FontWeight.Medium, color = IfxBlack) },
                                                onClick = {
                                                    expanded = false
                                                    onUpdateParam(param.key, option)
                                                }
                                            )
                                        }
                                        HorizontalDivider(color = Color(0xFFE5E7EB))
                                        DropdownMenuItem(
                                            text = { Text("Pick specific date...", fontFamily = Inter, fontWeight = FontWeight.Bold, color = IfxBlack) },
                                            onClick = {
                                                expanded = false
                                                val cal = Calendar.getInstance()
                                                DatePickerDialog(
                                                    context,
                                                    { _, y, m, d ->
                                                        val picked = Calendar.getInstance().apply { set(y, m, d) }
                                                        val formatted = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).format(picked.time)
                                                        onUpdateParam(param.key, formatted)
                                                    },
                                                    cal.get(Calendar.YEAR),
                                                    cal.get(Calendar.MONTH),
                                                    cal.get(Calendar.DAY_OF_MONTH)
                                                ).show()
                                            }
                                        )
                                    }
                                }
                            }

                            SparkParamType.DURATION -> {
                                val (initialNum, initialUnit) = splitDuration(param.value)
                                var numText by remember(param.key, param.value) { mutableStateOf(initialNum) }
                                var unitText by remember(param.key, param.value) { mutableStateOf(initialUnit) }
                                var unitMenuOpen by remember(param.key) { mutableStateOf(false) }
                                val unitOptions = listOf("min", "min before", "hours before", "days before")
                                val numInt = numText.toIntOrNull() ?: 0
                                val isNumInvalid = numInt <= 0

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = param.label,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = IfxBlack
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        OutlinedTextField(
                                            value = numText,
                                            onValueChange = { raw ->
                                                val digitsOnly = raw.filter { it.isDigit() }.take(3)
                                                numText = digitsOnly
                                                val formatted = if (digitsOnly.isBlank()) "" else "$digitsOnly $unitText"
                                                onUpdateParam(param.key, formatted)
                                            },
                                            placeholder = { Text("15", fontFamily = Inter) },
                                            isError = isNumInvalid,
                                            supportingText = if (isNumInvalid) {
                                                { Text("Numbers > 0 only", fontFamily = Inter) }
                                            } else null,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = fieldColors,
                                            modifier = Modifier.weight(0.42f)
                                        )

                                        Box(modifier = Modifier.weight(0.58f)) {
                                            Surface(
                                                onClick = { unitMenuOpen = true },
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color.White,
                                                border = BorderStroke(1.2.dp, IfxBlack),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = unitText,
                                                        fontFamily = Inter,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                        color = IfxBlack
                                                    )
                                                    Text("▾", fontSize = 14.sp, color = Color(0xFF686D76))
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = unitMenuOpen,
                                                onDismissRequest = { unitMenuOpen = false },
                                                containerColor = Color.White,
                                                border = BorderStroke(1.2.dp, IfxBlack),
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                unitOptions.forEach { u ->
                                                    DropdownMenuItem(
                                                        text = { Text(u, fontFamily = Inter, fontWeight = FontWeight.Medium, color = IfxBlack) },
                                                        onClick = {
                                                            unitMenuOpen = false
                                                            unitText = u
                                                            val safeNum = numText.ifBlank { "1" }
                                                            onUpdateParam(param.key, "$safeNum $u")
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            SparkParamType.DESTINATION -> {
                                var expanded by remember(param.key) { mutableStateOf(false) }
                                val options = if (param.presets.isNotEmpty()) {
                                    param.presets
                                } else {
                                    listOf("Primary", "Work", "Personal", "Expenses", "Archive")
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    SelectorInputRow(
                                        label = param.label,
                                        value = param.value,
                                        iconKind = SparkIconKind.NOTEBOOK_PAGE,
                                        actionHint = "Select ▾",
                                        onClick = { expanded = true }
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        containerColor = Color.White,
                                        border = BorderStroke(1.2.dp, IfxBlack),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        options.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option, fontFamily = Inter, fontWeight = FontWeight.Medium, color = IfxBlack) },
                                                onClick = {
                                                    expanded = false
                                                    onUpdateParam(param.key, option)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            SparkParamType.KEYWORD, SparkParamType.ACTIVITY -> {
                                val err = validateParamValue(param)
                                OutlinedTextField(
                                    value = param.value,
                                    onValueChange = { onUpdateParam(param.key, it) },
                                    label = { Text(param.label, fontFamily = Inter) },
                                    isError = err != null,
                                    supportingText = err?.let { msg -> { Text(msg, fontFamily = Inter) } },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = fieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // 5. Full Behavior & Routing Controls (User Sparks only!)
            if (!isOfficialSpark) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Step Settings",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF525866)
                    )

                    // Service Switcher
                    Box(modifier = Modifier.fillMaxWidth()) {
                        SelectorInputRow(
                            label = "App / Service",
                            value = node.serviceName,
                            iconKind = node.iconKind,
                            actionHint = "Change ▾",
                            onClick = { serviceMenuOpen = true }
                        )
                        DropdownMenu(
                            expanded = serviceMenuOpen,
                            onDismissRequest = { serviceMenuOpen = false },
                            containerColor = Color.White,
                            border = BorderStroke(1.2.dp, IfxBlack),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            IfxServiceApp.entries.forEach { app ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            SparkGlyphVector(app.iconKind, IfxBlack, Modifier.size(16.dp))
                                            Text(app.displayName, fontFamily = Inter, color = IfxBlack)
                                        }
                                    },
                                    onClick = {
                                        serviceMenuOpen = false
                                        onUpdateNode(node.copy(serviceName = app.displayName, iconKind = app.iconKind))
                                    }
                                )
                            }
                        }
                    }

                    // Timing & On-Failure Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            CompactDropdownSelector(
                                label = "Timing",
                                value = node.timingRule,
                                onClick = { timingMenuOpen = true }
                            )
                            DropdownMenu(
                                expanded = timingMenuOpen,
                                onDismissRequest = { timingMenuOpen = false },
                                containerColor = Color.White,
                                border = BorderStroke(1.2.dp, IfxBlack),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                timingOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt, fontFamily = Inter, color = IfxBlack) },
                                        onClick = {
                                            timingMenuOpen = false
                                            onUpdateNode(node.copy(timingRule = opt))
                                        }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            CompactDropdownSelector(
                                label = "On failure",
                                value = node.onFailureRule,
                                onClick = { failureMenuOpen = true }
                            )
                            DropdownMenu(
                                expanded = failureMenuOpen,
                                onDismissRequest = { failureMenuOpen = false },
                                containerColor = Color.White,
                                border = BorderStroke(1.2.dp, IfxBlack),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                failureOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt, fontFamily = Inter, color = IfxBlack) },
                                        onClick = {
                                            failureMenuOpen = false
                                            onUpdateNode(node.copy(onFailureRule = opt))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Outgoing Edge Connection Selector (lets user reconnect after deleting an edge!)
                    if (connectableTargets.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            CompactDropdownSelector(
                                label = "Next connected step",
                                value = connectedNextNode?.let { "${it.label}: ${it.title}" } ?: "Not connected",
                                onClick = { nextStepMenuOpen = true }
                            )
                            DropdownMenu(
                                expanded = nextStepMenuOpen,
                                onDismissRequest = { nextStepMenuOpen = false },
                                containerColor = Color.White,
                                border = BorderStroke(1.2.dp, IfxBlack),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (disconnect)", fontFamily = Inter, color = Color(0xFF686D76)) },
                                    onClick = {
                                        nextStepMenuOpen = false
                                        onSetNextNode(null)
                                    }
                                )
                                connectableTargets.forEach { target ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${target.label}: ${target.title}",
                                                fontFamily = Inter,
                                                color = IfxBlack,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        onClick = {
                                            nextStepMenuOpen = false
                                            onSetNextNode(target.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (canDelete) {
                    OutlinedButton(
                        onClick = onDeleteNode,
                        shape = CircleShape,
                        border = BorderStroke(1.2.dp, Color(0xFFF3D1D1)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        contentPadding = PaddingValues(vertical = 13.dp),
                        modifier = Modifier.weight(0.42f)
                    ) {
                        Text("⌫ Remove", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = IfxBlack, contentColor = IfxLime),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    modifier = Modifier.weight(if (canDelete) 0.58f else 1f)
                ) {
                    Text(
                        text = "Apply",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NodeMetaColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color(0xFF8C929D)
        )
        Text(
            text = value,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = IfxBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompactDropdownSelector(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = IfxBlack
        )
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(13.dp),
            color = Color.White,
            border = BorderStroke(1.2.dp, IfxBlack),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    color = IfxBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("▾", fontSize = 13.sp, color = Color(0xFF686D76))
            }
        }
    }
}

@Composable
private fun SelectorInputRow(
    label: String,
    value: String,
    iconKind: SparkIconKind,
    actionHint: String,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = IfxBlack
        )
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            border = BorderStroke(1.2.dp, IfxBlack),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SparkGlyphVector(iconKind = iconKind, tint = IfxBlack, modifier = Modifier.size(18.dp))
                    Text(
                        text = value,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = IfxBlack
                    )
                }
                Text(
                    text = actionHint,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = Color(0xFF686D76)
                )
            }
        }
    }
}

// --- Strict Validation & Formatting Helpers ---

private fun validateParamValue(param: SparkEditableParam): String? {
    val v = param.value.trim()
    if (v.isEmpty()) return "${param.label} cannot be empty."
    return when (param.type) {
        SparkParamType.TIME -> {
            val ok = Regex("""^\d{1,2}:\d{2}\s?(AM|PM)$""", RegexOption.IGNORE_CASE).matches(v)
            if (!ok) "Pick a valid time using the clock picker." else null
        }
        SparkParamType.DURATION -> {
            val match = Regex("""^(\d{1,3})\s+(min|min before|hour before|hours before|day before|days before)$""", RegexOption.IGNORE_CASE).find(v)
            val num = match?.groupValues?.get(1)?.toIntOrNull() ?: 0
            if (match == null || num <= 0) "Enter a valid number greater than 0 with a unit." else null
        }
        SparkParamType.DAY, SparkParamType.DESTINATION -> {
            if (v.length < 2) "Select a valid option." else null
        }
        SparkParamType.KEYWORD, SparkParamType.ACTIVITY -> {
            when {
                v.length < 2 -> "Must be at least 2 characters."
                v.all { it.isDigit() } -> "Enter words, not just numbers."
                looksLikeGibberish(v) -> "\"$v\" doesn't look like a valid word."
                else -> null
            }
        }
    }
}

private fun looksLikeGibberish(text: String): Boolean {
    val clean = text.trim().lowercase(Locale.ENGLISH)
    val letters = clean.filter { it.isLetter() }
    if (letters.length < 2) return true
    if (Regex("""(.)\1{3,}""").containsMatchIn(letters)) return true
    val mashPatterns = listOf("asdf", "qwer", "zxcv", "aada", "dsad", "fghj", "hjkl", "jkl")
    if (mashPatterns.any { letters.contains(it) }) return true
    val words = clean.split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.size == 1 && letters.length >= 4 && letters.none { it in "aeiouy" }) return true
    return false
}

private fun splitDuration(value: String): Pair<String, String> {
    val match = Regex("""^(\d{1,3})\s*(.*)$""").find(value.trim()) ?: return "15" to "min before"
    val num = match.groupValues[1].ifBlank { "15" }
    val rawUnit = match.groupValues[2].trim().lowercase(Locale.ENGLISH)
    val unit = when {
        rawUnit.startsWith("day") -> "days before"
        rawUnit.startsWith("hour") -> "hours before"
        rawUnit.contains("before") -> "min before"
        rawUnit == "min" -> "min"
        else -> "min before"
    }
    return num to unit
}

private fun parseHourMinute(value: String): Pair<Int, Int> {
    val regex = Regex("""(\d{1,2}):(\d{2})\s*(AM|PM)?""", RegexOption.IGNORE_CASE)
    val match = regex.find(value) ?: return 9 to 0
    var hour = match.groupValues[1].toIntOrNull() ?: 9
    val minute = match.groupValues[2].toIntOrNull() ?: 0
    val period = match.groupValues[3].uppercase(Locale.ENGLISH)
    if (period == "PM" && hour < 12) hour += 12
    if (period == "AM" && hour == 12) hour = 0
    return hour.coerceIn(0, 23) to minute.coerceIn(0, 59)
}

private fun format12HourTime(hourOfDay: Int, minute: Int): String {
    val period = if (hourOfDay >= 12) "PM" else "AM"
    val displayHour = when {
        hourOfDay == 0 -> 12
        hourOfDay > 12 -> hourOfDay - 12
        else -> hourOfDay
    }
    return String.format(Locale.ENGLISH, "%02d:%02d %s", displayHour, minute, period)
}
