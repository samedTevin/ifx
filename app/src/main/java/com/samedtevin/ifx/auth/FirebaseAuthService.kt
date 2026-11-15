package com.samedtevin.ifx.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.samedtevin.ifx.R
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : AuthResult<Nothing>()
}

data class PasswordResetSnapshot(
    val email: String,
    val idToken: String,
    val passwordHash: String,
    val validSince: String,
    val passwordUpdatedAt: Long,
    val hadActiveUser: Boolean
) : Serializable

private sealed class AccountLookupResult {
    data class Active(
        val passwordHash: String,
        val validSince: String,
        val passwordUpdatedAt: Long
    ) : AccountLookupResult()

    data object TokenRevoked : AccountLookupResult()
    data object TransientFailure : AccountLookupResult()
}

object FirebaseAuthService {

    @Volatile
    var lastKnownEmail: String? = null
        private set

    @Volatile
    var lastKnownIdToken: String? = null
        private set

    @Volatile
    var lastKnownPassword: String? = null
        private set

    val auth: FirebaseAuth
        get() = Firebase.auth

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserSignedIn: Boolean
        get() = currentUser != null

    suspend fun refreshAndCacheCurrentUserToken(): Pair<String, String>? {
        return try {
            val user = currentUser ?: return null
            val email = user.email?.trim() ?: return null
            val token = try {
                user.getIdToken(false).await().token
            } catch (_: Exception) {
                user.getIdToken(true).await().token
            } ?: return null
            lastKnownEmail = email
            lastKnownIdToken = token
            email to token
        } catch (_: Exception) {
            null
        }
    }

