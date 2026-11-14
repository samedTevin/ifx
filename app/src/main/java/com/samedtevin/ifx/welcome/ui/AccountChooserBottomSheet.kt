package com.samedtevin.ifx.welcome.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.samedtevin.ifx.welcome.SavedAccountInfo

/**
 * Account Chooser bottom sheet for Google or Microsoft login:
 * - Lists existing accounts on device
 * - Allows picking an account, extracting personal information (name, email, initials, provider)
 * - Allows simulating auth error to demonstrate error handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountChooserBottomSheet(
    provider: String,
    onDismissRequest: () -> Unit,
    onAccountSelected: (SavedAccountInfo) -> Unit,
    onError: (String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val isGoogle = provider.equals("Google", ignoreCase = true)
    val accounts = if (isGoogle) {
        listOf(
            SavedAccountInfo(fullName = "Samed Tevin", email = "samed.tevin@gmail.com", provider = "Google", initials = "ST"),
            SavedAccountInfo(fullName = "Alex Morgan", email = "alex.morgan@gmail.com", provider = "Google", initials = "AM")
        )
    } else {
        listOf(
            SavedAccountInfo(fullName = "Samed Tevin", email = "samed.tevin@outlook.com", provider = "Microsoft", initials = "ST"),
            SavedAccountInfo(fullName = "Alex Morgan", email = "alex.morgan@live.com", provider = "Microsoft", initials = "AM")
        )
    }

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
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            // Header with provider icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = if (isGoogle) R.drawable.ic_auth_google else R.drawable.ic_auth_microsoft),
                            contentDescription = provider,
                            tint = IfxBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isGoogle) "Choose an account" else "Pick an account",
                            fontFamily = Inter,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = IfxBlack
                        )
                        Text(
                            text = "to continue to ifx",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

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

            // Account Items List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
            ) {
                accounts.forEachIndexed { index, account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAccountSelected(account)
                                onDismissRequest()
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(IfxLime)
                                .border(1.dp, IfxBlack, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = account.initials,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = IfxBlack
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.fullName,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = IfxBlack
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = account.email,
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    if (index < accounts.size - 1) {
                        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Error Simulation / Cancel Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        onError("Authentication canceled by user or connection interrupted.")
                        onDismissRequest()
                    }
                ) {
                    Text(
                        text = "Simulate error",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFFDC2626)
                    )
                }

                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = "Cancel",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}
