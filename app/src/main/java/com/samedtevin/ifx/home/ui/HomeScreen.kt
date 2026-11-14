package com.samedtevin.ifx.home.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.samedtevin.ifx.welcome.SavedAccountInfo
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    account: SavedAccountInfo? = null,
    onLogoutClick: () -> Unit = {},
    onSwitchAccountClick: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isGuest = account == null || account.provider.equals("Guest", ignoreCase = true)
    var showAccountSheet by rememberSaveable { mutableStateOf(false) }

    var routineEmail by rememberSaveable { mutableStateOf(true) }
    var routineCalendar by rememberSaveable { mutableStateOf(true) }
    var routineNotes by rememberSaveable { mutableStateOf(true) }
    var routineMorningBrief by rememberSaveable { mutableStateOf(true) }

    val activeCount = listOf(routineEmail, routineCalendar, routineNotes, routineMorningBrief).count { it }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val displayName = if (isGuest) "Guest" else (account.firstName)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IfxBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "i",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = IfxLime
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ifx",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = (-1).sp,
                    color = IfxBlack
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isGuest) Color(0xFFE5E7EB) else Color(0xFFE9F9D3))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isGuest) Color(0xFF6B7280) else Color(0xFF16A34A))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGuest) "Offline Guest" else "Online Sync",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isGuest) Color(0xFF4B5563) else Color(0xFF166534)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isGuest) Color(0xFFE5E7EB) else IfxLime)
                        .border(1.5.dp, IfxBlack, CircleShape)
                        .clickable { showAccountSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (isGuest) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_auth_guest),
                            contentDescription = "Guest avatar",
                            tint = IfxBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = account.initials,
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = IfxBlack
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$greeting, $displayName",
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.5).sp,
                color = IfxBlack
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isGuest) {
                    "Explore Sparks without cloud sync. Everything stays local."
                } else {
                    "Your personal automations are running in real time."
                },
                fontFamily = Inter,
                fontSize = 14.5.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isGuest) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFEF3C7))
                        .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Guest Session Active",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Routines and preferences are stored on this device only. Sign in anytime to sync across devices.",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFFB45309),
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onSignInClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IfxBlack,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                text = "Sign in or Create account",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(IfxLime)
                                .border(1.5.dp, IfxBlack, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = account?.initials ?: "AM",
                                fontFamily = Inter,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = IfxBlack
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = account.fullName,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = IfxBlack
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE5E7EB))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = account.provider,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = IfxBlack
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = account.email,
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        IconButton(onClick = { showAccountSheet = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chevron_right_gray),
                                contentDescription = "View details",
                                tint = IfxBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardStatCard(
                    title = "Active Sparks",
                    value = "$activeCount/4",
                    highlight = true,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    title = "Triggers Today",
                    value = "18",
                    highlight = false,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatCard(
                    title = "Cloud State",
                    value = if (isGuest) "Local" else "Synced",
                    highlight = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sparks & Routines",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = IfxBlack
                )

                TextButton(
                    onClick = {
                        Toast.makeText(context, "New Spark wizard ready soon!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "+ Add spark",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = IfxBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RoutineCard(
                    iconRes = R.drawable.ic_tile_mail,
                    title = "Email Deadlines",
                    subtitle = "Scans inbox and highlights due dates",
                    schedule = "Continuous background sync",
                    enabled = routineEmail,
                    onToggle = { routineEmail = it }
                )
                RoutineCard(
                    iconRes = R.drawable.ic_tile_cal,
                    title = "Calendar & Alarms",
                    subtitle = "Exact alarms 15 minutes before events",
                    schedule = "Next at 11:30 AM",
                    enabled = routineCalendar,
                    onToggle = { routineCalendar = it }
                )
                RoutineCard(
                    iconRes = R.drawable.ic_tile_notebook,
                    title = "Smart Notes & Summaries",
                    subtitle = "Automated on-device summary extraction",
                    schedule = "Triggers on new note",
                    enabled = routineNotes,
                    onToggle = { routineNotes = it }
                )
                RoutineCard(
                    iconRes = R.drawable.ic_tile_bell,
                    title = "Morning Brief",
                    subtitle = "Daily digest of schedule, weather, and tasks",
                    schedule = "Every day at 07:30 AM",
                    enabled = routineMorningBrief,
                    onToggle = { routineMorningBrief = it }
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Recent Activity",
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = IfxBlack
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ActivityRow(
                        time = "10m ago",
                        action = "Email deadline flagged",
                        detail = "Design System Review due today at 5:00 PM"
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    ActivityRow(
                        time = "42m ago",
                        action = "Calendar synced",
                        detail = "3 upcoming meetings updated in background"
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                    ActivityRow(
                        time = "2h ago",
                        action = "Morning brief delivered",
                        detail = "Weather (19°C), 4 priorities, and 2 exact alarms scheduled"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSwitchAccountClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = IfxBlack
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .border(1.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                ) {
                    Text(
                        text = "Switch account",
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
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .border(1.5.dp, Color(0xFFFEE2E2), RoundedCornerShape(18.dp))
                ) {
                    Text(
                        text = if (isGuest) "Exit guest" else "Log out",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showAccountSheet) {
        AccountDetailsBottomSheet(
            account = account,
            isGuest = isGuest,
            onDismissRequest = { showAccountSheet = false },
            onSwitchAccountClick = {
                showAccountSheet = false
                onSwitchAccountClick()
            },
            onLogoutClick = {
                showAccountSheet = false
                onLogoutClick()
            }
        )
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (highlight) IfxLime else Color.White)
            .border(1.dp, if (highlight) IfxBlack else Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontFamily = Inter,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (highlight) IfxBlack else Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontFamily = Inter,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = IfxBlack
            )
        }
    }
}

@Composable
private fun RoutineCard(
    iconRes: Int,
    title: String,
    subtitle: String,
    schedule: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) Color(0xFFF3F4F6) else Color(0xFFF9FAFB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = if (enabled) IfxBlack else Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (enabled) IfxBlack else Color(0xFF9CA3AF)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = Inter,
                    fontSize = 12.5.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = schedule,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    color = if (enabled) Color(0xFF16A34A) else Color(0xFF9CA3AF)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = IfxBlack,
                    checkedTrackColor = IfxLime,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE5E7EB),
                    uncheckedBorderColor = Color(0xFFD1D5DB)
                )
            )
        }
    }
}

@Composable
private fun ActivityRow(time: String, action: String, detail: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(IfxLime)
                .border(1.dp, IfxBlack, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = action,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = IfxBlack
                )
                Text(
                    text = time,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                fontFamily = Inter,
                fontSize = 12.5.sp,
                color = Color(0xFF6B7280),
                lineHeight = 17.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDetailsBottomSheet(
    account: SavedAccountInfo?,
    isGuest: Boolean,
    onDismissRequest: () -> Unit,
    onSwitchAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB))
            )
        }
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
                    text = "Account Details",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = IfxBlack
                )
                IconButton(
                    onClick = onDismissRequest,
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

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isGuest) Color(0xFFE5E7EB) else IfxLime)
                            .border(1.5.dp, IfxBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGuest) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_auth_guest),
                                contentDescription = "Guest",
                                tint = IfxBlack,
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Text(
                                text = account?.initials ?: "IF",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = IfxBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (isGuest) "Guest Session" else (account?.fullName ?: "User"),
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = IfxBlack
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isGuest) "Local on-device storage" else (account?.email ?: ""),
                            fontFamily = Inter,
                            fontSize = 13.5.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                InfoDetailRow(label = "Provider", value = if (isGuest) "Anonymous" else (account?.provider ?: "Email"))
                HorizontalDivider(color = Color(0xFFF3F4F6))
                InfoDetailRow(label = "Authentication", value = if (isGuest) "Offline Token" else "OAuth 2.0 / Firebase")
                HorizontalDivider(color = Color(0xFFF3F4F6))
                InfoDetailRow(label = "Data Encryption", value = "AES-256 On-Device")
                HorizontalDivider(color = Color(0xFFF3F4F6))
                InfoDetailRow(label = "Status", value = if (isGuest) "Guest Active" else "Authenticated")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSwitchAccountClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IfxBlack,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Switch or add account",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFDC2626), RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onLogoutClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isGuest) "Exit guest mode" else "Log out",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
private fun InfoDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 13.5.sp,
            color = Color(0xFF6B7280)
        )
        Text(
            text = value,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = IfxBlack
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    IFXTheme {
        HomeScreen(
            account = SavedAccountInfo(
                fullName = "Alex Morgan",
                email = "alex.morgan@gmail.com",
                provider = "Google",
                initials = "AM"
            )
        )
    }
}
