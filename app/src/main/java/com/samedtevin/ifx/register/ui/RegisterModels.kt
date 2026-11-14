package com.samedtevin.ifx.register.ui

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import java.io.Serializable

/**
 * Enumeration representing each step in the 5-step registration funnel.
 */
enum class RegisterStep(val index: Int, val stepNumber: Int, val categoryName: String) : Serializable {
    ACCOUNT(0, 1, "ACCOUNT"),
    ABOUT_YOU(1, 2, "ABOUT YOU"),
    OCCUPATION(2, 3, "OCCUPATION"),
    ROUTINES(3, 4, "ROUTINES"),
    PERMISSIONS(4, 5, "PERMISSIONS");

    companion object {
        fun fromIndex(index: Int): RegisterStep =
            entries.getOrNull(index) ?: ACCOUNT
    }
}

/**
 * Validation errors across all steps of the registration funnel.
 */
data class RegisterStepErrors(
    // Step 1: Account
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,

    // Step 2: About You
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val phoneError: String? = null,
    val dateOfBirthError: String? = null,
    val countryError: String? = null,

    // Step 3: Occupation
    val occupationError: String? = null,

    // Step 4: Routines
    val routinesError: String? = null
) : Serializable {
    val hasStep1Errors: Boolean
        get() = emailError != null || passwordError != null || confirmPasswordError != null || termsError != null

    val hasStep2Errors: Boolean
        get() = firstNameError != null || lastNameError != null || phoneError != null || dateOfBirthError != null || countryError != null

    val hasStep3Errors: Boolean
        get() = occupationError != null

    val hasStep4Errors: Boolean
        get() = routinesError != null
}

/**
 * State holding all user inputs across the 5 registration steps.
 * Implements Serializable and provides a dedicated Compose Saver to guarantee
 * safe preservation in Android Bundles and rememberSaveable.
 */
data class RegisterFormData(
    // Step 1: Account
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,

    // Step 2: About You
    val photoUri: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val phoneCountryCode: String = "+1",
    val phoneCountryIso: String = "US",
    val phone: String = "",
    val dateOfBirth: String = "",
    val gender: String = "Prefer not to say",
    val country: String = "",

    // Step 3: Occupation
    val selectedOccupation: String = "",
    val schoolOrCompany: String = "",

    // Step 4: Routines
    val selectedRoutines: Set<String> = emptySet(),
    val automationExperience: String = "Some",

    // Step 5: Permissions
    val notificationsAllowed: Boolean = true,
    val exactAlarmsGranted: Boolean = false,
    val googleConnected: Boolean = false
) : Serializable {

    val fullName: String
        get() = "$firstName $lastName".trim()

    val firstNameClean: String
        get() = firstName.trim()

    val fullPhone: String
        get() = if (phone.isNotBlank()) "$phoneCountryCode ${phone.trim()}" else ""

    val initials: String
        get() {
            val f = firstName.trim().take(1).uppercase()
            val l = lastName.trim().take(1).uppercase()
            return when {
                f.isNotEmpty() && l.isNotEmpty() -> "$f$l"
                f.isNotEmpty() -> f
                l.isNotEmpty() -> l
                else -> ""
            }
        }

    val computedAge: String
        get() {
            val parts = dateOfBirth.split('/', '-', '.').map { it.trim() }
            val year = parts.lastOrNull()?.toIntOrNull()
            return if (year != null && year in 1900..2026) {
                "Age ${2026 - year}"
            } else {
                ""
            }
        }

    companion object {
        private const val serialVersionUID: Long = 2L

        val Saver: Saver<RegisterFormData, Any> = listSaver(
            save = { data ->
                listOf(
                    data.email,
                    data.password,
                    data.confirmPassword,
                    data.termsAccepted,
                    data.photoUri,
                    data.firstName,
                    data.lastName,
                    data.phone,
                    data.dateOfBirth,
                    data.gender,
                    data.country,
                    data.selectedOccupation,
                    data.schoolOrCompany,
                    ArrayList(data.selectedRoutines),
                    data.automationExperience,
                    data.notificationsAllowed,
                    data.exactAlarmsGranted,
                    data.googleConnected,
                    data.phoneCountryCode,
                    data.phoneCountryIso
                )
            },
            restore = { list ->
                @Suppress("UNCHECKED_CAST")
                val routinesList = (list.getOrNull(13) as? List<String>) ?: emptyList()
                RegisterFormData(
                    email = (list.getOrNull(0) as? String) ?: "",
                    password = (list.getOrNull(1) as? String) ?: "",
                    confirmPassword = (list.getOrNull(2) as? String) ?: "",
                    termsAccepted = (list.getOrNull(3) as? Boolean) ?: false,
                    photoUri = list.getOrNull(4) as? String,
                    firstName = (list.getOrNull(5) as? String) ?: "",
                    lastName = (list.getOrNull(6) as? String) ?: "",
                    phone = (list.getOrNull(7) as? String) ?: "",
                    dateOfBirth = (list.getOrNull(8) as? String) ?: "",
                    gender = (list.getOrNull(9) as? String) ?: "Prefer not to say",
                    country = (list.getOrNull(10) as? String) ?: "",
                    selectedOccupation = (list.getOrNull(11) as? String) ?: "",
                    schoolOrCompany = (list.getOrNull(12) as? String) ?: "",
                    selectedRoutines = routinesList.toSet(),
                    automationExperience = (list.getOrNull(14) as? String) ?: "Some",
                    notificationsAllowed = (list.getOrNull(15) as? Boolean) ?: true,
                    exactAlarmsGranted = (list.getOrNull(16) as? Boolean) ?: false,
                    googleConnected = (list.getOrNull(17) as? Boolean) ?: false,
                    phoneCountryCode = (list.getOrNull(18) as? String) ?: "+1",
                    phoneCountryIso = (list.getOrNull(19) as? String) ?: "US"
                )
            }
        )
    }
}