    suspend fun signInWithGoogle(context: Context): AuthResult<FirebaseUser> {
        return try {
            val webClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (_: Exception) {
                "25869147313-mf5duccjgtkhn8vs895ontj7q25ri38p.apps.googleusercontent.com"
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val hostActivity = context.findActivity() ?: context

            val result = credentialManager.getCredential(
                request = request,
                context = hostActivity
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: return AuthResult.Error("Google sign-in succeeded, but Firebase user is missing.")

                val googleDisplayName = googleIdTokenCredential.displayName
                    ?: listOfNotNull(googleIdTokenCredential.givenName, googleIdTokenCredential.familyName)
                        .joinToString(" ").ifBlank { null }
                if (user.displayName.isNullOrBlank() && !googleDisplayName.isNullOrBlank()) {
                    try {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(googleDisplayName)
                            .build()
                        user.updateProfile(profileUpdates).await()
                    } catch (_: Exception) {}
                }

                refreshAndCacheCurrentUserToken()
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Unexpected credential type returned from Google.")
            }
        } catch (_: GetCredentialCancellationException) {
            AuthResult.Error("Google sign-in was canceled.")
        } catch (e: NoCredentialException) {
            AuthResult.Error("No Google account found on this device. Please sign in to a Google account in device Settings and try again.", e)
        } catch (e: GetCredentialException) {
            AuthResult.Error(e.localizedMessage ?: "Failed to retrieve Google credentials.", e)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

    suspend fun registerWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val cleanEmail = email.trim()
            val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user ?: return AuthResult.Error("Registration succeeded, but user session is missing.")
            lastKnownEmail = cleanEmail
            lastKnownPassword = password
            refreshAndCacheCurrentUserToken()
            try { user.sendEmailVerification().await() } catch (_: Exception) {}
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val cleanEmail = email.trim()
            val result = auth.signInWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user ?: return AuthResult.Error("Sign in succeeded, but user profile could not be loaded.")
            lastKnownEmail = cleanEmail
            lastKnownPassword = password
            refreshAndCacheCurrentUserToken()
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

    suspend fun sendPasswordReset(email: String): AuthResult<Unit> {
        val cleanEmail = email.trim()
        return try {
            try {
                // Do not call setAndroidPackageName here because projects without legacy
                // Firebase Dynamic Links enabled reject it with DYNAMIC_LINK_NOT_ACTIVATED.
                val actionCodeSettings = ActionCodeSettings.newBuilder()
                    .setUrl("https://ifx-project-2c493.firebaseapp.com/login")
                    .setHandleCodeInApp(false)
                    .build()
                auth.sendPasswordResetEmail(cleanEmail, actionCodeSettings).await()
            } catch (_: Exception) {
                auth.sendPasswordResetEmail(cleanEmail).await()
            }
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

    suspend fun resendEmailVerification(): AuthResult<Unit> {
        return try {
            val user = currentUser ?: return AuthResult.Error("No active user session to send verification to.")
            user.sendEmailVerification().await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

    suspend fun checkEmailVerified(): Boolean {
        return try {
            val user = currentUser ?: return false
            user.reload().await()
            user.isEmailVerified
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Captures the baseline account snapshot before sending the password reset email.
     * Mints or reuses an ID token for [email] (or silently re-authenticates if [fallbackPassword]
     * is available) and queries Identity Toolkit `accounts:lookup`.
     */
    suspend fun capturePasswordResetSnapshot(
        email: String,
        fallbackIdToken: String? = null,
        fallbackPassword: String? = null
    ): PasswordResetSnapshot? = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        var user = currentUser?.takeIf { it.email.equals(cleanEmail, ignoreCase = true) }

        val candidateTokens = mutableListOf<String>()

        if (user != null) {
            val freshOrCached = try {
                // Mint a fresh 1-hour JWT before sending the reset link
                user.getIdToken(true).await().token
            } catch (_: Exception) {
                try {
                    user.getIdToken(false).await().token
                } catch (_: Exception) {
                    null
                }
            }
            if (!freshOrCached.isNullOrBlank()) {
                lastKnownEmail = cleanEmail
                lastKnownIdToken = freshOrCached
                candidateTokens.add(freshOrCached)
            }
        }

        if (lastKnownEmail.equals(cleanEmail, ignoreCase = true) && !lastKnownIdToken.isNullOrBlank()) {
            candidateTokens.add(lastKnownIdToken!!)
        }

        if (!fallbackIdToken.isNullOrBlank()) {
            candidateTokens.add(fallbackIdToken)
        }

        for (token in candidateTokens.distinct()) {
            val lookup = lookupAccountByIdToken(token)
            if (lookup is AccountLookupResult.Active) {
                lastKnownEmail = cleanEmail
                lastKnownIdToken = token
                return@withContext PasswordResetSnapshot(
                    email = cleanEmail,
                    idToken = token,
                    passwordHash = lookup.passwordHash,
                    validSince = lookup.validSince,
                    passwordUpdatedAt = lookup.passwordUpdatedAt,
                    hadActiveUser = currentUser != null
                )
            }
        }

        // If no existing token is active (e.g. token expired or currentUser was signed out),
        // silently re-authenticate with the cached password to obtain a live baseline token.
        val passwordToTry = lastKnownPassword?.takeIf { lastKnownEmail.equals(cleanEmail, ignoreCase = true) }
            ?: fallbackPassword
        if (!passwordToTry.isNullOrBlank()) {
            try {
                val signInResult = auth.signInWithEmailAndPassword(cleanEmail, passwordToTry).await()
                user = signInResult.user
                val mintedToken = user?.getIdToken(true)?.await()?.token
                if (!mintedToken.isNullOrBlank()) {
                    lastKnownEmail = cleanEmail
                    lastKnownIdToken = mintedToken
                    val lookup = lookupAccountByIdToken(mintedToken)
                    if (lookup is AccountLookupResult.Active) {
                        return@withContext PasswordResetSnapshot(
                            email = cleanEmail,
                            idToken = mintedToken,
                            passwordHash = lookup.passwordHash,
                            validSince = lookup.validSince,
                            passwordUpdatedAt = lookup.passwordUpdatedAt,
                            hadActiveUser = true
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }

        return@withContext if (user != null) {
            PasswordResetSnapshot(
                email = cleanEmail,
                idToken = candidateTokens.firstOrNull() ?: "",
                passwordHash = "",
                validSince = "",
                passwordUpdatedAt = 0L,
                hadActiveUser = true
            )
        } else {
            null
        }
    }

    /**
     * Polls whether the password reset for [email] has been completed in the email link.
     * Detects completion via:
     * 1. Identity Toolkit `accounts:lookup` changes (`passwordHash`, `validSince`, or `passwordUpdatedAt`)
     *    or token revocation (`TOKEN_EXPIRED` / `INVALID_ID_TOKEN`) on the pre-reset baseline token.
     * 2. Active `FirebaseUser.reload()` throwing `TOKEN_EXPIRED` or clearing `currentUser`.
     *    NOTE: Do NOT call `getIdToken(true)` inside the polling loop, as forced token refresh
     *    mints new JWTs with updated `iat` timestamps that mask `validSince` revocation.
     */
    suspend fun checkPasswordResetCompleted(
        email: String,
        baseline: PasswordResetSnapshot?
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()

        // 1. Check via direct Identity Toolkit `accounts:lookup` using the pre-reset ID token
        val tokenToCheck = baseline?.idToken?.takeIf { it.isNotBlank() }
            ?: lastKnownIdToken?.takeIf { lastKnownEmail.equals(cleanEmail, ignoreCase = true) }

        if (!tokenToCheck.isNullOrBlank()) {
            when (val currentLookup = lookupAccountByIdToken(tokenToCheck)) {
                is AccountLookupResult.Active -> {
                    if (baseline != null) {
                        if (baseline.passwordHash.isNotBlank() &&
                            currentLookup.passwordHash.isNotBlank() &&
                            currentLookup.passwordHash != baseline.passwordHash
                        ) {
                            return@withContext true
                        }
                        if (baseline.validSince.isNotBlank() &&
                            currentLookup.validSince.isNotBlank() &&
                            currentLookup.validSince != baseline.validSince
                        ) {
                            return@withContext true
                        }
                        if (baseline.passwordUpdatedAt > 0L &&
                            currentLookup.passwordUpdatedAt > baseline.passwordUpdatedAt
                        ) {
                            return@withContext true
                        }
                    }
                }
                is AccountLookupResult.TokenRevoked -> {
                    // The token was active when the reset email was sent and is now revoked (TOKEN_EXPIRED)
                    if (baseline != null) {
                        return@withContext true
                    }
                }
                is AccountLookupResult.TransientFailure -> {}
            }
        }

        // 2. Check via FirebaseAuth currentUser reload (without forcing a new token mint)
        val user = currentUser
        if (user == null) {
            if (baseline?.hadActiveUser == true) {
                return@withContext true
            }
            return@withContext false
        }

        if (!user.email.equals(cleanEmail, ignoreCase = true)) {
            return@withContext false
        }

        try {
            user.reload().await()
            if (currentUser == null) {
                return@withContext true
            }
            false
        } catch (_: FirebaseAuthInvalidUserException) {
            true
        } catch (e: FirebaseAuthException) {
            val code = e.errorCode.uppercase()
            val msg = (e.message ?: "").uppercase()
            code.contains("TOKEN_EXPIRED") ||
                code.contains("USER_TOKEN_EXPIRED") ||
                code.contains("INVALID_ID_TOKEN") ||
                code.contains("CREDENTIAL_TOO_OLD") ||
                msg.contains("TOKEN_EXPIRED") ||
                msg.contains("NO LONGER VALID")
        } catch (e: Exception) {
            val msg = (e.message ?: "").uppercase()
            if (currentUser == null ||
                msg.contains("TOKEN_EXPIRED") ||
                msg.contains("USER_TOKEN_EXPIRED") ||
                msg.contains("INVALID_ID_TOKEN") ||
                msg.contains("CREDENTIAL_TOO_OLD") ||
                msg.contains("NO LONGER VALID")
            ) {
                true
            } else {
                false
            }
        }
    }

    private fun lookupAccountByIdToken(idToken: String): AccountLookupResult {
        return try {
            val apiKey = auth.app.options.apiKey ?: return AccountLookupResult.TransientFailure
            val url = URL("https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=$apiKey")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }
            val payload = JSONObject().put("idToken", idToken).toString()
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            if (code == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val userObj = json.optJSONArray("users")?.optJSONObject(0)
                    ?: return AccountLookupResult.TransientFailure
                val passwordHash = userObj.optString("passwordHash", "")
                val validSince = userObj.optString("validSince", "")
                val passwordUpdatedAt = userObj.optDouble("passwordUpdatedAt", 0.0).toLong()
                AccountLookupResult.Active(
                    passwordHash = passwordHash,
                    validSince = validSince,
                    passwordUpdatedAt = passwordUpdatedAt
                )
            } else if (code in 400..499) {
                val errorBody = conn.errorStream?.bufferedReader()?.use { it.readText() }?.uppercase() ?: ""
                if (errorBody.contains("TOKEN_EXPIRED") ||
                    errorBody.contains("USER_TOKEN_EXPIRED") ||
                    errorBody.contains("INVALID_ID_TOKEN") ||
                    errorBody.contains("CREDENTIAL_TOO_OLD") ||
                    errorBody.contains("USER_DISABLED") ||
                    errorBody.contains("USER_NOT_FOUND")
                ) {
                    AccountLookupResult.TokenRevoked
                } else {
                    AccountLookupResult.TransientFailure
                }
            } else {
                AccountLookupResult.TransientFailure
            }
        } catch (_: Exception) {
            AccountLookupResult.TransientFailure
        }
    }

    suspend fun updateUserProfile(displayName: String): AuthResult<Unit> {
        return try {
            val user = currentUser ?: return AuthResult.Error("No active user session found.")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.trim())
                .build()
            user.updateProfile(profileUpdates).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getReadableErrorMessage(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidUserException ->
                "No account found with this email address. Please check your email or create an account."
            is FirebaseAuthInvalidCredentialsException ->
                "Invalid credentials. Please verify your email and password."
            is FirebaseAuthUserCollisionException ->
                "An account with this email address already exists. Please sign in instead."
            is FirebaseAuthWeakPasswordException ->
                e.reason ?: "The password is too weak. Please use at least 8 characters with letters and numbers."
            is com.google.firebase.FirebaseNetworkException ->
                "Network error. Please check your internet connection."
            is com.google.firebase.FirebaseTooManyRequestsException ->
                "Too many attempts. Please wait a moment before trying again."
            else -> e.localizedMessage ?: "An unexpected authentication error occurred."
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
