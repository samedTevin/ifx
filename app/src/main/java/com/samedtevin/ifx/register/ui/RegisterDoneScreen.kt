package com.samedtevin.ifx.register.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Registration Success / Done Scene ("You are all set, {FirstName}"):
 * Recomposes dynamically from user preferences and inputs stored in RegisterFormData.
 * Eliminates all static/hardcoded dummy placeholders.
 */
@Composable
fun RegisterDoneScreen(
    formData: RegisterFormData,
    onCreateSparkClick: () -> Unit,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayRoutines = formData.selectedRoutines.toList()
    val profileBitmap = rememberProfileBitmap(formData.photoUri)

    val greetingName = formData.firstNameClean.ifEmpty { "there" }
    val displayFullName = formData.fullName.ifEmpty { "Registered User" }
    val displayDetails = listOfNotNull(
        formData.email.ifBlank { null },
        formData.computedAge.ifBlank { null },
        formData.country.ifBlank { null }
    ).joinToString(" ? ")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.7f))

        // 1. Success Check Circle Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(IfxLime)
                .border(2.dp, IfxBlack, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_large_check),
                contentDescription = "Success",
                tint = IfxBlack,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. Dynamic Headline: "You are all set, {FirstName}"
        Text(
            text = "You are all set, $greetingName",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp,
            color = IfxBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Subtitle
        Text(
            text = "Your account is ready.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Dark Account Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(IfxBlack)
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar: User-uploaded photo or initials
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap,
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, IfxLime, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (formData.initials.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(IfxLime),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formData.initials,
                                fontFamily = Inter,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = IfxBlack
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF374151)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person_placeholder),
                                contentDescription = "Profile",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayFullName,
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayDetails.ifEmpty { "Account Active" },
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                if (displayRoutines.isNotEmpty() || formData.selectedOccupation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic chips row from user selections
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (formData.selectedOccupation.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IfxLime)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = formData.selectedOccupation,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = IfxBlack
                                )
                            }
                        }

                        displayRoutines.take(2).forEachIndexed { index, routine ->
                            val isOutline = (index == 0 && formData.selectedOccupation.isNotBlank()) || index == 1
                            if (isOutline) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = routine,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF262626))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = routine,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE5E7EB)
                                    )
                                }
                            }
                        }

                        if (displayRoutines.size > 2) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF262626))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "+${displayRoutines.size - 2} more",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1.2f))

        // 5. Primary Action: "Create my first Spark"
        Button(
            onClick = onCreateSparkClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = IfxLime,
                contentColor = IfxBlack
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Create my first Spark",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                color = IfxBlack
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Secondary Action: "Go to Home"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(1.5.dp, IfxBlack, RoundedCornerShape(28.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onGoHomeClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Go to Home",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                color = IfxBlack
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun RegisterDoneScreenPreview() {
    IFXTheme {
        RegisterDoneScreen(
            formData = RegisterFormData(
                firstName = "User",
                lastName = "Demo",
                email = "user@example.com",
                dateOfBirth = "14 / 03 / 2003",
                selectedRoutines = setOf("Email deadlines", "Calendar")
            ),
            onCreateSparkClick = {},
            onGoHomeClick = {}
        )
    }
}
