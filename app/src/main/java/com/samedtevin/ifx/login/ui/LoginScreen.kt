package com.samedtevin.ifx.login.ui

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.samedtevin.ifx.auth.FirebaseAuthService
import com.samedtevin.ifx.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter

/**
 * Modern IFX Login Screen with Regex Validation:
 * - Empty by default, user enters credentials
 * - Email regex validation: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$
 * - Password required validation
 * - Working eye show/hide password toggle
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    initialEmail: String = "",
    initialPassword: String = "",
    onBackClick: () -> Unit = {},
    onSignInClick: (String, String) -> Unit = { _, _ -> },
    onForgotPasswordClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onMicrosoftClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var password by rememberSaveable { mutableStateOf(initialPassword) }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var generalError by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var showUnverifiedDialog by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val emailRegex = remember { Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$") }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun attemptSignIn() {
        if (isLoading) return
        val cleanEmail = email.trim()
        val emailValid = cleanEmail.isNotBlank() && emailRegex.matches(cleanEmail)
        val passValid = password.isNotBlank()

        emailError = when {
            cleanEmail.isBlank() -> "Email address is required."
            !emailValid -> "Please enter a valid email address."
            else -> null
        }

        passwordError = if (!passValid) "Password cannot be empty." else null

        if (emailValid && passValid) {
            keyboardController?.hide()
            isLoading = true
            generalError = null
            coroutineScope.launch {
                when (val result = FirebaseAuthService.loginWithEmail(cleanEmail, password)) {
                    is AuthResult.Success -> {
                        val isVerified = FirebaseAuthService.checkEmailVerified()
                        isLoading = false
                        if (!isVerified) {
                            showUnverifiedDialog = true
                        } else {
                            onSignInClick(cleanEmail, password)
                        }
                    }
                    is AuthResult.Error -> {
                        isLoading = false
                        when (result.exception) {
                            is FirebaseAuthInvalidUserException -> {
                                emailError = result.message
                            }
                            is FirebaseAuthInvalidCredentialsException -> {
                                passwordError = result.message
                            }
                            else -> {
                                generalError = result.message
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUnverifiedDialog) {
        AlertDialog(
            onDismissRequest = { showUnverifiedDialog = false },
            title = {
                Text(
                    text = "Email Not Verified",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = IfxBlack
                )
            },
            text = {
                Text(
                    text = "Your account (${email.trim()}) has not been verified yet. Please check your inbox (and spam folder) and click the verification link before logging in.",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            when (val res = FirebaseAuthService.resendEmailVerification()) {
                                is AuthResult.Success -> {
                                    Toast.makeText(context, "Verification email resent! Check your inbox.", Toast.LENGTH_LONG).show()
                                }
                                is AuthResult.Error -> {
                                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IfxLime,
                        contentColor = IfxBlack
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Resend Email",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val verified = FirebaseAuthService.checkEmailVerified()
                            if (verified) {
                                showUnverifiedDialog = false
                                Toast.makeText(context, "Email verified successfully!", Toast.LENGTH_SHORT).show()
                                onSignInClick(email.trim(), password)
                            } else {
                                Toast.makeText(context, "Still not verified. Please click the link in your email.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text(
                        text = "I've Verified",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = IfxBlack
                    )
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // 1. Top Navigation Bar: Back Arrow
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

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Headline & Subtitle
        Text(
            text = "Welcome back",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 34.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp,
            color = IfxBlack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to pick up where your Sparks left off.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Email Outlined Text Field with Regex Validation
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
                imeAction = ImeAction.Next
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

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Password Outlined Text Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) passwordError = null
            },
            label = {
                Text(
                    text = "Password",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            },
            placeholder = {
                Text(
                    text = "Enter your password",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF)
                )
            },
            isError = passwordError != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPasswordVisible) KeyboardType.Text else KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { attemptSignIn() }
            ),
            trailingIcon = {
                IconButton(
                    onClick = { isPasswordVisible = !isPasswordVisible }
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        ),
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF5F6368),
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
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
        if (passwordError != null) {
            Text(
                text = passwordError ?: "",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = Color(0xFFDC2626),
                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. "Forgot password?" Link
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Forgot password?",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline,
                color = IfxBlack,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 20.dp),
                    onClick = onForgotPasswordClick
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (generalError != null) {
            Text(
                text = generalError ?: "",
                fontFamily = Inter,
                fontSize = 13.sp,
                color = Color(0xFFDC2626),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // 6. Primary "Sign in" Action Button (Integrated with Firebase Auth)
        Button(
            onClick = { attemptSignIn() },
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
                        text = "Signing in...",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = IfxBlack
                    )
                }
            } else {
                Text(
                    text = "Sign in",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp,
                    color = IfxBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 7. "or continue with" Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE5E7EB),
                thickness = 1.dp
            )
            Text(
                text = "or continue with",
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFE5E7EB),
                thickness = 1.dp
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 8. Social Auth Options Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SocialAuthButton(
                text = "Google",
                iconRes = R.drawable.ic_auth_google,
                onClick = onGoogleClick,
                modifier = Modifier.weight(1f)
            )
            SocialAuthButton(
                text = "Microsoft",
                iconRes = R.drawable.ic_auth_microsoft,
                onClick = onMicrosoftClick,
                modifier = Modifier.weight(1f)
            )
            SocialAuthButton(
                text = "Guest",
                iconRes = R.drawable.ic_auth_guest,
                onClick = onGuestClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 9. Footer Link: "New to ifx? Create account"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New to ifx? ",
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Create account",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline,
                color = IfxBlack,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 20.dp),
                    onClick = onCreateAccountClick
                )
            )
        }
    }
}

/**
 * Outlined social authentication pill button with icon and title.
 */
@Composable
fun SocialAuthButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF9FAFB))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = IfxBlack,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = IfxBlack
            )
        }
    }
}

@Preview(name = "Login Screen - Default", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoginScreenPreview() {
    IFXTheme {
        LoginScreen()
    }
}
