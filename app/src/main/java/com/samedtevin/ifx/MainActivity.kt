package com.samedtevin.ifx

import android.content.Intent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.samedtevin.ifx.data.UserPreferencesDataStore
import com.samedtevin.ifx.home.ui.HomeScreen
import com.samedtevin.ifx.login.ui.ForgotPasswordScreen
import com.samedtevin.ifx.login.ui.LoginScreen
import com.samedtevin.ifx.register.ui.RegisterScreen
import com.samedtevin.ifx.onboarding.ui.OnboardingScreen
import com.samedtevin.ifx.splash.SplashScreen
import com.samedtevin.ifx.ui.components.IfxBackendProgressOverlay
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
    private var deepLinkLoginTrigger by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Initialize AndroidX core-splashscreen BEFORE super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (isLoginRedirectIntent(intent)) {
            deepLinkLoginTrigger++
        }

        setContent {
            IFXTheme {
                // 2. rememberSaveable persists the splash state across configuration changes (rotation)
                var showSplash by rememberSaveable { mutableStateOf(deepLinkLoginTrigger == 0) }

                LaunchedEffect(deepLinkLoginTrigger) {
                    if (deepLinkLoginTrigger > 0) {
                        showSplash = false
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Main app content
                    AppContent(deepLinkLoginTrigger = deepLinkLoginTrigger)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isLoginRedirectIntent(intent)) {
            deepLinkLoginTrigger++
        }
    }

    private fun isLoginRedirectIntent(intent: Intent?): Boolean {
        val data = intent?.data ?: return false
        return (data.scheme == "ifx" && data.host == "login") ||
            (data.host == "ifx-project-2c493.firebaseapp.com" && data.path?.startsWith("/login") == true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(deepLinkLoginTrigger: Int = 0) {
    val context = LocalContext.current
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.ONBOARDING) }
    var isSessionInitialized by rememberSaveable { mutableStateOf(false) }
    var lastResetEmail by rememberSaveable { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val currentFirebaseUser = remember { FirebaseAuthService.currentUser }

    val fallbackFirebaseAccount = remember(currentFirebaseUser) {
        currentFirebaseUser?.takeIf { user ->
            user.isEmailVerified || user.providerData.any { it.providerId == "google.com" }
        }?.let { user ->
            val isGoogle = user.providerData.any { it.providerId == "google.com" }
            val name = user.displayName?.ifBlank { null }
                ?: user.email?.substringBefore("@")?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                ?: "User"
            SavedAccountInfo(
                fullName = name,
                email = user.email ?: "",
                provider = if (isGoogle) "Google" else "Email",
                initials = SavedAccountInfo.computeInitials(name),
                photoUri = user.photoUrl?.toString()
            )
        }
    }

    // Dynamic saved account state backed by a custom Compose Saver for safe Bundle preservation
    var savedAccount by rememberSaveable(stateSaver = SavedAccountInfo.Saver) {
        mutableStateOf<SavedAccountInfo?>(fallbackFirebaseAccount)
    }

    // Load persisted Onboarding & Login state from DataStore on initial app launch
    LaunchedEffect(Unit) {
        if (!isSessionInitialized) {
            FirebaseAuthService.refreshAndCacheCurrentUserToken()?.let { (email, token) ->
                UserPreferencesDataStore.saveAuthToken(context, email, token)
            }
            val prefs = UserPreferencesDataStore.readPreferences(context)
            val resolvedAccount = prefs.savedAccount ?: fallbackFirebaseAccount

            when {
                deepLinkLoginTrigger > 0 -> {
                    savedAccount = resolvedAccount
                    currentDestination = AppDestination.LOGIN
                }
                prefs.isLoggedIn -> {
                    if (prefs.isGuestMode) {
                        savedAccount = null
                        currentDestination = AppDestination.HOME
                    } else if (resolvedAccount != null) {
                        val isEmailProvider = resolvedAccount.provider.equals("Email", ignoreCase = true)
                        if (isEmailProvider && FirebaseAuthService.currentUser == null) {
                            savedAccount = resolvedAccount
                            lastResetEmail = resolvedAccount.email
                            UserPreferencesDataStore.setLoggedOut(context, clearSavedAccount = false)
                            currentDestination = AppDestination.LOGIN
                        } else {
                            savedAccount = resolvedAccount
                            currentDestination = AppDestination.HOME
                        }
                    } else {
                        currentDestination = if (prefs.onboardingCompleted) AppDestination.WELCOME else AppDestination.ONBOARDING
                    }
                }
                prefs.onboardingCompleted -> {
                    savedAccount = prefs.savedAccount
                    currentDestination = AppDestination.WELCOME
                }
                fallbackFirebaseAccount != null -> {
                    savedAccount = fallbackFirebaseAccount
                    UserPreferencesDataStore.saveLoggedInAccount(context, fallbackFirebaseAccount)
                    currentDestination = AppDestination.HOME
                }
                else -> {
                    currentDestination = AppDestination.ONBOARDING
                }
            }
            isSessionInitialized = true
        }
    }

    LaunchedEffect(deepLinkLoginTrigger) {
        if (deepLinkLoginTrigger > 0) {
            currentDestination = AppDestination.LOGIN
        }
    }

    // Modal BottomSheet & Backend Loading States
    var showGuestSheet by rememberSaveable { mutableStateOf(false) }
    var accountChooserProvider by rememberSaveable { mutableStateOf<String?>(null) }
    var authErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isGoogleLoading by rememberSaveable { mutableStateOf(false) }
    var isVerifyingSavedAccount by rememberSaveable { mutableStateOf(false) }

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
                    val newAccount = SavedAccountInfo(
                        fullName = name,
                        email = email,
                        provider = "Google",
                        initials = SavedAccountInfo.computeInitials(name),
                        photoUri = user.photoUrl?.toString()
                    )
                    savedAccount = newAccount
                    UserPreferencesDataStore.saveLoggedInAccount(
                        context = context,
                        account = newAccount,
                        idToken = FirebaseAuthService.lastKnownIdToken
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

    if (!isSessionInitialized) {
        Box(modifier = Modifier.fillMaxSize())
        return
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
                        coroutineScope.launch {
                            UserPreferencesDataStore.setOnboardingCompleted(context, true)
                        }
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
                            if (isVerifyingSavedAccount) return@WelcomeScreen
                            coroutineScope.launch {
                                isVerifyingSavedAccount = true
                                val isGoogle = account.provider.equals("Google", ignoreCase = true)
                                val isMicrosoft = account.provider.equals("Microsoft", ignoreCase = true)
                                val isEmailProvider = account.provider.equals("Email", ignoreCase = true)
                                val isVerified = isGoogle || isMicrosoft || FirebaseAuthService.checkEmailVerified()
                                isVerifyingSavedAccount = false
                                if (isEmailProvider && FirebaseAuthService.currentUser == null) {
                                    lastResetEmail = account.email
                                    Toast.makeText(context, "Please sign in with your password to continue.", Toast.LENGTH_SHORT).show()
                                    currentDestination = AppDestination.LOGIN
                                } else if (FirebaseAuthService.currentUser != null && !isVerified) {
                                    Toast.makeText(context, "Please verify your email before continuing.", Toast.LENGTH_LONG).show()
                                    currentDestination = AppDestination.LOGIN
                                } else {
                                    savedAccount = account
                                    UserPreferencesDataStore.saveLoggedInAccount(context, account)
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
                    initialEmail = lastResetEmail,
                    initialPassword = "",
                    onBackClick = {
                        currentDestination = AppDestination.WELCOME
                    },
                    onSignInClick = { email, password ->
                        val user = FirebaseAuthService.currentUser
                        val name = user?.displayName?.ifBlank { null }
                            ?: email.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        val newAccount = SavedAccountInfo(
                            fullName = name,
                            email = email,
                            provider = "Email",
                            initials = SavedAccountInfo.computeInitials(name)
                        )
                        savedAccount = newAccount
                        coroutineScope.launch {
                            UserPreferencesDataStore.saveLoggedInAccount(
                                context = context,
                                account = newAccount,
                                idToken = FirebaseAuthService.lastKnownIdToken,
                                password = password
                            )
                        }
                        Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                        currentDestination = AppDestination.HOME
                    },
                    onForgotPasswordClick = { typedEmail ->
                        if (typedEmail.isNotBlank()) {
                            lastResetEmail = typedEmail
                        }
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
                    initialEmail = lastResetEmail,
                    onBackClick = {
                        currentDestination = AppDestination.LOGIN
                    },
                    onSendResetLinkClick = { email ->
                        lastResetEmail = email
                        Toast.makeText(context, "Reset link sent to $email", Toast.LENGTH_SHORT).show()
                    },
                    onBackToSignInClick = { resetEmail ->
                        if (resetEmail.isNotBlank()) {
                            lastResetEmail = resetEmail
                        }
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

                        val newAccount = SavedAccountInfo(
                            fullName = validName,
                            email = registeredData.email,
                            provider = "Email",
                            initials = initialsStr
                        )
                        savedAccount = newAccount

                        coroutineScope.launch {
                            FirebaseAuthService.updateUserProfile(validName)
                            FirebaseAuthService.refreshAndCacheCurrentUserToken()
                            UserPreferencesDataStore.saveLoggedInAccount(
                                context = context,
                                account = newAccount,
                                idToken = FirebaseAuthService.lastKnownIdToken,
                                password = FirebaseAuthService.lastKnownPassword
                            )
                        }

                        Toast.makeText(context, "Registration complete! Welcome to ifx.", Toast.LENGTH_SHORT).show()
                        currentDestination = AppDestination.HOME
                    }
                )
            }
            AppDestination.HOME -> {
                HomeScreen(
                    account = savedAccount,
                    onAccountUpdated = { updatedAccount ->
                        savedAccount = updatedAccount
                    },
                    onLogoutClick = {
                        // Keep Firebase token in background so Forgot Password polling can detect
                        // token revocation / passwordUpdatedAt changes, while clearing app session.
                        savedAccount = null
                        coroutineScope.launch {
                            UserPreferencesDataStore.setLoggedOut(context, clearSavedAccount = true)
                        }
                        currentDestination = AppDestination.WELCOME
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    },
                    onSwitchAccountClick = {
                        coroutineScope.launch {
                            UserPreferencesDataStore.setLoggedOut(context, clearSavedAccount = false)
                        }
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
                savedAccount = null
                showGuestSheet = false
                coroutineScope.launch {
                    UserPreferencesDataStore.saveGuestSession(context)
                }
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
                coroutineScope.launch {
                    UserPreferencesDataStore.saveLoggedInAccount(context, chosenAccount)
                }
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

    // Global Backend Progress Bar Overlay for Google Sign-In & Saved Session Verification
    IfxBackendProgressOverlay(
        visible = isGoogleLoading || isVerifyingSavedAccount,
        title = if (isGoogleLoading) "Signing in with Google..." else "Restoring session..."
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IFXTheme {
        AppContent()
    }
}
