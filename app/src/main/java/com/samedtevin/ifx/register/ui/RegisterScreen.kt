package com.samedtevin.ifx.register.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import android.widget.Toast
import com.samedtevin.ifx.auth.FirebaseAuthService
import com.samedtevin.ifx.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IFXTheme
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter
import kotlinx.coroutines.launch

/**
 * Main Registration Screen orchestrating:
 * 1. 5-Step Horizontal Pager (Account, About You, Occupation, Routines, Permissions)
 * 2. Intermediate Email Verification Scene ("Check your inbox") after Step 1
 * 3. Final Done Scene ("You are all set, {FirstName}") after Step 5
 *
 * Steps CANNOT be skipped; all fields are validated via regex before navigating.
 */
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = remember { RegisterViewModel() },
    onBackClick: () -> Unit = {},
    onFinishRegister: (RegisterFormData) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })

    val formData by viewModel.uiState.collectAsState()
    val errors by viewModel.stepErrors.collectAsState()

    var isVerifyingEmail by rememberSaveable { mutableStateOf(false) }
    var isRegistrationDone by rememberSaveable { mutableStateOf(false) }
    var isCreatingAccount by rememberSaveable { mutableStateOf(false) }
    var step1AuthError by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    when {
        isRegistrationDone -> {
            RegisterDoneScreen(
                formData = formData,
                onCreateSparkClick = { onFinishRegister(formData) },
                onGoHomeClick = { onFinishRegister(formData) }
            )
        }
        isVerifyingEmail -> {
            EmailVerificationScreen(
                email = formData.email,
                onBackClick = { isVerifyingEmail = false },
                onChangeEmailClick = { isVerifyingEmail = false },
                onResendEmailClick = {
                    coroutineScope.launch {
                        FirebaseAuthService.resendEmailVerification()
                    }
                },
                onVerifiedClick = {
                    isVerifyingEmail = false
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            )
        }
        else -> {
            val currentStep = RegisterStep.fromIndex(pagerState.currentPage)

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
            ) {
                RegisterTopBar(
                    onBackClick = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        } else {
                            onBackClick()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                RegisterProgressBar(
                    currentStep = pagerState.currentPage,
                    categoryName = currentStep.categoryName
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> AccountStep(
                            formData = formData,
                            errors = errors,
                            onUpdate = { viewModel.setFormData(it) }
                        )
                        1 -> AboutYouStep(
                            formData = formData,
                            errors = errors,
                            onUpdate = { viewModel.setFormData(it) }
                        )
                        2 -> OccupationStep(
                            formData = formData,
                            errors = errors,
                            onUpdate = { viewModel.setFormData(it) }
                        )
                        3 -> RoutinesStep(
                            formData = formData,
                            errors = errors,
                            onUpdate = { viewModel.setFormData(it) }
                        )
                        4 -> PermissionsStep(
                            formData = formData,
                            onUpdate = { viewModel.setFormData(it) }
                        )
                    }
                }

                if (step1AuthError != null && pagerState.currentPage == 0) {
                    Text(
                        text = step1AuthError ?: "",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (isCreatingAccount) return@Button
                        when (pagerState.currentPage) {
                            0 -> {
                                if (viewModel.validateStep1()) {
                                    viewModel.clearStep1Errors()
                                    isCreatingAccount = true
                                    step1AuthError = null
                                    coroutineScope.launch {
                                        when (val result = FirebaseAuthService.registerWithEmail(formData.email, formData.password)) {
                                            is AuthResult.Success -> {
                                                isCreatingAccount = false
                                                isVerifyingEmail = true
                                            }
                                            is AuthResult.Error -> {
                                                isCreatingAccount = false
                                                when (result.exception) {
                                                    is FirebaseAuthUserCollisionException -> {
                                                        viewModel.setEmailError("This email address is already registered. Please sign in instead.")
                                                    }
                                                    is FirebaseAuthWeakPasswordException -> {
                                                        viewModel.setPasswordError(result.message)
                                                    }
                                                    else -> {
                                                        step1AuthError = result.message
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                if (viewModel.validateStep2()) {
                                    viewModel.clearStep2Errors()
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(2)
                                    }
                                }
                            }
                            2 -> {
                                if (viewModel.validateStep3()) {
                                    viewModel.clearStep3Errors()
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(3)
                                    }
                                }
                            }
                            3 -> {
                                if (viewModel.validateStep4()) {
                                    viewModel.clearStep4Errors()
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(4)
                                    }
                                }
                            }
                            4 -> {
                                isRegistrationDone = true
                            }
                        }
                    },
                    enabled = !isCreatingAccount,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IfxLime,
                        contentColor = IfxBlack,
                        disabledContainerColor = IfxLime.copy(alpha = 0.6f),
                        disabledContentColor = IfxBlack.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, top = 8.dp)
                        .height(54.dp)
                ) {
                    if (isCreatingAccount) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = IfxBlack,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Creating account...",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = IfxBlack
                            )
                        }
                    } else {
                        Text(
                            text = if (pagerState.currentPage == 4) "Finish" else "Continue",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp,
                            color = IfxBlack
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// STEP 1: ACCOUNT
// =========================================================================
@Composable
private fun AccountStep(
    formData: RegisterFormData,
    errors: RegisterStepErrors,
    onUpdate: (RegisterFormData) -> Unit
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    if (showTermsDialog) {
        TermsAndPrivacyDialog(
            onDismissRequest = { showTermsDialog = false },
            onAccept = {
                onUpdate(formData.copy(termsAccepted = true))
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Create your account",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp,
            color = IfxBlack
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter your email and choose a strong password.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email Address Field with regex validation
        OutlinedTextField(
            value = formData.email,
            onValueChange = { onUpdate(formData.copy(email = it)) },
            label = { Text("Email address", fontFamily = Inter, fontSize = 14.sp) },
            placeholder = { Text("name@example.com", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
            isError = errors.emailError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IfxBlack,
                unfocusedBorderColor = Color(0xFFD1D5DB),
                errorBorderColor = Color(0xFFDC2626),
                focusedTextColor = IfxBlack,
                unfocusedTextColor = IfxBlack,
                cursorColor = IfxBlack
            )
        )
        FormErrorText(errors.emailError)

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field with regex validation
        OutlinedTextField(
            value = formData.password,
            onValueChange = { onUpdate(formData.copy(password = it)) },
            label = { Text("Password", fontFamily = Inter, fontSize = 14.sp) },
            placeholder = { Text("At least 8 characters", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
            isError = errors.passwordError != null,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPasswordVisible) KeyboardType.Text else KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
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
                unfocusedBorderColor = Color(0xFFD1D5DB),
                errorBorderColor = Color(0xFFDC2626),
                focusedTextColor = IfxBlack,
                unfocusedTextColor = IfxBlack,
                cursorColor = IfxBlack
            )
        )
        FormErrorText(errors.passwordError)

        Spacer(modifier = Modifier.height(10.dp))

        PasswordStrengthTracker(password = formData.password)

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = formData.confirmPassword,
            onValueChange = { onUpdate(formData.copy(confirmPassword = it)) },
            label = { Text("Confirm password", fontFamily = Inter, fontSize = 14.sp) },
            placeholder = { Text("Re-enter password", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
            isError = errors.confirmPasswordError != null,
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isConfirmPasswordVisible) KeyboardType.Text else KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (isConfirmPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        ),
                        contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF5F6368),
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IfxBlack,
                unfocusedBorderColor = Color(0xFFD1D5DB),
                errorBorderColor = Color(0xFFDC2626),
                focusedTextColor = IfxBlack,
                unfocusedTextColor = IfxBlack,
                cursorColor = IfxBlack
            )
        )
        FormErrorText(errors.confirmPasswordError)

        Spacer(modifier = Modifier.height(24.dp))

        // Terms and Privacy Policy agreement (Opens scrollable alert dialog)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (!formData.termsAccepted) {
                            showTermsDialog = true
                        } else {
                            onUpdate(formData.copy(termsAccepted = false))
                        }
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IfxCheckbox(
                checked = formData.termsAccepted,
                onCheckedChange = {
                    if (it) {
                        showTermsDialog = true
                    } else {
                        onUpdate(formData.copy(termsAccepted = false))
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            val termsText = buildAnnotatedString {
                append("I agree to the ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, color = IfxBlack)) {
                    append("Terms")
                }
                append(" and ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, color = IfxBlack)) {
                    append("Privacy Policy")
                }
            }

            Text(
                text = termsText,
                fontFamily = Inter,
                fontSize = 14.sp,
                color = IfxBlack,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showTermsDialog = true }
                )
            )
        }
        FormErrorText(errors.termsError)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// =========================================================================
// STEP 2: ABOUT YOU
// =========================================================================
@Composable
private fun AboutYouStep(
    formData: RegisterFormData,
    errors: RegisterStepErrors,
    onUpdate: (RegisterFormData) -> Unit
) {
    val context = LocalContext.current
    var genderDropdownExpanded by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPhoneCodeDialog by remember { mutableStateOf(false) }

    val currentDialItem = remember(formData.phoneCountryCode, formData.phoneCountryIso) {
        CountryDialCodes.list.firstOrNull { it.dialCode == formData.phoneCountryCode && it.iso == formData.phoneCountryIso }
            ?: CountryDialCodes.list.firstOrNull { it.dialCode == formData.phoneCountryCode }
            ?: CountryDialCodes.list.first()
    }

    val genderOptions = listOf("Prefer not to say", "Female", "Male", "Non-binary", "Other")
    val profileBitmap = rememberProfileBitmap(formData.photoUri)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val uriStr = ImageStorageUtils.saveBitmap(context, bitmap)
            if (uriStr != null) {
                onUpdate(formData.copy(photoUri = uriStr))
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val uriStr = ImageStorageUtils.copyUri(context, uri)
            if (uriStr != null) {
                onUpdate(formData.copy(photoUri = uriStr))
            }
        }
    }

    if (showCountryDialog) {
        CountryPickerDialog(
            onDismissRequest = { showCountryDialog = false },
            onCountrySelected = { selectedCountry ->
                val matchingDial = CountryDialCodes.findByCountryName(selectedCountry)
                if (matchingDial != null) {
                    onUpdate(formData.copy(
                        country = selectedCountry,
                        phoneCountryCode = matchingDial.dialCode,
                        phoneCountryIso = matchingDial.iso
                    ))
                } else {
                    onUpdate(formData.copy(country = selectedCountry))
                }
                showCountryDialog = false
            }
        )
    }

    if (showDatePicker) {
        @OptIn(ExperimentalMaterial3Api::class)
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = millis
                            }
                            val day = calendar.get(Calendar.DAY_OF_MONTH)
                            val month = calendar.get(Calendar.MONTH) + 1
                            val year = calendar.get(Calendar.YEAR)
                            val formatted = String.format(Locale.US, "%02d / %02d / %04d", day, month, year)
                            onUpdate(formData.copy(dateOfBirth = formatted))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", fontFamily = Inter, fontWeight = FontWeight.Bold, color = IfxBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontFamily = Inter, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            ),
            modifier = Modifier.border(1.5.dp, IfxLime, RoundedCornerShape(24.dp))
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    selectedDayContainerColor = IfxLime,
                    selectedDayContentColor = IfxBlack,
                    todayDateBorderColor = IfxBlack,
                    todayContentColor = IfxBlack
                )
            )
        }
    }

    if (showPhoneCodeDialog) {
        PhoneCodePickerDialog(
            onDismissRequest = { showPhoneCodeDialog = false },
            onCodeSelected = { dialItem ->
                onUpdate(formData.copy(
                    phoneCountryCode = dialItem.dialCode,
                    phoneCountryIso = dialItem.iso
                ))
            }
        )
    }

    if (showPhotoDialog) {
        PhotoSourceDialog(
            hasCurrentPhoto = formData.photoUri != null,
            onDismissRequest = { showPhotoDialog = false },
            onTakePhoto = { cameraLauncher.launch(null) },
            onChooseGallery = { galleryLauncher.launch("image/*") },
            onRemovePhoto = { onUpdate(formData.copy(photoUri = null)) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "About you",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp,
            color = IfxBlack
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "So ifx can greet you properly.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar Section: Replaces static 'AM' with silhouette placeholder or user-selected photo
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clickable { showPhotoDialog = true }
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                        .border(1.5.dp, IfxBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person_placeholder),
                            contentDescription = "Avatar placeholder",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(IfxLime)
                        .border(1.5.dp, IfxBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = IfxBlack
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.clickable { showPhotoDialog = true }) {
                Text(
                    text = if (formData.photoUri != null) "Change photo" else "Add a photo",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                    color = IfxBlack
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap to choose from gallery or camera.",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // First Name & Last Name (Start EMPTY with regex validation)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = formData.firstName,
                    onValueChange = { onUpdate(formData.copy(firstName = it)) },
                    label = { Text("First name", fontFamily = Inter, fontSize = 14.sp) },
                    placeholder = { Text("Enter first name", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                    isError = errors.firstNameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IfxBlack,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        errorBorderColor = Color(0xFFDC2626),
                        focusedTextColor = IfxBlack,
                        unfocusedTextColor = IfxBlack,
                        cursorColor = IfxBlack
                    )
                )
                FormErrorText(errors.firstNameError)
            }

            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = formData.lastName,
                    onValueChange = { onUpdate(formData.copy(lastName = it)) },
                    label = { Text("Last name", fontFamily = Inter, fontSize = 14.sp) },
                    placeholder = { Text("Enter last name", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                    isError = errors.lastNameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IfxBlack,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        errorBorderColor = Color(0xFFDC2626),
                        focusedTextColor = IfxBlack,
                        unfocusedTextColor = IfxBlack,
                        cursorColor = IfxBlack
                    )
                )
                FormErrorText(errors.lastNameError)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Number Field with Country Code Picker & Flag in ONE Equal Row
        val isPhoneValid = formData.phone.filter { it.isDigit() }.length in 7..15

        OutlinedTextField(
            value = formData.phone,
            onValueChange = { onUpdate(formData.copy(phone = it)) },
            label = { Text("Phone number", fontFamily = Inter, fontSize = 14.sp) },
            placeholder = { Text("555 123 4567", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
            isError = errors.phoneError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            leadingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showPhoneCodeDialog = true }
                        )
                        .padding(start = 12.dp, end = 6.dp)
                ) {
                    Text(
                        text = currentDialItem.flagEmoji,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentDialItem.dialCode,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = IfxBlack
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_down),
                        contentDescription = "Select country code",
                        tint = IfxBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(26.dp)
                            .background(Color(0xFFE5E7EB))
                    )
                }
            },
            trailingIcon = {
                if (isPhoneValid) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Valid phone number",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(20.dp)
                    )
                } else if (formData.phone.isNotBlank()) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_x_bullet),
                        contentDescription = "Invalid phone number",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IfxBlack,
                unfocusedBorderColor = Color(0xFFD1D5DB),
                errorBorderColor = Color(0xFFDC2626),
                focusedTextColor = IfxBlack,
                unfocusedTextColor = IfxBlack,
                cursorColor = IfxBlack
            )
        )
        FormErrorText(errors.phoneError)

        Spacer(modifier = Modifier.height(16.dp))

        // Date of birth Field (Allows selecting from calendar or entering manually)
        OutlinedTextField(
            value = formData.dateOfBirth,
            onValueChange = { onUpdate(formData.copy(dateOfBirth = it)) },
            label = { Text("Date of birth", fontFamily = Inter, fontSize = 14.sp) },
            placeholder = { Text("DD / MM / YYYY", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
            isError = errors.dateOfBirthError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (formData.computedAge.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE2FFA8))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = formData.computedAge,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = IfxBlack
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_outline),
                            contentDescription = "Pick date from calendar",
                            tint = IfxBlack,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IfxBlack,
                unfocusedBorderColor = Color(0xFFD1D5DB),
                errorBorderColor = Color(0xFFDC2626),
                focusedTextColor = IfxBlack,
                unfocusedTextColor = IfxBlack,
                cursorColor = IfxBlack
            )
        )
        FormErrorText(errors.dateOfBirthError)

        Spacer(modifier = Modifier.height(16.dp))

        // Gender Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formData.gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender (optional)", fontFamily = Inter, fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { genderDropdownExpanded = true },
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_down),
                        contentDescription = "Expand gender",
                        tint = IfxBlack,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { genderDropdownExpanded = true }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IfxBlack,
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedTextColor = IfxBlack,
                    unfocusedTextColor = IfxBlack
                )
            )

            DropdownMenu(
                expanded = genderDropdownExpanded,
                onDismissRequest = { genderDropdownExpanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .border(1.5.dp, IfxLime, RoundedCornerShape(16.dp))
            ) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, fontFamily = Inter, fontSize = 14.sp, color = IfxBlack) },
                        onClick = {
                            onUpdate(formData.copy(gender = option))
                            genderDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Country / Region Field (Opens searchable modal covering all countries worldwide)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCountryDialog = true }
        ) {
            OutlinedTextField(
                value = formData.country,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                isError = errors.countryError != null,
                label = { Text("Country / region", fontFamily = Inter, fontSize = 14.sp) },
                placeholder = { Text("Select your country or region", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_down),
                        contentDescription = "Select country",
                        tint = IfxBlack,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = IfxBlack,
                    disabledBorderColor = if (errors.countryError != null) Color(0xFFDC2626) else Color(0xFFD1D5DB),
                    disabledLabelColor = IfxBlack,
                    disabledPlaceholderColor = Color(0xFF9CA3AF)
                )
            )
        }
        FormErrorText(errors.countryError)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// =========================================================================
