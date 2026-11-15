package com.samedtevin.ifx.login.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.samedtevin.ifx.R
import com.samedtevin.ifx.auth.AuthResult
import com.samedtevin.ifx.auth.FirebaseAuthService
import com.samedtevin.ifx.auth.PasswordResetSnapshot
import com.samedtevin.ifx.data.UserPreferencesDataStore
import com.samedtevin.ifx.ui.components.IfxButtonProgressContent
import com.samedtevin.ifx.ui.components.IfxWaitingIndicator
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Forgot Password Flow:
 * 1. Form State: User enters email address and taps "Send reset link".
 * 2. Check Your Inbox State: Displays the confirmation screen with the user's email,
 *    "Open email app" button, 45s resend timer, and "Back to sign in" link.
 * 3. Automatic Navigation to Login: Polls Firebase in the background (same approach as
 *    EmailVerificationScreen) and observes lifecycle return so as soon as the user submits
 *    their new password in the reset email, it navigates directly to the Login section.
 */
@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    initialEmail: String = "",
    onBackClick: () -> Unit = {},
    onSendResetLinkClick: (String) -> Unit = {},
    onBackToSignInClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = remember(context) { context.findLifecycleOwner() }

    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var statusMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSuccess by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var hasLeftAppForReset by rememberSaveable { mutableStateOf(false) }
    var baselineSnapshot by rememberSaveable { mutableStateOf<PasswordResetSnapshot?>(null) }
    var isResetDetected by rememberSaveable { mutableStateOf(false) }

    var countdownSeconds by rememberSaveable { mutableIntStateOf(45) }
    var timerTrigger by rememberSaveable { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val emailRegex = remember { Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Countdown timer active when on "Check your inbox" screen
    LaunchedEffect(isSuccess, timerTrigger) {
        if (isSuccess) {
            countdownSeconds = 45
            while (countdownSeconds > 0) {
                delay(1000L)
                countdownSeconds--
            }
        }
    }

    // Automatic background polling (every 2 seconds, same approach as EmailVerificationScreen)
    // to detect when the user submits their new password via the email reset link.
    LaunchedEffect(isSuccess, baselineSnapshot) {
        if (isSuccess) {
            isResetDetected = false
            while (isSuccess && !isResetDetected) {
                delay(2000L)
                if (FirebaseAuthService.checkPasswordResetCompleted(email, baselineSnapshot)) {
                    isResetDetected = true
                    Toast.makeText(
                        context,
                        "Password updated! Please sign in with your new password.",
                        Toast.LENGTH_SHORT
                    ).show()
                    delay(300L)
                    onBackToSignInClick(email.trim())
                    break
                }
            }
        }
    }

    val currentEmail by rememberUpdatedState(email)
    val currentOnBackToSignInClick by rememberUpdatedState(onBackToSignInClick)

    // Also detect when the user leaves the app to open Gmail/browser on this device
    // and returns after resetting their password.
    DisposableEffect(lifecycleOwner, isSuccess) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (isSuccess) {
                        hasLeftAppForReset = true
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isSuccess && hasLeftAppForReset && !isResetDetected) {
                        hasLeftAppForReset = false
                        isResetDetected = true
                        Toast.makeText(
                            context,
                            "Sign in with your new password.",
                            Toast.LENGTH_SHORT
                        ).show()
                        currentOnBackToSignInClick(currentEmail.trim())
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    fun attemptSendReset() {
        if (isLoading) return
        val cleanEmail = email.trim()
        val isValid = cleanEmail.isNotBlank() && emailRegex.matches(cleanEmail)

        emailError = when {
            cleanEmail.isBlank() -> "Email address is required."
            !isValid -> "Please enter a valid email address."
            else -> null
        }

        if (isValid) {
            keyboardController?.hide()
            isLoading = true
            statusMessage = null
            coroutineScope.launch {
                val cachedToken = UserPreferencesDataStore.getCachedAuthToken(context, cleanEmail)
                val cachedPassword = UserPreferencesDataStore.getCachedAuthPassword(context, cleanEmail)
                val snapshot = FirebaseAuthService.capturePasswordResetSnapshot(
                    email = cleanEmail,
                    fallbackIdToken = cachedToken,
                    fallbackPassword = cachedPassword
                )
                if (snapshot != null && snapshot.idToken.isNotBlank()) {
                    UserPreferencesDataStore.saveAuthToken(context, cleanEmail, snapshot.idToken)
                }
                baselineSnapshot = snapshot

                when (val result = FirebaseAuthService.sendPasswordReset(cleanEmail)) {
                    is AuthResult.Success -> {
                        isLoading = false
                        hasLeftAppForReset = false
                        isResetDetected = false
                        isSuccess = true
                        timerTrigger++
                        onSendResetLinkClick(cleanEmail)
                    }
                    is AuthResult.Error -> {
                        isLoading = false
                        isSuccess = false
                        if (result.exception is FirebaseAuthInvalidUserException) {
                            emailError = "No account found with this email address."
                        } else {
                            statusMessage = result.message
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = isSuccess,
            animationSpec = tween(durationMillis = 300),
            label = "forgotPasswordStateCrossfade"
        ) { emailSent ->
            if (emailSent) {
                ForgotPasswordCheckInboxContent(
                    email = email.trim().ifBlank { "alex@gmail.com" },
                    countdownSeconds = countdownSeconds,
                    isResending = isLoading,
                    onOpenEmailAppClick = {
                        openEmailClient(context)
                    },
                    onResendClick = {
                        attemptSendReset()
                    },
                    onBackToSignInClick = {
                        onBackToSignInClick(email.trim())
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp)
                ) {
                    // 1. Top Back Navigation Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 12.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Electric Lime Shield Badge
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(IfxLime),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield_outline),
                            contentDescription = "Security Shield",
                            tint = IfxBlack,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Headline & Subtitle
                    Text(
                        text = "Forgot password?",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        lineHeight = 40.sp,
                        letterSpacing = (-0.5).sp,
                        color = IfxBlack
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter your email and we will send a reset link.",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 4. Email Outlined Text Field with Regex Validation
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) emailError = null
                            if (statusMessage != null) statusMessage = null
                        },
                        label = {
                            Text(
                                text = "Email",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        },
                        placeholder = {
                            Text(
                                text = "name@example.com",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        isError = emailError != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { attemptSendReset() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IfxBlack,
                            unfocusedBorderColor = Color(0xFF9CA3AF),
                            errorBorderColor = Color(0xFFDC2626),
                            focusedLabelColor = IfxBlack,
                            unfocusedLabelColor = Color(0xFF6B7280),
                            focusedTextColor = IfxBlack,
                            unfocusedTextColor = IfxBlack,
                            cursorColor = IfxBlack
                        )
                    )
                    if (emailError != null) {
                        Text(
                            text = emailError ?: "",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (statusMessage != null) {
                        Text(
                            text = statusMessage ?: "",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // 5. "Send reset link" Action Button (Single inline progress bar when loading)
                    Button(
                        onClick = { attemptSendReset() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IfxLime,
                            contentColor = IfxBlack,
                            disabledContainerColor = IfxLime.copy(alpha = 0.85f),
                            disabledContentColor = IfxBlack
                        ),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        if (isLoading) {
                            IfxButtonProgressContent(text = "Sending reset link...")
                        } else {
                            Text(
                                text = "Send reset link",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp,
                                color = IfxBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 6. Information Card: "Used Google or Microsoft?"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF7F8FA))
                            .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {
                        Column {
                            Text(
                                text = "Used Google or Microsoft?",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = IfxBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reset your password with that provider.\nGuests have no password to reset.",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 7. Footer: "Back to sign in"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Back to sign in",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            textDecoration = TextDecoration.Underline,
                            color = IfxBlack,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 24.dp),
                                onClick = { onBackToSignInClick(email.trim()) }
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * "Check your inbox" screen shown after the password reset email is dispatched.
 */
@Composable
fun ForgotPasswordCheckInboxContent(
    email: String,
    countdownSeconds: Int,
    isResending: Boolean = false,
    onOpenEmailAppClick: () -> Unit = {},
    onResendClick: () -> Unit = {},
    onBackToSignInClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // 1. Lime Circle Mail Badge with Bottom-Right Black Checkmark Badge
        Box(
            modifier = Modifier.size(136.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(IfxLime),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_auth_mail),
                    contentDescription = "Mail sent",
                    tint = IfxBlack,
                    modifier = Modifier.size(46.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(IfxBlack)
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Checkmark",
                    tint = IfxLime,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. Headline: "Check your inbox"
        Text(
            text = "Check your inbox",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 33.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp,
            color = IfxBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Subtitle: "We sent a reset link to\n{email}. It expires in 30 minutes."
        val subtitleText = buildAnnotatedString {
            append("We sent a reset link to\n")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = IfxBlack)) {
                append(email)
            }
            append(". It expires in 30 minutes.")
        }

        Text(
            text = subtitleText,
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.5.sp,
            lineHeight = 23.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Single clean progress indicator (no duplicate pulsing dot)
        IfxWaitingIndicator(
            text = "Waiting for password reset...",
            alignment = Alignment.CenterHorizontally
        )

        Spacer(modifier = Modifier.weight(1.25f))

        // 4. Primary Button: Black Pill with Lime Text ("Open email app")
        Button(
            onClick = onOpenEmailAppClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = IfxBlack,
                contentColor = IfxLime
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Open email app",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp,
                color = IfxLime
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 5. Resend Timer ("Resend in 0:42" or clickable "Resend reset link")
        if (countdownSeconds > 0) {
            val secStr = if (countdownSeconds < 10) "0$countdownSeconds" else "$countdownSeconds"
            Text(
                text = "Resend in 0:$secStr",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = if (isResending) "Resending..." else "Resend reset link",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                textDecoration = TextDecoration.Underline,
                color = IfxBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable(
                    enabled = !isResending,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 24.dp),
                    onClick = onResendClick
                )
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 6. Footer Link: "Back to sign in"
        Text(
            text = "Back to sign in",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 14.5.sp,
            textDecoration = TextDecoration.Underline,
            color = IfxBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 24.dp),
                    onClick = onBackToSignInClick
                )
        )
    }
}

private fun openEmailClient(context: Context) {
    val emailAppIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_EMAIL)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(emailAppIntent)
        return
    } catch (_: Exception) {
    }

    val gmailIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
    if (gmailIntent != null) {
        try {
            gmailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(gmailIntent)
            return
        } catch (_: Exception) {
        }
    }

    try {
        val mailtoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(mailtoIntent)
    } catch (_: Exception) {
        Toast.makeText(context, "Please open your email app to view the reset link.", Toast.LENGTH_SHORT).show()
    }
}

private tailrec fun Context.findLifecycleOwner(): LifecycleOwner? = when (this) {
    is LifecycleOwner -> this
    is ContextWrapper -> baseContext.findLifecycleOwner()
    else -> null
}

@Preview(name = "Forgot Password - Form", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ForgotPasswordScreenPreview() {
    IFXTheme {
        ForgotPasswordScreen()
    }
}

@Preview(name = "Forgot Password - Check Your Inbox", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ForgotPasswordCheckInboxPreview() {
    IFXTheme {
        ForgotPasswordCheckInboxContent(
            email = "alex@gmail.com",
            countdownSeconds = 42
        )
    }
}
