package com.samedtevin.ifx.register.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel orchestrating reactive registration state and step-by-step validations.
 * Exposes uiState and stepErrors as StateFlows for real-time Jetpack Compose reactivity.
 */
class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterFormData())
    val uiState: StateFlow<RegisterFormData> = _uiState.asStateFlow()

    private val _stepErrors = MutableStateFlow(RegisterStepErrors())
    val stepErrors: StateFlow<RegisterStepErrors> = _stepErrors.asStateFlow()

    // Regular Expression definitions
    private val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")
    private val nameRegex = Regex("^[a-zA-Z?-?\\s'-]{2,30}$")
    private val phoneRegex = Regex("^\\+?[0-9\\s\\-()]{7,18}$")
    private val dobRegex = Regex("^(0[1-9]|[12][0-9]|3[01])\\s*[/.-]\\s*(0[1-9]|1[0-2])\\s*[/.-]\\s*(19[2-9][0-9]|20[0-2][0-9])$")

    fun updateFormData(transform: (RegisterFormData) -> RegisterFormData) {
        _uiState.update(transform)
    }

    fun setFormData(data: RegisterFormData) {
        _uiState.value = data
    }

    fun setEmailError(error: String?) {
        _stepErrors.update { it.copy(emailError = error) }
    }

    fun setPasswordError(error: String?) {
        _stepErrors.update { it.copy(passwordError = error) }
    }

    fun clearStep1Errors() {
        _stepErrors.update {
            it.copy(
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
                termsError = null
            )
        }
    }

    fun clearStep2Errors() {
        _stepErrors.update {
            it.copy(
                firstNameError = null,
                lastNameError = null,
                phoneError = null,
                dateOfBirthError = null,
                countryError = null
            )
        }
    }

    fun clearStep3Errors() {
        _stepErrors.update { it.copy(occupationError = null) }
    }

    fun clearStep4Errors() {
        _stepErrors.update { it.copy(routinesError = null) }
    }

    /**
     * Validates Step 1 (Account):
     * - Email (regex)
     * - Password (regex: min 8, letters and numbers)
     * - Confirm password (matches password)
     * - Terms accepted (must be true)
     */
    fun validateStep1(): Boolean {
        val state = _uiState.value

        val emailErr = when {
            state.email.isBlank() -> "Email address is required."
            !emailRegex.matches(state.email.trim()) -> "Enter a valid email address (e.g. name@domain.com)."
            else -> null
        }

        val passErr = when {
            state.password.isBlank() -> "Password is required."
            !passwordRegex.matches(state.password) -> "Password must be at least 8 characters with letters & numbers."
            else -> null
        }

        val confirmErr = when {
            state.confirmPassword.isBlank() -> "Please confirm your password."
            state.confirmPassword != state.password -> "Passwords do not match."
            else -> null
        }

        val termsErr = if (!state.termsAccepted) {
            "You must agree to the Terms and Privacy Policy."
        } else null

        _stepErrors.update {
            it.copy(
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmErr,
                termsError = termsErr
            )
        }

        return emailErr == null && passErr == null && confirmErr == null && termsErr == null
    }

    /**
     * Validates Step 2 (About You):
     * - First name (regex: min 2 letters)
     * - Last name (regex: min 2 letters)
     * - Phone (regex: min 7 digits)
     * - Date of birth (regex + age >= 13)
     * - Country (must be chosen)
     */
    fun validateStep2(): Boolean {
        val state = _uiState.value

        val firstErr = when {
            state.firstName.isBlank() -> "First name is required."
            !nameRegex.matches(state.firstName.trim()) -> "First name must have at least 2 letters."
            else -> null
        }

        val lastErr = when {
            state.lastName.isBlank() -> "Last name is required."
            !nameRegex.matches(state.lastName.trim()) -> "Last name must have at least 2 letters."
            else -> null
        }

        val digitsCount = state.phone.filter { it.isDigit() }.length
        val phoneErr = when {
            state.phone.isBlank() -> "Phone number is required."
            digitsCount < 7 || digitsCount > 15 -> "Enter a valid phone number (7-15 digits)."
            else -> null
        }

        val dobErr = when {
            state.dateOfBirth.isBlank() -> "Date of birth is required."
            !dobRegex.matches(state.dateOfBirth.trim()) -> "Use DD / MM / YYYY format."
            else -> {
                val parts = state.dateOfBirth.split('/', '-', '.').map { it.trim() }
                val year = parts.lastOrNull()?.toIntOrNull()
                if (year == null || year > 2013) {
                    "You must be at least 13 years old to use ifx."
                } else null
            }
        }

        val countryErr = if (state.country.isBlank()) {
            "Please select your country or region."
        } else null

        _stepErrors.update {
            it.copy(
                firstNameError = firstErr,
                lastNameError = lastErr,
                phoneError = phoneErr,
                dateOfBirthError = dobErr,
                countryError = countryErr
            )
        }

        return firstErr == null && lastErr == null && phoneErr == null && dobErr == null && countryErr == null
    }

    /**
     * Validates Step 3 (Occupation):
     * - Occupation must be selected
     */
    fun validateStep3(): Boolean {
        val state = _uiState.value
        val occErr = if (state.selectedOccupation.isBlank()) {
            "Please select your occupation."
        } else null

        _stepErrors.update { it.copy(occupationError = occErr) }
        return occErr == null
    }

    /**
     * Validates Step 4 (Routines):
     * - Must have at least one routine chosen
     */
    fun validateStep4(): Boolean {
        val state = _uiState.value
        val routinesErr = if (state.selectedRoutines.isEmpty()) {
            "Please select at least one routine to automate."
        } else null

        _stepErrors.update { it.copy(routinesError = routinesErr) }
        return routinesErr == null
    }
}
