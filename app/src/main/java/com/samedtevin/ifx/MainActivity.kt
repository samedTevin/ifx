package com.samedtevin.ifx

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.samedtevin.ifx.auth.AuthResult
import com.samedtevin.ifx.auth.FirebaseAuthService
import com.samedtevin.ifx.home.ui.HomeScreen
import com.samedtevin.ifx.login.ui.ForgotPasswordScreen
import com.samedtevin.ifx.login.ui.LoginScreen
import com.samedtevin.ifx.register.ui.RegisterScreen
import com.samedtevin.ifx.onboarding.ui.OnboardingScreen
import com.samedtevin.ifx.splash.SplashScreen
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.Inter
import com.samedtevin.ifx.welcome.SavedAccountInfo
import com.samedtevin.ifx.welcome.WelcomeMode
import com.samedtevin.ifx.welcome.ui.AccountChooserBottomSheet
import com.samedtevin.ifx.welcome.ui.GuestBottomSheet
import com.samedtevin.ifx.welcome.ui.WelcomeScreen
import java.io.Serializable

/**
 * Top-level navigation destinations matching Photo 1.
 */
enum class AppDestination : Serializable {
    ONBOARDING,
    WELCOME,
    LOGIN,
    FORGOT_PASSWORD,
    REGISTER,
    HOME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Initialize AndroidX core-splashscreen BEFORE super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            IFXTheme {
                // 2. rememberSaveable persists the splash state across configuration changes (rotation)
                var showSplash by rememberSaveable { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Main app content
                    AppContent()

                    // Splash screen overlay
                    if (showSplash) {
                        SplashScreen(
                            onFinished = {
                                showSplash = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    val context = LocalContext.current
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.ONBOARDING) }

    val coroutineScope = rememberCoroutineScope()
    val currentFirebaseUser = remember { FirebaseAuthService.currentUser }

    // Dynamic saved account state backed by a custom Compose Saver for safe Bundle preservation
    var savedAccount by rememberSaveable(stateSaver = SavedAccountInfo.Saver) {
        mutableStateOf<SavedAccountInfo?>(
            currentFirebaseUser?.takeIf { it.isEmailVerified }?.let { user ->
                val name = user.displayName?.ifBlank { null }
                    ?: user.email?.substringBefore("@")?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    ?: "User"
                SavedAccountInfo(
                    fullName = name,
                    email = user.email ?: "",
                    provider = "Email",
                    initials = SavedAccountInfo.computeInitials(name)
                )
            }
        )
    }

    // Modal BottomSheet States
    var showGuestSheet by rememberSaveable { mutableStateOf(false) }
    var accountChooserProvider by rememberSaveable { mutableStateOf<String?>(null) }
    var authErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isGoogleLoading by rememberSaveable { mutableStateOf(false) }

    fun launchGoogleSignIn() {
        if (isGoogleLoading) return
        coroutineScope.launch {
            isGoogleLoading = true
            val result = FirebaseAuthService.signInWithGoogle(context)
            isGoogleLoading = false
            when (result) {
                is AuthResult.Success -> {
                    val user = result.data
                    val name = user.displayName?.ifBlank { null }
                        ?: user.email?.substringBefore("@")?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        ?: "Google User"
                    val email = user.email ?: ""
                    savedAccount = SavedAccountInfo(
                        fullName = name,
                        email = email,
                        provider = "Google",
                        initials = SavedAccountInfo.computeInitials(name)
                    )
                    Toast.makeText(context, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show()
                    currentDestination = AppDestination.HOME
                }
                is AuthResult.Error -> {
                    if (!result.message.contains("canceled", ignoreCase = true)) {
                        authErrorMessage = result.message
                    }
                }
                            }
        }
    }

    Crossfade(
        targetState = currentDestination,
        animationSpec = tween(350),
        label = "appNavigationCrossfade"
    ) { destination ->
        when (destination) {
            AppDestination.ONBOARDING -> {
                OnboardingScreen(
                    onFinished = {
                        currentDestination = AppDestination.WELCOME
                    }
                )
            }
            AppDestination.WELCOME -> {
                WelcomeScreen(
                    savedAccount = savedAccount,
                    initialMode = if (savedAccount != null) WelcomeMode.SAVED_ACCOUNT else WelcomeMode.NEW_DEVICE,
                    onEmailPasswordClick = {
                        currentDestination = AppDestination.REGISTER
                    },
                    onGoogleClick = {
                        launchGoogleSignIn()
                    },
                    onMicrosoftClick = {
                        accountChooserProvider = "Microsoft"
                    },
                    onGuestClick = {
                        showGuestSheet = true
                    },
                    onSignInClick = {
                        currentDestination = AppDestination.LOGIN
                    },
                    onContinueAsAccount = { account ->
                        if (account != null) {
                            coroutineScope.launch {
                                val isGoogle = account.provider.equals("Google", ignoreCase = true)
                                val isVerified = isGoogle || FirebaseAuthService.checkEmailVerified()
                                if (FirebaseAuthService.currentUser != null && !isVerified) {
                                    Toast.makeText(context, "Please verify your email before continuing.", Toast.LENGTH_LONG).show()
                                    currentDestination = AppDestination.LOGIN
                                } else {
                                    savedAccount = account
                                    Toast.makeText(context, "Welcome back, ${account.firstName}!", Toast.LENGTH_SHORT).show()
                                    currentDestination = AppDestination.HOME
                                }
                            }
                        } else {
                            showGuestSheet = true
                        }
                    }
                )
            }
            AppDestination.LOGIN -> {
                LoginScreen(
                    initialEmail = "",
                    initialPassword = "",
                    onBackClick = {
                        currentDestination = AppDestination.WELCOME
                    },
                    onSignInClick = { email, _ ->
                        val user = FirebaseAuthService.currentUser
                        val name = user?.displayName?.ifBlank { null }
                            ?: email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        savedAccount = SavedAccountInfo(
                            fullName = name,
                            email = email,
                            provider = "Email",
                            initials = SavedAccountInfo.computeInitials(name)
                        )
                        Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                        currentDestination = AppDestination.HOME
                    },
                    onForgotPasswordClick = {
                        currentDestination = AppDestination.FORGOT_PASSWORD
                    },
                    onGoogleClick = {
                        launchGoogleSignIn()
                    },
                    onMicrosoftClick = {
                        accountChooserProvider = "Microsoft"
                    },
                    onGuestClick = {
                        showGuestSheet = true
                    },
                    onCreateAccountClick = {
                        currentDestination = AppDestination.REGISTER
                    }
                )
            }
            AppDestination.FORGOT_PASSWORD -> {
                ForgotPasswordScreen(
                    initialEmail = "",
                    onBackClick = {
                        currentDestination = AppDestination.LOGIN
                    },
                    onSendResetLinkClick = { email ->
                        Toast.makeText(context, "Reset link sent to $email", Toast.LENGTH_SHORT).show()
                    },
                    onBackToSignInClick = {
                        currentDestination = AppDestination.LOGIN
                    }
                )
            }
            AppDestination.REGISTER -> {
                RegisterScreen(
                    onBackClick = {
                        currentDestination = AppDestination.WELCOME
                    },
                    onFinishRegister = { registeredData ->
                        val combinedName = "${registeredData.firstName} ${registeredData.lastName}".trim()
                        val validName = combinedName.ifEmpty { "User" }
                        val initialsStr = registeredData.initials

                        coroutineScope.launch {
                            FirebaseAuthService.updateUserProfile(validName)
                        }

                        savedAccount = SavedAccountInfo(
                            fullName = validName,
                            email = registeredData.email,
                            provider = "Email",
                            initials = initialsStr
                        )
                        Toast.makeText(context, "Registration complete! Welcome to ifx.", Toast.LENGTH_SHORT).show()
                        currentDestination = AppDestination.HOME
                    }
                )
            }
            AppDestination.HOME -> {
                HomeScreen(
                    account = savedAccount,
                    onLogoutClick = {
                        FirebaseAuthService.signOut()
                        savedAccount = null
                        currentDestination = AppDestination.WELCOME
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    },
                    onSwitchAccountClick = {
                        currentDestination = AppDestination.WELCOME
                    },
                    onSignInClick = {
                        currentDestination = AppDestination.LOGIN
                    }
                )
            }
        }
    }

    // Modal BottomSheet for Guest Mode (Photo 2)
    if (showGuestSheet) {
        GuestBottomSheet(
            onDismissRequest = { showGuestSheet = false },
            onContinueAsGuest = {
                FirebaseAuthService.signOut()
                savedAccount = null
                showGuestSheet = false
                currentDestination = AppDestination.HOME
                Toast.makeText(context, "Continuing as guest (Offline mode)", Toast.LENGTH_SHORT).show()
            },
            onSignInInstead = {
                showGuestSheet = false
                currentDestination = AppDestination.LOGIN
            }
        )
    }

    // Modal BottomSheet for Google / Microsoft Account Chooser (Photo 1)
    accountChooserProvider?.let { provider ->
        AccountChooserBottomSheet(
            provider = provider,
            onDismissRequest = { accountChooserProvider = null },
            onAccountSelected = { chosenAccount ->
                savedAccount = chosenAccount
                accountChooserProvider = null
                currentDestination = AppDestination.HOME
                Toast.makeText(context, "Signed in with $provider as ${chosenAccount.fullName}", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                accountChooserProvider = null
                authErrorMessage = error
            }
        )
    }

    // Authentication Error Dialog (If Google/Microsoft account chooser fails)
    authErrorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { authErrorMessage = null },
            title = {
                Text(
                    text = "Authentication Error",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = IfxBlack
                )
            },
            text = {
                Text(
                    text = message,
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                TextButton(onClick = { authErrorMessage = null }) {
                    Text(
                        text = "Dismiss",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        color = IfxBlack
                    )
                }
            },
            containerColor = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IFXTheme {
        AppContent()
    }
}
