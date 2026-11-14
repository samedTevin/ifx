package com.samedtevin.ifx.register.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.samedtevin.ifx.auth.FirebaseAuthService
import com.samedtevin.ifx.auth.AuthResult
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Verification Scene matching Image 1:
 * - Appears after Step 1 is submitted.
 * - Displays dynamic email verification notice.
 * - Instruction steps 1, 2, 3 with pulsing status indicator.
 * - "I have verified" (advances to Step 2) and "Open email app".
 * - "Change email" link (returns to Step 1 to edit email).
 */
@Composable
fun EmailVerificationScreen(
    email: String,
    onBackClick: () -> Unit = {},
    onVerifiedClick: () -> Unit = {},
    onChangeEmailClick: () -> Unit = {},
    onResendEmailClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val displayEmail = email.ifBlank { "alex@gmail.com" }

    var isChecking by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    var isVerifiedSuccess by remember { mutableStateOf(false) }

    // Active 45s countdown timer for resending verification email
    var countdownSeconds by rememberSaveable { mutableStateOf(45) }
    var timerTrigger by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(timerTrigger) {
        countdownSeconds = 45
        while (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    // Automatic polling every 3 seconds to detect when user taps verification link
    LaunchedEffect(Unit) {
        while (!isVerifiedSuccess) {
            delay(3000L)
            if (FirebaseAuthService.checkEmailVerified()) {
                isVerifiedSuccess = true
                Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                delay(400L)
                onVerifiedClick()
                break
            }
        }
    }

    fun verifyNow() {
        if (isChecking || isVerifiedSuccess) return
        isChecking = true
        verificationError = null
        coroutineScope.launch {
            val verified = FirebaseAuthService.checkEmailVerified()
            isChecking = false
            if (verified) {
                isVerifiedSuccess = true
                Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                delay(300L)
                onVerifiedClick()
            } else {
                verificationError = "We haven't detected your verification yet. Please tap the link sent to $displayEmail, then try again."
                Toast.makeText(context, "Email not verified yet. Please check your inbox.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun resendVerification() {
        if (isResending) return
        isResending = true
        verificationError = null
        coroutineScope.launch {
            when (val res = FirebaseAuthService.resendEmailVerification()) {
                is AuthResult.Success -> {
                    isResending = false
                    timerTrigger += 1
                    Toast.makeText(context, "Verification email resent to $displayEmail! Check spam/promotions if needed.", Toast.LENGTH_LONG).show()
                    onResendEmailClick()
                }
                is AuthResult.Error -> {
                    isResending = false
                    verificationError = res.message
                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Pulsing animation for the waiting dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // 1. Top Bar with Back action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint = IfxBlack,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Step 1 of 5 Progress Bar
        RegisterProgressBar(
            currentStep = 0,
            categoryName = "ACCOUNT"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // 3. Electric Lime Circular Mail Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(IfxLime),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_auth_mail),
                    contentDescription = "Mail",
                    tint = IfxBlack,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Headline
            Text(
                text = "Check your inbox",
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                letterSpacing = (-0.5).sp,
                color = IfxBlack
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Dynamic Subtitle with user's email
            val subtitleText = buildAnnotatedString {
                append("We sent a verification link to ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = IfxBlack)) {
                    append(displayEmail)
                }
                append(".")
            }

            Text(
                text = subtitleText,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Instruction Card (Steps 1, 2, 3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    InstructionStepRow(
                        number = "1",
                        title = "Open the email from ifx",
                        subtitle = "Not there? Check spam or promotions."
                    )
                    InstructionStepRow(
                        number = "2",
                        title = "Tap Verify email",
                        subtitle = "The link opens in your browser."
                    )
                    InstructionStepRow(
                        number = "3",
                        title = "Come back here",
                        subtitle = "ifx notices it automatically."
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. Waiting for verification pulse indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alpha(dotAlpha)
                        .clip(CircleShape)
                        .background(IfxLime)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Waiting for verification...",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Verification error banner
        if (verificationError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Error",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = verificationError ?: "",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFB91C1C)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 7. Action Buttons
        Button(
            onClick = { verifyNow() },
            enabled = !isChecking,
            colors = ButtonDefaults.buttonColors(
                containerColor = IfxLime,
                contentColor = IfxBlack,
                disabledContainerColor = IfxLime.copy(alpha = 0.6f),
                disabledContentColor = IfxBlack.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            if (isChecking) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = IfxBlack,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Checking verification...",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = IfxBlack
                    )
                }
            } else {
                Text(
                    text = "I have verified",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp,
                    color = IfxBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_EMAIL)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(emailIntent)
                        } catch (_: Exception) {
                            // Fallback if no specific email app handler exists
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Open email app",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                color = IfxBlack
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 8. Footer Link Row: "Resend email in 0:42" | "Change email"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (countdownSeconds > 0) {
                val secStr = if (countdownSeconds < 10) "0$countdownSeconds" else "$countdownSeconds"
                Text(
                    text = "Resend email in 0:$secStr",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280)
                )
            } else {
                Text(
                    text = "Resend email",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    textDecoration = TextDecoration.Underline,
                    color = IfxBlack,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { resendVerification() }
                    )
                )
            }

            Text(
                text = "Change email",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                textDecoration = TextDecoration.Underline,
                color = IfxBlack,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onChangeEmailClick
                )
            )
        }
    }
}

@Composable
private fun InstructionStepRow(
    number: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(IfxBlack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = IfxBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun EmailVerificationScreenPreview() {
    IFXTheme {
        EmailVerificationScreen(email = "alex@gmail.com")
    }
}
