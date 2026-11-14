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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.samedtevin.ifx.R
import kotlinx.coroutines.tasks.await

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : AuthResult<Nothing>()
}

object FirebaseAuthService {

    val auth: FirebaseAuth
        get() = Firebase.auth

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserSignedIn: Boolean
        get() = currentUser != null

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
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return AuthResult.Error("Registration succeeded, but user session is missing.")
            try { user.sendEmailVerification().await() } catch (_: Exception) {}
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: return AuthResult.Error("Sign in succeeded, but user profile could not be loaded.")
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(getReadableErrorMessage(e), e)
        }
    }

    suspend fun sendPasswordReset(email: String): AuthResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
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