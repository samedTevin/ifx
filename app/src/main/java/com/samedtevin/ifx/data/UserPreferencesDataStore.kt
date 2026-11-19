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
    val cachedAuthPassword: String? = null,
    val appTourSkipped: Boolean = false,
    val appTourCompleted: Boolean = false
) {
    val hasSeenAppTour: Boolean
        get() = appTourSkipped || appTourCompleted
}

/**
 * Jetpack DataStore manager persisting onboarding completion and active authentication state.
 * - Ensures Onboarding is only shown on first launch (until skipped or completed).
 * - Automatically routes to Welcome when Onboarding is done and user is logged out.
 * - Automatically routes to Home when the user has an active logged-in session.
 * - Persists Guided App Tour skipped/completed flags so newcomers are only prompted once.
 */
object UserPreferencesDataStore {

    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
    private val SAVED_FULL_NAME = stringPreferencesKey("saved_full_name")
    private val SAVED_EMAIL = stringPreferencesKey("saved_email")
    private val SAVED_PROVIDER = stringPreferencesKey("saved_provider")
    private val SAVED_INITIALS = stringPreferencesKey("saved_initials")
    private val SAVED_PHOTO_URI = stringPreferencesKey("saved_photo_uri")
    private val CACHED_AUTH_EMAIL = stringPreferencesKey("cached_auth_email")
    private val CACHED_AUTH_ID_TOKEN = stringPreferencesKey("cached_auth_id_token")
    private val CACHED_AUTH_PASSWORD = stringPreferencesKey("cached_auth_password")
    private val APP_TOUR_SKIPPED = booleanPreferencesKey("app_tour_skipped")
    private val APP_TOUR_COMPLETED = booleanPreferencesKey("app_tour_completed")

    fun observePreferences(context: Context): Flow<UserSessionPreferences> {
        return context.userPreferencesDataStore.data.map { prefs ->
            val fullName = prefs[SAVED_FULL_NAME]
            val email = prefs[SAVED_EMAIL]
            val provider = prefs[SAVED_PROVIDER]
            val initials = prefs[SAVED_INITIALS]
            val photoUri = prefs[SAVED_PHOTO_URI]?.ifBlank { null }

            val account = if (!fullName.isNullOrBlank()) {
                SavedAccountInfo(
                    fullName = fullName,
                    email = email ?: "",
                    provider = provider ?: "Email",
                    initials = initials ?: SavedAccountInfo.computeInitials(fullName),
                    photoUri = photoUri
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
                cachedAuthPassword = prefs[CACHED_AUTH_PASSWORD],
                appTourSkipped = prefs[APP_TOUR_SKIPPED] ?: false,
                appTourCompleted = prefs[APP_TOUR_COMPLETED] ?: false
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

    suspend fun setAppTourSkipped(context: Context, skipped: Boolean = true) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[APP_TOUR_SKIPPED] = skipped
        }
    }

    suspend fun setAppTourCompleted(context: Context, completed: Boolean = true) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[APP_TOUR_COMPLETED] = completed
            if (completed) {
                prefs[APP_TOUR_SKIPPED] = false
            }
        }
    }

    suspend fun resetAppTour(context: Context) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[APP_TOUR_SKIPPED] = false
            prefs[APP_TOUR_COMPLETED] = false
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
            if (!account.photoUri.isNullOrBlank()) {
                prefs[SAVED_PHOTO_URI] = account.photoUri
            }
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

    suspend fun updateProfilePhoto(context: Context, photoUri: String?) {
        context.userPreferencesDataStore.edit { prefs ->
            if (photoUri.isNullOrBlank()) {
                prefs.remove(SAVED_PHOTO_URI)
            } else {
                prefs[SAVED_PHOTO_URI] = photoUri
            }
        }
    }

    suspend fun updateDisplayName(context: Context, fullName: String) {
        val clean = fullName.trim()
        if (clean.isBlank()) return
        context.userPreferencesDataStore.edit { prefs ->
            prefs[SAVED_FULL_NAME] = clean
            prefs[SAVED_INITIALS] = SavedAccountInfo.computeInitials(clean)
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
                prefs.remove(SAVED_PHOTO_URI)
            }
        }
    }
}
