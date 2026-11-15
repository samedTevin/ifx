package com.samedtevin.ifx.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.samedtevin.ifx.welcome.SavedAccountInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ifx_user_preferences"
)

data class UserSessionPreferences(
    val onboardingCompleted: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isGuestMode: Boolean = false,
    val savedAccount: SavedAccountInfo? = null,
    val cachedAuthEmail: String? = null,
    val cachedAuthIdToken: String? = null,
    val cachedAuthPassword: String? = null
)

/**
 * Jetpack DataStore manager persisting onboarding completion and active authentication state.
 * - Ensures Onboarding is only shown on first launch (until skipped or completed).
 * - Automatically routes to Welcome when Onboarding is done and user is logged out.
 * - Automatically routes to Home when the user has an active logged-in session.
 */
object UserPreferencesDataStore {

    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
    private val SAVED_FULL_NAME = stringPreferencesKey("saved_full_name")
    private val SAVED_EMAIL = stringPreferencesKey("saved_email")
    private val SAVED_PROVIDER = stringPreferencesKey("saved_provider")
    private val SAVED_INITIALS = stringPreferencesKey("saved_initials")
    private val CACHED_AUTH_EMAIL = stringPreferencesKey("cached_auth_email")
    private val CACHED_AUTH_ID_TOKEN = stringPreferencesKey("cached_auth_id_token")
    private val CACHED_AUTH_PASSWORD = stringPreferencesKey("cached_auth_password")

    fun observePreferences(context: Context): Flow<UserSessionPreferences> {
        return context.userPreferencesDataStore.data.map { prefs ->
            val fullName = prefs[SAVED_FULL_NAME]
            val email = prefs[SAVED_EMAIL]
            val provider = prefs[SAVED_PROVIDER]
            val initials = prefs[SAVED_INITIALS]

            val account = if (!fullName.isNullOrBlank()) {
                SavedAccountInfo(
                    fullName = fullName,
                    email = email ?: "",
                    provider = provider ?: "Email",
                    initials = initials ?: SavedAccountInfo.computeInitials(fullName)
                )
            } else {
                null
            }

            UserSessionPreferences(
                onboardingCompleted = prefs[ONBOARDING_COMPLETED] ?: false,
                isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
                isGuestMode = prefs[IS_GUEST_MODE] ?: false,
                savedAccount = account,
                cachedAuthEmail = prefs[CACHED_AUTH_EMAIL],
                cachedAuthIdToken = prefs[CACHED_AUTH_ID_TOKEN],
                cachedAuthPassword = prefs[CACHED_AUTH_PASSWORD]
            )
        }
    }

    suspend fun readPreferences(context: Context): UserSessionPreferences {
        return observePreferences(context).first()
    }

    suspend fun setOnboardingCompleted(context: Context, completed: Boolean = true) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveLoggedInAccount(
        context: Context,
        account: SavedAccountInfo,
        idToken: String? = null,
        password: String? = null
    ) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = true
            prefs[IS_LOGGED_IN] = true
            prefs[IS_GUEST_MODE] = false
            prefs[SAVED_FULL_NAME] = account.fullName
            prefs[SAVED_EMAIL] = account.email
            prefs[SAVED_PROVIDER] = account.provider
            prefs[SAVED_INITIALS] = account.initials
            if (account.email.isNotBlank()) {
                prefs[CACHED_AUTH_EMAIL] = account.email.trim()
            }
            if (!idToken.isNullOrBlank()) {
                prefs[CACHED_AUTH_ID_TOKEN] = idToken
            }
            if (!password.isNullOrBlank()) {
                prefs[CACHED_AUTH_PASSWORD] = password
            }
        }
    }

    suspend fun saveAuthToken(
        context: Context,
        email: String,
        idToken: String,
        password: String? = null
    ) {
        if (email.isBlank() || idToken.isBlank()) return
        context.userPreferencesDataStore.edit { prefs ->
            prefs[CACHED_AUTH_EMAIL] = email.trim()
            prefs[CACHED_AUTH_ID_TOKEN] = idToken
            if (!password.isNullOrBlank()) {
                prefs[CACHED_AUTH_PASSWORD] = password
            }
        }
    }

    suspend fun getCachedAuthToken(context: Context, email: String): String? {
        val prefs = readPreferences(context)
        val cachedEmail = prefs.cachedAuthEmail ?: return null
        return if (cachedEmail.equals(email.trim(), ignoreCase = true)) {
            prefs.cachedAuthIdToken
        } else {
            null
        }
    }

    suspend fun getCachedAuthPassword(context: Context, email: String): String? {
        val prefs = readPreferences(context)
        val cachedEmail = prefs.cachedAuthEmail ?: prefs.savedAccount?.email ?: return null
        return if (cachedEmail.equals(email.trim(), ignoreCase = true)) {
            prefs.cachedAuthPassword
        } else {
            null
        }
    }

    suspend fun saveGuestSession(context: Context) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = true
            prefs[IS_LOGGED_IN] = true
            prefs[IS_GUEST_MODE] = true
        }
    }

    suspend fun setLoggedOut(context: Context, clearSavedAccount: Boolean = false) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = false
            prefs[IS_GUEST_MODE] = false
            if (clearSavedAccount) {
                prefs.remove(SAVED_FULL_NAME)
                prefs.remove(SAVED_EMAIL)
                prefs.remove(SAVED_PROVIDER)
                prefs.remove(SAVED_INITIALS)
            }
        }
    }
}
