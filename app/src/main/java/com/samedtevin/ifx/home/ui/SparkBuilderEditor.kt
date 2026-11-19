package com.samedtevin.ifx.home.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class BuilderFlowNode(
    val id: String,
    val badge: String,
    val variable: String,
    val title: String,
    val iconKind: SparkIconKind,
    val isTrigger: Boolean,
    val actionIndex: Int = -1,
    val previewPills: List<Pair<String, String>> = emptyList()
)

/**
 * Interactive n8n-inspired Drag-and-Drop Flowchart Builder rendered directly over IFX's grid canvas.
 * - Identical node-graph canvas experience for BOTH Official IFX Sparks (`isOfficial = true`)
 *   and User Sparks (`isOfficial = false`).
 * - Users drag nodes freely on the grid while curved cubic-Bezier edges + arrowheads redraw edge-to-edge.
 * - Tapping any node opens a clean Node Inspector sheet to edit its inputs (When, Where, Time, Activity, Keyword...).
 * - Official IFX Sparks lock structural add/delete; User Sparks allow adding connected nodes via the `[+]` port
 *   and editing full node statements or deleting steps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SparkBuilderEditor(
    spark: SparkRoutineItem,
    onSaveSpark: (SparkRoutineItem) -> Unit,
    onDeleteSpark: (SparkRoutineItem) -> Unit,
    onBackToDetail: (SparkRoutineItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Editable draft state for this Spark
    var draftTitle by remember(spark.id) { mutableStateOf(spark.title) }
    var draftAbout by remember(spark.id) { mutableStateOf(spark.about) }
    var draftTrigger by remember(spark.id) { mutableStateOf(spark.triggerTitle) }
    var draftActions by remember(spark.id) { mutableStateOf(spark.actions) }
    var draftParams by remember(spark.id) { mutableStateOf(spark.params) }
    var draftWhenInput by remember(spark.id) { mutableStateOf(spark.params.firstOrNull { it.appliesToTrigger }?.value ?: "Anytime") }
    var draftWhereInput by remember(spark.id) { mutableStateOf(spark.params.firstOrNull { !it.appliesToTrigger }?.value ?: "Default app") }

    val liveSpark = remember(spark, draftTitle, draftAbout, draftTrigger, draftActions, draftParams) {
        spark.copy(
            title = draftTitle,
            about = draftAbout,
            triggerTitle = draftTrigger,
            actions = draftActions,
            params = draftParams
        )
    }

    // Build visual node list (`If X` -> `Then do Y` -> `And do Z`)
    val nodes = remember(liveSpark) {
        val triggerPills = if (liveSpark.isOfficial) {
            liveSpark.params.filter { it.appliesToTrigger }.map { it.label to it.value }
        } else {
            listOf("When" to draftWhenInput)
        }
        val actionPills = if (liveSpark.isOfficial) {
            liveSpark.params.filter { !it.appliesToTrigger }.map { it.label to it.value }
        } else {
            listOf("Where" to draftWhereInput)
        }

        val list = mutableListOf(
            BuilderFlowNode(
                id = "node_trigger",
                badge = "If",
                variable = "X",
                title = liveSpark.resolvedTriggerText(),
                iconKind = liveSpark.primaryIcon,
                isTrigger = true,
                previewPills = triggerPills
            )
        )
        liveSpark.resolvedActions().forEachIndexed { idx, actionText ->
            val letter = ('Y'.code + idx).coerceAtMost('Z'.code).toChar().toString()
            list.add(
                BuilderFlowNode(
                    id = "node_action_$idx",
                    badge = if (idx == 0) "Then do" else "And do",
                    variable = letter,
                    title = actionText,
                    iconKind = liveSpark.actionIcon,
                    isTrigger = false,
                    actionIndex = idx,
                    previewPills = if (idx == 0) actionPills else emptyList()
                )
            )
        }
        list
    }

    // Node positions & measured pixel sizes for accurate edge-to-edge port connectors
    val nodeOffsets = remember(spark.id) { mutableStateMapOf<String, Offset>() }
    val nodeSizes = remember(spark.id) { mutableStateMapOf<String, IntSize>() }
    var selectedNodeId by remember(spark.id) { mutableStateOf<String?>(null) }
    var showSparkMetaSheet by remember(spark.id) { mutableStateOf(false) }
    var canvasZoom by remember(spark.id) { mutableFloatStateOf(1f) }

    val nodeWidthDp = 246.dp
    val nodeWidthPx = with(density) { nodeWidthDp.toPx() }
    val defaultNodeHeightPx = with(density) { 112.dp.toPx() }
    val topicAccent = liveSpark.topic.pillAccent

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // 1. Clean Top Editor Toolbar (n8n-inspired minimal header over grid)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                onClick = { onBackToDetail(spark) },
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.2.dp, IfxBlack),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "← Back",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = IfxBlack,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = !spark.isOfficial) { showSparkMetaSheet = true }
            ) {
                Text(
                    text = liveSpark.title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp,
                    color = IfxBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (spark.isOfficial) "by IFX · Tap nodes to edit inputs" else "by You · Drag or tap nodes",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color(0xFF686D76),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = {
                    val cleanedParams = draftParams.mapIndexed { idx, p ->
                        val fallback = spark.params.getOrNull(idx)?.value ?: p.value
                        p.copy(value = p.value.trim().ifBlank { fallback })
                    }
                    val updated = liveSpark.copy(
                        title = draftTitle.trim().ifBlank { spark.title },
                        about = draftAbout.trim().ifBlank { spark.about },
                        triggerTitle = draftTrigger.trim().ifBlank { spark.triggerTitle },
                        actions = draftActions,
                        params = cleanedParams
                    ).withSyncedSubtitle()
                    onSaveSpark(updated)
                    Toast.makeText(context, "Saved ${updated.title}", Toast.LENGTH_SHORT).show()
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IfxLime,
                    contentColor = IfxBlack
                ),
                border = BorderStroke(1.3.dp, IfxBlack),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 7.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Save",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }

        // 2. Full Interactive Drag-and-Drop Flowchart Canvas (transparent over IFX's existing square grid!)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val canvasHeightPx = constraints.maxHeight.toFloat()
            val gapYPx = with(density) { 164.dp.toPx() }
            val startYPx = with(density) { 44.dp.toPx() }

            // Initialize default clean staggered node positions when first entering canvas or adding a node
            nodes.forEachIndexed { idx, node ->
                if (!nodeOffsets.containsKey(node.id)) {
                    val centerX = ((canvasWidthPx - nodeWidthPx) / 2f).coerceAtLeast(16f)
                    val staggerX = when (idx % 3) {
                        0 -> -18f
                        1 -> 18f
                        else -> 0f
                    }
                    val initX = (centerX + staggerX).coerceIn(12f, (canvasWidthPx - nodeWidthPx - 12f).coerceAtLeast(12f))
                    val initY = (startYPx + idx * gapYPx).coerceIn(16f, (canvasHeightPx - defaultNodeHeightPx - 96f).coerceAtLeast(16f))
                    nodeOffsets[node.id] = Offset(initX, initY)
                }
            }

            // Scaled Canvas Container (nodes + live curved Bezier edge connectors)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = canvasZoom,
                        scaleY = canvasZoom
                    )
            ) {
                // Edge Layer: Live cubic-Bezier connectors + arrowheads connecting node ports edge-to-edge
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val edgeColor = Color(0xFF474D57)
                    val edgeStroke = 2.4.dp.toPx()
                    val arrowLen = 10.dp.toPx()
                    val arrowSpread = 0.48f // radians

                    for (i in 0 until nodes.lastIndex) {
                        val srcNode = nodes[i]
                        val dstNode = nodes[i + 1]
                        val srcPos = nodeOffsets[srcNode.id] ?: continue
                        val dstPos = nodeOffsets[dstNode.id] ?: continue
                        val srcSize = nodeSizes[srcNode.id] ?: IntSize(nodeWidthPx.roundToInt(), defaultNodeHeightPx.roundToInt())
                        val dstSize = nodeSizes[dstNode.id] ?: IntSize(nodeWidthPx.roundToInt(), defaultNodeHeightPx.roundToInt())

                        val srcCenter = Offset(srcPos.x + srcSize.width / 2f, srcPos.y + srcSize.height / 2f)
                        val dstCenter = Offset(dstPos.x + dstSize.width / 2f, dstPos.y + dstSize.height / 2f)
                        val dx = dstCenter.x - srcCenter.x
                        val dy = dstCenter.y - srcCenter.y

                        val isHorizontal = abs(dx) > abs(dy) * 1.15f
                        val start: Offset
                        val end: Offset
                        val ctrl1: Offset
                        val ctrl2: Offset

                        if (isHorizontal) {
                            if (dx >= 0f) {
                                start = Offset(srcPos.x + srcSize.width.toFloat(), srcCenter.y)
                                end = Offset(dstPos.x, dstCenter.y)
                            } else {
                                start = Offset(srcPos.x, srcCenter.y)
                                end = Offset(dstPos.x + dstSize.width.toFloat(), dstCenter.y)
                            }
                            val bend = (abs(end.x - start.x) * 0.48f).coerceAtLeast(42f)
                            val dir = if (dx >= 0f) 1f else -1f
                            ctrl1 = Offset(start.x + bend * dir, start.y)
                            ctrl2 = Offset(end.x - bend * dir, end.y)
                        } else {
                            if (dy >= 0f) {
                                start = Offset(srcCenter.x, srcPos.y + srcSize.height.toFloat())
                                end = Offset(dstCenter.x, dstPos.y)
                            } else {
                                start = Offset(srcCenter.x, srcPos.y)
                                end = Offset(dstCenter.x, dstPos.y + dstSize.height.toFloat())
                            }
                            val bend = (abs(end.y - start.y) * 0.48f).coerceAtLeast(42f)
                            val dir = if (dy >= 0f) 1f else -1f
                            ctrl1 = Offset(start.x, start.y + bend * dir)
                            ctrl2 = Offset(end.x, end.y - bend * dir)
                        }

                        // Curved Bezier connector path
                        val edgePath = Path().apply {
                            moveTo(start.x, start.y)
                            cubicTo(ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, end.x, end.y)
                        }
                        drawPath(
                            path = edgePath,
                            color = edgeColor,
                            style = Stroke(width = edgeStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Arrowhead pointing cleanly into the destination port
                        val angle = atan2((end.y - ctrl2.y).toDouble(), (end.x - ctrl2.x).toDouble()).toFloat()
                        val tip1 = Offset(
                            end.x - arrowLen * cos(angle - arrowSpread),
                            end.y - arrowLen * sin(angle - arrowSpread)
                        )
                        val tip2 = Offset(
                            end.x - arrowLen * cos(angle + arrowSpread),
                            end.y - arrowLen * sin(angle + arrowSpread)
                        )
                        val arrowPath = Path().apply {
                            moveTo(tip1.x, tip1.y)
                            lineTo(end.x, end.y)
                            lineTo(tip2.x, tip2.y)
                        }
                        drawPath(
                            path = arrowPath,
                            color = edgeColor,
                            style = Stroke(width = edgeStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                // Node Layer: Draggable n8n-style cards with port circles
                nodes.forEachIndexed { idx, node ->
                    val pos = nodeOffsets[node.id] ?: Offset(24f, 40f)
                    val isSelected = selectedNodeId == node.id

                    DraggableFlowNodeCard(
                        node = node,
                        accentColor = topicAccent,
                        isSelected = isSelected,
                        showInputPort = idx > 0,
                        showOutputPort = idx < nodes.lastIndex || !spark.isOfficial,
                        showAddStepPort = !spark.isOfficial && idx == nodes.lastIndex,
                        modifier = Modifier
                            .width(nodeWidthDp)
                            .offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                            .onGloballyPositioned { coords ->
                                nodeSizes[node.id] = coords.size
                            }
                            .pointerInput(node.id, canvasZoom) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val cur = nodeOffsets[node.id] ?: Offset.Zero
                                    val measuredW = nodeSizes[node.id]?.width?.toFloat() ?: nodeWidthPx
                                    val measuredH = nodeSizes[node.id]?.height?.toFloat() ?: defaultNodeHeightPx
                                    val nextX = (cur.x + dragAmount.x / canvasZoom)
                                        .coerceIn(8f, (canvasWidthPx - measuredW - 8f).coerceAtLeast(8f))
                                    val nextY = (cur.y + dragAmount.y / canvasZoom)
                                        .coerceIn(8f, (canvasHeightPx - measuredH - 80f).coerceAtLeast(8f))
                                    nodeOffsets[node.id] = Offset(nextX, nextY)
                                }
                            },
                        onClick = { selectedNodeId = node.id },
                        onAddStepClick = {
                            val newIdx = draftActions.size
                            draftActions = draftActions + "New action step"
                            val lastPos = nodeOffsets[node.id] ?: Offset(40f, 160f)
                            nodeOffsets["node_action_$newIdx"] = Offset(
                                x = lastPos.x,
                                y = (lastPos.y + gapYPx).coerceAtMost((canvasHeightPx - defaultNodeHeightPx - 92f).coerceAtLeast(24f))
                            )
                            selectedNodeId = "node_action_$newIdx"
                        }
                    )
                }
            }

            // 3. Floating Bottom-Left Canvas Controls (matching Photo 3's Reset + Zoom bar!)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CanvasControlPill(label = "Reset") {
                    canvasZoom = 1f
                    nodes.forEachIndexed { idx, node ->
                        val centerX = ((canvasWidthPx - nodeWidthPx) / 2f).coerceAtLeast(16f)
                        val staggerX = when (idx % 3) {
                            0 -> -18f
                            1 -> 18f
                            else -> 0f
                        }
                        nodeOffsets[node.id] = Offset(
                            x = (centerX + staggerX).coerceIn(12f, (canvasWidthPx - nodeWidthPx - 12f).coerceAtLeast(12f)),
                            y = (startYPx + idx * gapYPx).coerceIn(16f, (canvasHeightPx - defaultNodeHeightPx - 96f).coerceAtLeast(16f))
                        )
                    }
                }
                CanvasControlPill(label = "−") {
                    canvasZoom = (canvasZoom - 0.12f).coerceAtLeast(0.76f)
                }
                CanvasControlPill(label = "+") {
                    canvasZoom = (canvasZoom + 0.12f).coerceAtMost(1.24f)
                }
            }

            // Floating Bottom-Right Helper Pill (or Delete for user Sparks)
            if (!spark.isOfficial) {
                Surface(
                    onClick = {
                        onDeleteSpark(spark)
                        Toast.makeText(context, "Deleted ${spark.title}", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.2.dp, Color(0xFFD92D20)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 14.dp)
                ) {
                    Text(
                        text = "Delete Spark",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFD92D20),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    // 4. Node Inspector Modal Bottom Sheet (opens when tapping any node card on the canvas!)
    val activeNode = selectedNodeId?.let { id -> nodes.firstOrNull { it.id == id } }
    if (activeNode != null) {
        NodeInspectorSheet(
            node = activeNode,
            isOfficialSpark = spark.isOfficial,
            topicAccent = topicAccent,
            allParams = draftParams,
            whenValue = draftWhenInput,
            whereValue = draftWhereInput,
            canDeleteNode = !spark.isOfficial && !activeNode.isTrigger && draftActions.size > 1,
            onDismiss = { selectedNodeId = null },
            onUpdateParam = { key, newValue ->
                draftParams = draftParams.map { p ->
                    if (p.key == key) p.copy(value = newValue) else p
                }
            },
            onUpdateNodeText = { newText ->
                if (activeNode.isTrigger) {
                    draftTrigger = newText
                } else if (activeNode.actionIndex in draftActions.indices) {
                    draftActions = draftActions.mapIndexed { idx, old ->
                        if (idx == activeNode.actionIndex) newText else old
                    }
                }
            },
            onUpdateWhen = { draftWhenInput = it },
            onUpdateWhere = { draftWhereInput = it },
            onDeleteNode = {
                if (!activeNode.isTrigger && activeNode.actionIndex in draftActions.indices && draftActions.size > 1) {
                    draftActions = draftActions.toMutableList().also { it.removeAt(activeNode.actionIndex) }
                    selectedNodeId = null
                }
            }
        )
    }

    // Optional Spark Name & About Sheet (when tapping the header title on user-created Sparks)
    if (showSparkMetaSheet && !spark.isOfficial) {
        ModalBottomSheet(
            onDismissRequest = { showSparkMetaSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IfxBlack,
                unfocusedBorderColor = IfxBlack.copy(alpha = 0.55f),
                focusedLabelColor = IfxBlack,
                cursorColor = IfxBlack
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Spark Details",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = IfxBlack
                )
                OutlinedTextField(
                    value = draftTitle,
                    onValueChange = { draftTitle = it },
                    label = { Text("Spark name", fontFamily = Inter) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draftAbout,
                    onValueChange = { draftAbout = it },
                    label = { Text("About this Spark", fontFamily = Inter) },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { showSparkMetaSheet = false },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = IfxBlack, contentColor = IfxLime),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontFamily = Inter, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * Single draggable node card styled after n8n's crisp white cards with top/bottom port circles,
 * bespoke [SparkGlyphTile], `If X` / `Then do Y` pill, and live input chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DraggableFlowNodeCard(
    node: BuilderFlowNode,
    accentColor: Color,
    isSelected: Boolean,
    showInputPort: Boolean,
    showOutputPort: Boolean,
    showAddStepPort: Boolean,
    onClick: () -> Unit,
    onAddStepClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = if (isSelected) 10.dp else 5.dp,
                border = BorderStroke(
                    width = if (isSelected) 2.2.dp else 1.4.dp,
                    color = IfxBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        SparkGlyphTile(
                            iconKind = node.iconKind,
                            size = 40.dp,
                            cornerRadius = 12.dp,
                            containerColor = IfxBlack,
                            iconTint = accentColor,
                            borderColor = IfxBlack,
                            borderWidth = 1.dp
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(if (node.isTrigger) IfxBlack else accentColor)
                                        .border(1.dp, IfxBlack, RoundedCornerShape(7.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = node.badge,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.5.sp,
                                        color = if (node.isTrigger) IfxLime else IfxBlack
                                    )
                                }
                                Text(
                                    text = "Edit ✎",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF7B818C)
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = node.title,
                                fontFamily = Inter,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = IfxBlack,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (node.previewPills.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            node.previewPills.forEach { (k, v) ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(Color(0xFFF3F5F8))
                                        .border(1.dp, Color(0xFFD0D5DD), RoundedCornerShape(7.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$k:",
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = Color(0xFF686D76)
                                    )
                                    Text(
                                        text = v,
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

            // Top Input Connection Port Dot
            if (showInputPort) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, IfxBlack, CircleShape)
                )
            }

            // Bottom Output Connection Port Dot
            if (showOutputPort) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(2.dp, IfxBlack, CircleShape)
                )
            }
        }

        // n8n-style `[+]` Add Node Port Button (User Sparks only)
        if (showAddStepPort) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(14.dp)
                    .background(Color(0xFF474D57))
            )
            Surface(
                onClick = onAddStepClick,
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                border = BorderStroke(1.4.dp, IfxBlack),
                shadowElevation = 3.dp,
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = IfxBlack
                    )
                }
            }
        }
    }
}

@Composable
private fun CanvasControlPill(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, IfxBlack),
        shadowElevation = 3.dp
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = IfxBlack,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

/**
 * Node Inspector Modal Sheet opened whenever the user taps any node card on the canvas.
 * - For IFX Official Sparks: edits that node's structured inputs (Time, Activity, Keyword, Calendar...)
 * - For User Sparks: edits the node's text + When / Where inputs (and lets user delete action steps).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NodeInspectorSheet(
    node: BuilderFlowNode,
    isOfficialSpark: Boolean,
    topicAccent: Color,
    allParams: List<SparkEditableParam>,
    whenValue: String,
    whereValue: String,
    canDeleteNode: Boolean,
    onDismiss: () -> Unit,
    onUpdateParam: (key: String, value: String) -> Unit,
    onUpdateNodeText: (String) -> Unit,
    onUpdateWhen: (String) -> Unit,
    onUpdateWhere: (String) -> Unit,
    onDeleteNode: () -> Unit
) {
    val nodeParams = remember(allParams, node.isTrigger) {
        val filtered = allParams.filter { it.appliesToTrigger == node.isTrigger }
        if (filtered.isNotEmpty()) filtered else allParams
    }
    var editableText by remember(node.id) { mutableStateOf(node.title) }
    var editableWhen by remember(node.id, whenValue) { mutableStateOf(whenValue) }
    var editableWhere by remember(node.id, whereValue) { mutableStateOf(whereValue) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = IfxBlack,
        unfocusedBorderColor = IfxBlack.copy(alpha = 0.55f),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SparkGlyphTile(
                    iconKind = node.iconKind,
                    size = 44.dp,
                    cornerRadius = 13.dp,
                    containerColor = IfxBlack,
                    iconTint = topicAccent
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.badge,
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF686D76)
                    )
                    Text(
                        text = if (isOfficialSpark) "Edit Node Inputs" else "Edit Node",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = IfxBlack
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE4E7EC))

            if (isOfficialSpark) {
                // Restricted IFX mode: edit inputs (Time, Activity, Keyword, Repeat, Calendar...)
                nodeParams.forEach { param ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = param.label,
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = IfxBlack
                        )
                        if (param.presets.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                param.presets.forEach { preset ->
                                    val chosen = param.value.equals(preset, ignoreCase = true)
                                    Surface(
                                        onClick = { onUpdateParam(param.key, preset) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (chosen) topicAccent else Color(0xFFF4F6F8),
                                        border = BorderStroke(
                                            width = if (chosen) 1.5.dp else 1.dp,
                                            color = if (chosen) IfxBlack else Color(0xFFD0D5DD)
                                        )
                                    ) {
                                        Text(
                                            text = preset,
                                            fontFamily = Inter,
                                            fontWeight = if (chosen) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            fontSize = 12.5.sp,
                                            color = IfxBlack,
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                        OutlinedTextField(
                            value = param.value,
                            onValueChange = { onUpdateParam(param.key, it) },
                            label = { Text("Custom ${param.label.lowercase()}", fontFamily = Inter) },
                            singleLine = true,
                            shape = RoundedCornerShape(13.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Full User Spark mode: edit node statement + When / Where inputs
                OutlinedTextField(
                    value = editableText,
                    onValueChange = {
                        editableText = it
                        onUpdateNodeText(it)
                    },
                    label = { Text(if (node.isTrigger) "If..." else "Then do...", fontFamily = Inter) },
                    singleLine = true,
                    shape = RoundedCornerShape(13.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                if (node.isTrigger) {
                    val whenPresets = listOf("Anytime", "08:00 AM", "02:00 PM", "Weekdays", "Every Sunday")
                    Text(
                        text = "When",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = IfxBlack
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        whenPresets.forEach { preset ->
                            val chosen = editableWhen.equals(preset, ignoreCase = true)
                            Surface(
                                onClick = {
                                    editableWhen = preset
                                    onUpdateWhen(preset)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (chosen) IfxLime else Color(0xFFF4F6F8),
                                border = BorderStroke(if (chosen) 1.5.dp else 1.dp, if (chosen) IfxBlack else Color(0xFFD0D5DD))
                            ) {
                                Text(
                                    text = preset,
                                    fontFamily = Inter,
                                    fontWeight = if (chosen) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = IfxBlack,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = editableWhen,
                        onValueChange = {
                            editableWhen = it
                            onUpdateWhen(it)
                        },
                        label = { Text("Custom when", fontFamily = Inter) },
                        singleLine = true,
                        shape = RoundedCornerShape(13.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val wherePresets = listOf("Calendar", "Notebook", "Notification", "Gmail", "Gym")
                    Text(
                        text = "Where",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = IfxBlack
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        wherePresets.forEach { preset ->
                            val chosen = editableWhere.equals(preset, ignoreCase = true)
                            Surface(
                                onClick = {
                                    editableWhere = preset
                                    onUpdateWhere(preset)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (chosen) IfxLime else Color(0xFFF4F6F8),
                                border = BorderStroke(if (chosen) 1.5.dp else 1.dp, if (chosen) IfxBlack else Color(0xFFD0D5DD))
                            ) {
                                Text(
                                    text = preset,
                                    fontFamily = Inter,
                                    fontWeight = if (chosen) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = IfxBlack,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = editableWhere,
                        onValueChange = {
                            editableWhere = it
                            onUpdateWhere(it)
                        },
                        label = { Text("Custom where", fontFamily = Inter) },
                        singleLine = true,
                        shape = RoundedCornerShape(13.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (canDeleteNode) {
                    OutlinedButton(
                        onClick = onDeleteNode,
                        shape = CircleShape,
                        border = BorderStroke(1.2.dp, Color(0xFFD92D20)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD92D20)),
                        contentPadding = PaddingValues(vertical = 13.dp),
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Text("Remove", fontFamily = Inter, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IfxBlack,
                        contentColor = IfxLime
                    ),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    modifier = Modifier.weight(if (canDeleteNode) 0.6f else 1f)
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