// STEP 3: OCCUPATION
// =========================================================================
@Composable
private fun OccupationStep(
    formData: RegisterFormData,
    errors: RegisterStepErrors,
    onUpdate: (RegisterFormData) -> Unit
) {
    var occupationDropdownExpanded by remember { mutableStateOf(false) }

    val occupationTagsRow1 = listOf("Student", "Developer", "Designer")
    val occupationTagsRow2 = listOf("Teacher", "Freelancer", "Manager")
    val occupationTagsRow3 = listOf("Healthcare", "Other")
    val allOccupations = occupationTagsRow1 + occupationTagsRow2 + occupationTagsRow3

    val suggestedProjectName = when (formData.selectedOccupation) {
        "Student" -> "Study"
        "Developer" -> "Code"
        "Designer" -> "Design"
        "Teacher" -> "Classroom"
        "Manager" -> "Projects"
        else -> "Productivity"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "What do you do?",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp,
            color = IfxBlack
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Gemini uses this to suggest Sparks and Projects.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Occupation Chips
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                occupationTagsRow1.forEach { tag ->
                    RegisterSelectChip(
                        text = tag,
                        isSelected = formData.selectedOccupation == tag,
                        onClick = { onUpdate(formData.copy(selectedOccupation = tag)) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                occupationTagsRow2.forEach { tag ->
                    RegisterSelectChip(
                        text = tag,
                        isSelected = formData.selectedOccupation == tag,
                        onClick = { onUpdate(formData.copy(selectedOccupation = tag)) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                occupationTagsRow3.forEach { tag ->
                    RegisterSelectChip(
                        text = tag,
                        isSelected = formData.selectedOccupation == tag,
                        onClick = { onUpdate(formData.copy(selectedOccupation = tag)) }
                    )
                }
            }
        }
        FormErrorText(errors.occupationError)

        Spacer(modifier = Modifier.height(20.dp))

        // Occupation Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formData.selectedOccupation,
                onValueChange = {},
                readOnly = true,
                isError = errors.occupationError != null,
                label = { Text("Occupation", fontFamily = Inter, fontSize = 14.sp) },
                placeholder = { Text("Select occupation", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { occupationDropdownExpanded = true },
                shape = RoundedCornerShape(16.dp),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_down),
                        contentDescription = "Expand occupation",
                        tint = IfxBlack,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { occupationDropdownExpanded = true }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IfxBlack,
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    errorBorderColor = Color(0xFFDC2626),
                    focusedTextColor = IfxBlack,
                    unfocusedTextColor = IfxBlack
                )
            )

            DropdownMenu(
                expanded = occupationDropdownExpanded,
                onDismissRequest = { occupationDropdownExpanded = false },
                modifier = Modifier
                    .background(Color.White)
                    .border(1.5.dp, IfxLime, RoundedCornerShape(16.dp))
            ) {
                allOccupations.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, fontFamily = Inter, fontSize = 14.sp, color = IfxBlack) },
                        onClick = {
                            onUpdate(formData.copy(selectedOccupation = option))
                            occupationDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // School or company (Starts EMPTY)
        OutlinedTextField(
            value = formData.schoolOrCompany,
            onValueChange = { onUpdate(formData.copy(schoolOrCompany = it)) },
            label = { Text("School or company (optional)", fontFamily = Inter, fontSize = 14.sp) },
            placeholder = { Text("Enter school or company", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IfxBlack,
                unfocusedBorderColor = Color(0xFFD1D5DB),
                focusedTextColor = IfxBlack,
                unfocusedTextColor = IfxBlack,
                cursorColor = IfxBlack
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Gemini recommendation banner (Dynamic based on selected occupation)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(IfxLime)
                .border(1.dp, IfxBlack, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gemini_star_vector),
                    contentDescription = "Gemini",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                val recommendationText = buildAnnotatedString {
                    append("Gemini will suggest a ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = IfxBlack)) {
                        append(suggestedProjectName)
                    }
                    append(" project with 3 starter Sparks.")
                }

                Text(
                    text = recommendationText,
                    fontFamily = Inter,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    color = IfxBlack
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// =========================================================================
// STEP 4: ROUTINES
// =========================================================================
@Composable
private fun RoutinesStep(
    formData: RegisterFormData,
    errors: RegisterStepErrors,
    onUpdate: (RegisterFormData) -> Unit
) {
    val routineTagsRow1 = listOf("Email deadlines", "Calendar")
    val routineTagsRow2 = listOf("Notes", "Study", "Work")
    val routineTagsRow3 = listOf("Location", "Battery & device")
    val routineTagsRow4 = listOf("Messages", "Travel", "Home")

    fun toggleRoutine(tag: String) {
        val updated = if (formData.selectedRoutines.contains(tag)) {
            formData.selectedRoutines - tag
        } else {
            formData.selectedRoutines + tag
        }
        onUpdate(formData.copy(selectedRoutines = updated))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "What will you\nautomate?",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp,
            color = IfxBlack
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Pick at least one routine. Gemini uses them to suggest Sparks.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Multi-select routine tags
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                routineTagsRow1.forEach { tag ->
                    RegisterSelectChip(
                        text = tag,
                        isSelected = formData.selectedRoutines.contains(tag),
                        onClick = { toggleRoutine(tag) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                routineTagsRow2.forEach { tag ->
                    RegisterSelectChip(
                        text = tag,
                        isSelected = formData.selectedRoutines.contains(tag),
                        onClick = { toggleRoutine(tag) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                routineTagsRow3.forEach { tag ->
                    RegisterSelectChip(
                        text = tag,
                        isSelected = formData.selectedRoutines.contains(tag),
                        onClick = { toggleRoutine(tag) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                routineTagsRow4.forEach { tag ->
                    RegisterSelectChip(
                        text = tag,
                        isSelected = formData.selectedRoutines.contains(tag),
                        onClick = { toggleRoutine(tag) }
                    )
                }
            }
        }
        FormErrorText(errors.routinesError)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "AUTOMATION EXPERIENCE",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = Color(0xFF6B7280),
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        ExperienceToggle(
            selectedExperience = formData.automationExperience,
            onExperienceSelected = { onUpdate(formData.copy(automationExperience = it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// =========================================================================
// STEP 5: PERMISSIONS
// =========================================================================
@Composable
private fun PermissionsStep(
    formData: RegisterFormData,
    onUpdate: (RegisterFormData) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Set up ifx",
            fontFamily = Inter,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp,
            color = IfxBlack
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Three quick permissions so your Sparks can run seamlessly.",
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 1. Notifications Toggle
        PermissionToggleItem(
            iconRes = R.drawable.ic_bell_outline,
            title = "Notifications",
            subtitle = "So ifx can ask for confirmation before high-impact steps.",
            checked = formData.notificationsAllowed,
            onCheckedChange = { onUpdate(formData.copy(notificationsAllowed = it)) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Exact Alarms Toggle
        PermissionToggleItem(
            iconRes = R.drawable.ic_clock_outline,
            title = "Exact alarms",
            subtitle = "Required for time triggers: 'every morning at 8:30'.",
            checked = formData.exactAlarmsGranted,
            onCheckedChange = { onUpdate(formData.copy(exactAlarmsGranted = it)) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Connect Google Toggle
        PermissionToggleItem(
            iconRes = R.drawable.ic_auth_google,
            title = "Connect Google",
            subtitle = "Optional. Read calendar & drafts to automate your schedule.",
            checked = formData.googleConnected,
            onCheckedChange = { onUpdate(formData.copy(googleConnected = it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionToggleItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = IfxBlack,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = IfxBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = IfxBlack,
                checkedTrackColor = IfxLime,
                uncheckedThumbColor = Color(0xFF9CA3AF),
                uncheckedTrackColor = Color(0xFFE5E7EB)
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun RegisterScreenPreview() {
    IFXTheme {
        RegisterScreen()
    }
}
