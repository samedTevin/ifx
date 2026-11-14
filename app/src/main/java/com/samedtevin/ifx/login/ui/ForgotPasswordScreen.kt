package com.samedtevin.ifx.login.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.samedtevin.ifx.auth.FirebaseAuthService
import com.samedtevin.ifx.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Forgot Password Screen with email regex validation.
 */
@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    initialEmail: String = "",
    onBackClick: () -> Unit = {},
    onSendResetLinkClick: (String) -> Unit = {},
    onBackToSignInClick: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var statusMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isSuccess by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val emailRegex = remember { Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                when (val result = FirebaseAuthService.sendPasswordReset(cleanEmail)) {
                    is AuthResult.Success -> {
                        isLoading = false
                        isSuccess = true
                        statusMessage = "Password reset link sent! Check your inbox."
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

    Column(
        modifier = modifier
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
                color = if (isSuccess) Color(0xFF16A34A) else Color(0xFFDC2626),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // 5. "Send reset link" Action Button (Integrated with Firebase Auth)
        Button(
            onClick = { attemptSendReset() },
            enabled = !isLoading,
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
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = IfxBlack,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sending reset link...",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = IfxBlack
                    )
                }
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
                    onClick = onBackToSignInClick
                )
            )
        }
    }
}

@Preview(name = "Forgot Password Screen", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun ForgotPasswordScreenPreview() {
    IFXTheme {
        ForgotPasswordScreen()
    }
}
