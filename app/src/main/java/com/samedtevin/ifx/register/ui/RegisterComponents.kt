package com.samedtevin.ifx.register.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.window.DialogProperties
import com.samedtevin.ifx.R
import com.samedtevin.ifx.ui.theme.IfxBlack
import com.samedtevin.ifx.ui.theme.IfxLime
import com.samedtevin.ifx.ui.theme.Inter
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Top bar for the registration flow.
 * Note: Skip option has been completely removed as all registration steps must be completed.
 */
@Composable
fun RegisterTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Back",
                tint = IfxBlack,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Progress indicator showing the current registration step category and step counter.
 */
@Composable
fun RegisterProgressBar(
    currentStep: Int,
    categoryName: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryName,
                fontFamily = Inter,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.5.sp,
                letterSpacing = 1.sp,
                color = IfxBlack
            )

            Text(
                text = "${currentStep + 1} of 5",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5-segment step bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (stepIndex in 0 until 5) {
                val isCompletedOrCurrent = stepIndex <= currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isCompletedOrCurrent) IfxLime else Color(0xFFE5E7EB))
                )
            }
        }
    }
}

/**
 * Reusable inline red error message component for form validation.
 */
@Composable
fun FormErrorText(
    message: String?,
    modifier: Modifier = Modifier
) {
    if (!message.isNullOrBlank()) {
        Text(
            text = message,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = Color(0xFFDC2626), // Tailwind Red 600
            modifier = modifier.padding(start = 6.dp, top = 4.dp)
        )
    }
}

/**
 * Custom Password strength and progress tracker matching IFX styling.
 */
@Composable
fun PasswordStrengthTracker(
    password: String,
    modifier: Modifier = Modifier
) {
    val lengthOk = password.length >= 8
    val hasLetterAndDigit = password.any { it.isLetter() } && password.any { it.isDigit() }
    val hasSymbol = password.any { !it.isLetterOrDigit() }

    val statusText = when {
        password.isEmpty() -> "Use 8+ characters with a letter, number & symbol."
        !lengthOk -> "Add ${8 - password.length} more characters."
        !hasLetterAndDigit -> "Include both letters and numbers."
        !hasSymbol -> "Good. Add a symbol for maximum security."
        else -> "Great! Very strong password."
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(width = 84.dp, height = 20.dp)) {
            val centerY = size.height / 2

            // 1. Circle with filled dot (Step 1)
            drawCircle(
                color = IfxBlack,
                radius = 6.dp.toPx(),
                center = Offset(8.dp.toPx(), centerY),
                style = Stroke(width = 2.dp.toPx())
            )
            if (lengthOk) {
                drawCircle(
                    color = IfxBlack,
                    radius = 3.dp.toPx(),
                    center = Offset(8.dp.toPx(), centerY)
                )
            }

            // Line 1
            drawLine(
                color = if (lengthOk) IfxBlack else Color(0xFFD1D5DB),
                start = Offset(16.dp.toPx(), centerY),
                end = Offset(36.dp.toPx(), centerY),
                strokeWidth = 2.dp.toPx()
            )

            // 2. Diamond (Step 2)
            val diamondCenter = Offset(44.dp.toPx(), centerY)
            val dSize = 6.dp.toPx()
            val diamondPath = Path().apply {
                moveTo(diamondCenter.x, diamondCenter.y - dSize)
                lineTo(diamondCenter.x + dSize, diamondCenter.y)
                lineTo(diamondCenter.x, diamondCenter.y + dSize)
                lineTo(diamondCenter.x - dSize, diamondCenter.y)
                close()
            }
            drawPath(
                path = diamondPath,
                color = if (hasLetterAndDigit) IfxBlack else Color(0xFFD1D5DB),
                style = Stroke(width = 2.dp.toPx())
            )

            // Line 2
            drawLine(
                color = if (hasLetterAndDigit) IfxBlack else Color(0xFFD1D5DB),
                start = Offset(52.dp.toPx(), centerY),
                end = Offset(72.dp.toPx(), centerY),
                strokeWidth = 2.dp.toPx()
            )

            // 3. X marker (Step 3)
            val xCenter = Offset(80.dp.toPx(), centerY)
            val xOffset = 4.dp.toPx()
            val xColor = if (hasSymbol) IfxBlack else Color(0xFFD1D5DB)
            drawLine(
                color = xColor,
                start = Offset(xCenter.x - xOffset, centerY - xOffset),
                end = Offset(xCenter.x + xOffset, centerY + xOffset),
                strokeWidth = 2.5.dp.toPx()
            )
            drawLine(
                color = xColor,
                start = Offset(xCenter.x - xOffset, centerY + xOffset),
                end = Offset(xCenter.x + xOffset, centerY - xOffset),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = statusText,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            color = Color(0xFF4B5563),
            lineHeight = 17.sp
        )
    }
}

/**
 * Selectable tag/pill chip used in Occupation and Routines.
 */
@Composable
fun RegisterSelectChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) IfxLime else Color.White)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) IfxBlack else Color(0xFF0B0B0B),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    tint = IfxBlack,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = IfxBlack
            )
        }
    }
}

/**
 * 3-way segmented control for Automation Experience: "New" | "Some" | "Pro".
 */
@Composable
fun ExperienceToggle(
    selectedExperience: String,
    onExperienceSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("New", "Some", "Pro")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFF0B0B0B), RoundedCornerShape(24.dp))
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = selectedExperience.equals(option, ignoreCase = true)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .background(if (isSelected) IfxLime else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onExperienceSelected(option) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null,
                            tint = IfxBlack,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = option,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = IfxBlack
                    )
                }
            }
            if (index < options.size - 1) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(46.dp)
                        .background(Color(0xFF0B0B0B))
                )
            }
        }
    }
}

/**
 * Custom checkbox component matching IFX design.
 */
@Composable
fun IfxCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) IfxLime else Color.White)
            .border(
                width = 1.5.dp,
                color = if (checked) IfxBlack else Color(0xFF0B0B0B),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "Checked",
                tint = IfxBlack,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Global repository of all ISO countries and territories in the world.
 */
object WorldCountries {
    val list: List<String> by lazy {
        Locale.getISOCountries()
            .map { code -> Locale.Builder().setRegion(code).build().getDisplayCountry(Locale.ENGLISH) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
}

/**
 * Searchable full-screen or modal dialog listing all countries and regions worldwide.
 */
@Composable
fun CountryPickerDialog(
    onDismissRequest: () -> Unit,
    onCountrySelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            WorldCountries.list
        } else {
            WorldCountries.list.filter { it.contains(query, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, IfxLime),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select country / region",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = IfxBlack,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = IfxBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IfxBlack,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        cursorColor = IfxBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredCountries) { country ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCountrySelected(country)
                                    onDismissRequest()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = country,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = IfxBlack
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    }

                    if (filteredCountries.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching countries found",
                                    fontFamily = Inter,
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modal dialog enabling the user to choose photo source: Camera, Gallery, or Remove Photo.
 */
@Composable
fun PhotoSourceDialog(
    hasCurrentPhoto: Boolean,
    onDismissRequest: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, IfxLime),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Profile photo",
                    fontFamily = Inter,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = IfxBlack
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Choose an option to set your avatar photo.",
                    fontFamily = Inter,
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Option 1: Take photo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onDismissRequest()
                            onTakePhoto()
                        }
                        .padding(vertical = 12.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Camera",
                        tint = IfxBlack,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Take a photo now",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = IfxBlack
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Option 2: Choose from gallery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            onDismissRequest()
                            onChooseGallery()
                        }
                        .padding(vertical = 12.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gallery),
                        contentDescription = "Gallery",
                        tint = IfxBlack,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Choose from gallery",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = IfxBlack
                    )
                }

                if (hasCurrentPhoto) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 3: Remove photo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onDismissRequest()
                                onRemovePhoto()
                            }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_x_bullet),
                            contentDescription = "Remove",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Remove photo",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = "Cancel",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Utility to save captured bitmap or copy URI safely to the app's internal files directory.
 */
object ImageStorageUtils {
    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            null
        }
    }

    fun copyUri(context: Context, uri: Uri): String? {
        return try {
            val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.jpg")
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val output = FileOutputStream(file)
            input.copyTo(output)
            input.close()
            output.close()
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Composable helper that decodes a local Uri or File string into an ImageBitmap.
 */
@Composable
fun rememberProfileBitmap(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uriString) {
        if (!uriString.isNullOrBlank()) {
            try {
                val uri = Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(uri)
                val decoded = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bitmap = decoded?.asImageBitmap()
            } catch (e: Exception) {
                bitmap = null
            }
        } else {
            bitmap = null
        }
    }

    return bitmap
}


/**
 * Model representing a country dial code with its flag emoji.
 */

/**
 * Helper generating native Unicode flag emoji from a 2-letter ISO country code.
 */
fun countryFlag(iso: String): String {
    if (iso.length != 2) return "🌐"
    val first = Character.toChars(0x1F1E6 + (iso[0].uppercaseChar() - 'A'))
    val second = Character.toChars(0x1F1E6 + (iso[1].uppercaseChar() - 'A'))
    return String(first) + String(second)
}

/**
 * Model representing a country dial code with dynamic flag emoji.
 */
data class CountryDialCode(
    val name: String,
    val iso: String,
    val dialCode: String
) {
    val flagEmoji: String
        get() = countryFlag(iso)
}

/**
 * Catalog of dial codes and flag emojis for phone input.
 */

/**
 * Catalog of dial codes and flag emojis for all countries worldwide.
 */
object CountryDialCodes {
    val list: List<CountryDialCode> = listOf(
        CountryDialCode("Afghanistan", "AF", "+93"),        CountryDialCode("Albania", "AL", "+355"),        CountryDialCode("Algeria", "DZ", "+213"),        CountryDialCode("American Samoa", "AS", "+1684"),        CountryDialCode("Andorra", "AD", "+376"),        CountryDialCode("Angola", "AO", "+244"),        CountryDialCode("Anguilla", "AI", "+1264"),        CountryDialCode("Antigua and Barbuda", "AG", "+1268"),        CountryDialCode("Argentina", "AR", "+54"),        CountryDialCode("Armenia", "AM", "+374"),        CountryDialCode("Aruba", "AW", "+297"),        CountryDialCode("Australia", "AU", "+61"),        CountryDialCode("Austria", "AT", "+43"),        CountryDialCode("Azerbaijan", "AZ", "+994"),        CountryDialCode("Bahamas", "BS", "+1242"),        CountryDialCode("Bahrain", "BH", "+973"),        CountryDialCode("Bangladesh", "BD", "+880"),        CountryDialCode("Barbados", "BB", "+1246"),        CountryDialCode("Belarus", "BY", "+375"),        CountryDialCode("Belgium", "BE", "+32"),        CountryDialCode("Belize", "BZ", "+501"),        CountryDialCode("Benin", "BJ", "+229"),        CountryDialCode("Bermuda", "BM", "+1441"),        CountryDialCode("Bhutan", "BT", "+975"),        CountryDialCode("Bolivia", "BO", "+591"),        CountryDialCode("Bosnia and Herzegovina", "BA", "+387"),        CountryDialCode("Botswana", "BW", "+267"),        CountryDialCode("Brazil", "BR", "+55"),        CountryDialCode("British Indian Ocean Territory", "IO", "+246"),        CountryDialCode("British Virgin Islands", "VG", "+1284"),        CountryDialCode("Brunei", "BN", "+673"),        CountryDialCode("Bulgaria", "BG", "+359"),        CountryDialCode("Burkina Faso", "BF", "+226"),        CountryDialCode("Burundi", "BI", "+257"),        CountryDialCode("Cambodia", "KH", "+855"),        CountryDialCode("Cameroon", "CM", "+237"),        CountryDialCode("Canada", "CA", "+1"),        CountryDialCode("Cape Verde", "CV", "+238"),        CountryDialCode("Cayman Islands", "KY", "+1345"),        CountryDialCode("Central African Republic", "CF", "+236"),        CountryDialCode("Chad", "TD", "+235"),        CountryDialCode("Chile", "CL", "+56"),        CountryDialCode("China", "CN", "+86"),        CountryDialCode("Colombia", "CO", "+57"),        CountryDialCode("Comoros", "KM", "+269"),        CountryDialCode("Congo", "CG", "+242"),        CountryDialCode("Congo (DRC)", "CD", "+243"),        CountryDialCode("Cook Islands", "CK", "+682"),        CountryDialCode("Costa Rica", "CR", "+506"),        CountryDialCode("Cote d'Ivoire", "CI", "+225"),        CountryDialCode("Croatia", "HR", "+385"),        CountryDialCode("Cuba", "CU", "+53"),        CountryDialCode("Cyprus", "CY", "+357"),        CountryDialCode("Czech Republic", "CZ", "+420"),        CountryDialCode("Denmark", "DK", "+45"),        CountryDialCode("Djibouti", "DJ", "+253"),        CountryDialCode("Dominica", "DM", "+1767"),        CountryDialCode("Dominican Republic", "DO", "+1809"),        CountryDialCode("Ecuador", "EC", "+593"),        CountryDialCode("Egypt", "EG", "+20"),        CountryDialCode("El Salvador", "SV", "+503"),        CountryDialCode("Equatorial Guinea", "GQ", "+240"),        CountryDialCode("Eritrea", "ER", "+291"),        CountryDialCode("Estonia", "EE", "+372"),        CountryDialCode("Eswatini", "SZ", "+268"),        CountryDialCode("Ethiopia", "ET", "+251"),        CountryDialCode("Fiji", "FJ", "+679"),        CountryDialCode("Finland", "FI", "+358"),        CountryDialCode("France", "FR", "+33"),        CountryDialCode("French Guiana", "GF", "+594"),        CountryDialCode("French Polynesia", "PF", "+689"),        CountryDialCode("Gabon", "GA", "+241"),        CountryDialCode("Gambia", "GM", "+220"),        CountryDialCode("Georgia", "GE", "+995"),        CountryDialCode("Germany", "DE", "+49"),        CountryDialCode("Ghana", "GH", "+233"),        CountryDialCode("Gibraltar", "GI", "+350"),        CountryDialCode("Greece", "GR", "+30"),        CountryDialCode("Greenland", "GL", "+299"),        CountryDialCode("Grenada", "GD", "+1473"),        CountryDialCode("Guadeloupe", "GP", "+590"),        CountryDialCode("Guam", "GU", "+1671"),        CountryDialCode("Guatemala", "GT", "+502"),        CountryDialCode("Guinea", "GN", "+224"),        CountryDialCode("Guinea-Bissau", "GW", "+245"),        CountryDialCode("Guyana", "GY", "+592"),        CountryDialCode("Haiti", "HT", "+509"),        CountryDialCode("Honduras", "HN", "+504"),        CountryDialCode("Hong Kong", "HK", "+852"),        CountryDialCode("Hungary", "HU", "+36"),        CountryDialCode("Iceland", "IS", "+354"),        CountryDialCode("India", "IN", "+91"),        CountryDialCode("Indonesia", "ID", "+62"),        CountryDialCode("Iran", "IR", "+98"),        CountryDialCode("Iraq", "IQ", "+964"),        CountryDialCode("Ireland", "IE", "+353"),        CountryDialCode("Israel", "IL", "+972"),        CountryDialCode("Italy", "IT", "+39"),        CountryDialCode("Jamaica", "JM", "+1876"),        CountryDialCode("Japan", "JP", "+81"),        CountryDialCode("Jordan", "JO", "+962"),        CountryDialCode("Kazakhstan", "KZ", "+7"),        CountryDialCode("Kenya", "KE", "+254"),        CountryDialCode("Kiribati", "KI", "+686"),        CountryDialCode("Kuwait", "KW", "+965"),        CountryDialCode("Kyrgyzstan", "KG", "+996"),        CountryDialCode("Laos", "LA", "+856"),        CountryDialCode("Latvia", "LV", "+371"),        CountryDialCode("Lebanon", "LB", "+961"),        CountryDialCode("Lesotho", "LS", "+266"),        CountryDialCode("Liberia", "LR", "+231"),        CountryDialCode("Libya", "LY", "+218"),        CountryDialCode("Liechtenstein", "LI", "+423"),        CountryDialCode("Lithuania", "LT", "+370"),        CountryDialCode("Luxembourg", "LU", "+352"),        CountryDialCode("Macau", "MO", "+853"),        CountryDialCode("Madagascar", "MG", "+261"),        CountryDialCode("Malawi", "MW", "+265"),        CountryDialCode("Malaysia", "MY", "+60"),        CountryDialCode("Maldives", "MV", "+960"),        CountryDialCode("Mali", "ML", "+223"),        CountryDialCode("Malta", "MT", "+356"),        CountryDialCode("Marshall Islands", "MH", "+692"),        CountryDialCode("Martinique", "MQ", "+596"),        CountryDialCode("Mauritania", "MR", "+222"),        CountryDialCode("Mauritius", "MU", "+230"),        CountryDialCode("Mexico", "MX", "+52"),        CountryDialCode("Micronesia", "FM", "+691"),        CountryDialCode("Moldova", "MD", "+373"),        CountryDialCode("Monaco", "MC", "+377"),        CountryDialCode("Mongolia", "MN", "+976"),        CountryDialCode("Montenegro", "ME", "+382"),        CountryDialCode("Montserrat", "MS", "+1664"),        CountryDialCode("Morocco", "MA", "+212"),        CountryDialCode("Mozambique", "MZ", "+258"),        CountryDialCode("Myanmar", "MM", "+95"),        CountryDialCode("Namibia", "NA", "+264"),        CountryDialCode("Nauru", "NR", "+674"),        CountryDialCode("Nepal", "NP", "+977"),        CountryDialCode("Netherlands", "NL", "+31"),        CountryDialCode("New Caledonia", "NC", "+687"),        CountryDialCode("New Zealand", "NZ", "+64"),        CountryDialCode("Nicaragua", "NI", "+505"),        CountryDialCode("Niger", "NE", "+227"),        CountryDialCode("Nigeria", "NG", "+234"),        CountryDialCode("North Macedonia", "MK", "+389"),        CountryDialCode("Norway", "NO", "+47"),        CountryDialCode("Oman", "OM", "+968"),        CountryDialCode("Pakistan", "PK", "+92"),        CountryDialCode("Palau", "PW", "+680"),        CountryDialCode("Palestine", "PS", "+970"),        CountryDialCode("Panama", "PA", "+507"),        CountryDialCode("Papua New Guinea", "PG", "+675"),        CountryDialCode("Paraguay", "PY", "+595"),        CountryDialCode("Peru", "PE", "+51"),        CountryDialCode("Philippines", "PH", "+63"),        CountryDialCode("Poland", "PL", "+48"),        CountryDialCode("Portugal", "PT", "+351"),        CountryDialCode("Puerto Rico", "PR", "+1787"),        CountryDialCode("Qatar", "QA", "+974"),        CountryDialCode("Reunion", "RE", "+262"),        CountryDialCode("Romania", "RO", "+40"),        CountryDialCode("Russia", "RU", "+7"),        CountryDialCode("Rwanda", "RW", "+250"),        CountryDialCode("Samoa", "WS", "+685"),        CountryDialCode("San Marino", "SM", "+378"),        CountryDialCode("Sao Tome and Principe", "ST", "+239"),        CountryDialCode("Saudi Arabia", "SA", "+966"),        CountryDialCode("Senegal", "SN", "+221"),        CountryDialCode("Serbia", "RS", "+381"),        CountryDialCode("Seychelles", "SC", "+248"),        CountryDialCode("Sierra Leone", "SL", "+232"),        CountryDialCode("Singapore", "SG", "+65"),        CountryDialCode("Slovakia", "SK", "+421"),        CountryDialCode("Slovenia", "SI", "+386"),        CountryDialCode("Solomon Islands", "SB", "+677"),        CountryDialCode("Somalia", "SO", "+252"),        CountryDialCode("South Africa", "ZA", "+27"),        CountryDialCode("South Korea", "KR", "+82"),        CountryDialCode("South Sudan", "SS", "+211"),        CountryDialCode("Spain", "ES", "+34"),        CountryDialCode("Sri Lanka", "LK", "+94"),        CountryDialCode("Sudan", "SD", "+249"),        CountryDialCode("Suriname", "SR", "+597"),        CountryDialCode("Sweden", "SE", "+46"),        CountryDialCode("Switzerland", "CH", "+41"),        CountryDialCode("Syria", "SY", "+963"),        CountryDialCode("Taiwan", "TW", "+886"),        CountryDialCode("Tajikistan", "TJ", "+992"),        CountryDialCode("Tanzania", "TZ", "+255"),        CountryDialCode("Thailand", "TH", "+66"),        CountryDialCode("Timor-Leste", "TL", "+670"),        CountryDialCode("Togo", "TG", "+228"),        CountryDialCode("Tonga", "TO", "+676"),        CountryDialCode("Trinidad and Tobago", "TT", "+1868"),        CountryDialCode("Tunisia", "TN", "+216"),        CountryDialCode("Turkey", "TR", "+90"),        CountryDialCode("Turkmenistan", "TM", "+993"),        CountryDialCode("Turks and Caicos", "TC", "+1649"),        CountryDialCode("Uganda", "UG", "+256"),        CountryDialCode("Ukraine", "UA", "+380"),        CountryDialCode("United Arab Emirates", "AE", "+971"),        CountryDialCode("United Kingdom", "GB", "+44"),        CountryDialCode("United States", "US", "+1"),        CountryDialCode("Uruguay", "UY", "+598"),        CountryDialCode("Uzbekistan", "UZ", "+998"),        CountryDialCode("Vanuatu", "VU", "+678"),        CountryDialCode("Venezuela", "VE", "+58"),        CountryDialCode("Vietnam", "VN", "+84"),        CountryDialCode("Yemen", "YE", "+967"),        CountryDialCode("Zambia", "ZM", "+260"),        CountryDialCode("Zimbabwe", "ZW", "+263")
    )

    fun findByCountryName(name: String): CountryDialCode? {
        val query = name.trim().lowercase()
        return list.firstOrNull { it.name.lowercase() == query }
    }
}

/**
 * Searchable modal dialog enabling user to select country dial code from all 212 countries worldwide.
 */
@Composable
fun PhoneCodePickerDialog(
    onDismissRequest: () -> Unit,
    onCodeSelected: (CountryDialCode) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = remember(searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            CountryDialCodes.list
        } else {
            CountryDialCodes.list.filter {
                it.name.lowercase().contains(query) ||
                it.dialCode.contains(query) ||
                it.iso.lowercase().contains(query)
            }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, IfxLime),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select country code",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = IfxBlack,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = IfxBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country or dial code (+90, US)...", color = Color(0xFF9CA3AF), fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IfxBlack,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        cursorColor = IfxBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredList) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onCodeSelected(item)
                                    onDismissRequest()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = item.flagEmoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.name,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.5.sp,
                                    color = IfxBlack,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = item.dialCode,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = Color(0xFF4B5563)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching country code found",
                                    fontFamily = Inter,
                                    fontSize = 14.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard legal text for Terms of Service and Privacy Policy.
 */
object LegalTermsContent {
    const val TEXT = """IFX TERMS OF SERVICE & PRIVACY POLICY

Last updated: October 2026

Welcome to IFX. Please review our Terms of Service and Privacy Policy carefully before using the IFX application and platform.

1. ACCEPTANCE OF TERMS
By registering, creating an account, or accessing IFX, you represent and warrant that you are at least 13 years of age and agree to be bound by these Terms of Service. If you do not agree to these terms, you must not use or access our services.

2. ACCOUNT SECURITY & USER RESPONSIBILITIES
You agree to provide true, accurate, current, and complete registration information. You are solely responsible for safeguarding your login credentials and password. Any actions taken through your account are your responsibility. You agree to notify IFX immediately of any unauthorized use or security breach.

3. DATA PRIVACY & INFORMATION COLLECTION
We prioritize your privacy. We collect the following categories of information:
- Identity & Contact Information: Your name, email address, date of birth, country/region, and phone number.
- Preferences & Profile: Selected routines, automation experience level, occupation, and optional profile photograph.
- Telemetry & Usage Data: Device model, OS version, performance logs, and interaction analytics to improve application stability.

4. AUTOMATION & SPARKS PERMISSIONS
IFX provides autonomous routine automation known as Sparks. When enabling Notifications and Exact Alarms permissions:
- Notifications allow IFX to alert you and request confirmation before running critical tasks.
- Exact Alarms allow IFX to trigger timely routines precisely at your scheduled hours.
- You maintain full control over your Sparks and may disable or revoke these permissions in your system settings at any time.

5. THIRD-PARTY INTEGRATIONS (GOOGLE SERVICES)
Connecting your Google account grants IFX read and sync access strictly to calendar events and email drafts as required to fulfill your chosen Sparks automations. We never sell, transfer, or disclose your private communications to third-party advertisers or data brokers.

6. DATA PROTECTION & RETENTION
Your personal information is encrypted in transit using industry-standard TLS protocols and stored securely in cloud-hosted environments adhering to modern encryption standards. You may request full deletion of your account and personal data at any time via Settings -> Account -> Delete Account.

7. PROHIBITED USES
You agree not to use IFX for any unlawful, harassing, fraudulent, or harmful purposes, nor to attempt to reverse engineer, disrupt, or compromise the platform's infrastructure or security.

8. DISCLAIMER OF WARRANTIES
IFX is provided on an "AS IS" and "AS AVAILABLE" basis without warranties of any kind, whether express or implied. We do not guarantee that automations will run without interruption or error.

9. GOVERNING LAW
These Terms shall be governed by and construed in accordance with applicable consumer protection and data privacy legislation.

10. CONTACT US
For any questions regarding our terms, data protection practices, or privacy rights, please contact our support team at legal@ifx.app.
"""
}

/**
 * Terms and Privacy Policy Alert Dialog:
 * Users must scroll to the bottom of the legal terms before the "OK / I Agree" button is enabled.
 */
@Composable
fun TermsAndPrivacyDialog(
    onDismissRequest: () -> Unit,
    onAccept: () -> Unit
) {
    val scrollState = rememberScrollState()
    val canAccept by remember {
        derivedStateOf {
            scrollState.maxValue == 0 || scrollState.value >= (scrollState.maxValue - 40)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, IfxBlack),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .fillMaxHeight(0.88f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms & Privacy Policy",
                        fontFamily = Inter,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = IfxBlack,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = "Close",
                            tint = IfxBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (canAccept) "Thank you for reviewing! You may now accept below." else "Please scroll through to the bottom to accept.",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.5.sp,
                    color = if (canAccept) Color(0xFF16A34A) else Color(0xFF6B7280)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reading progress indicator
                val scrollProgress by remember {
                    derivedStateOf {
                        if (scrollState.maxValue > 0) {
                            (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0f, 1f)
                        } else 0f
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE5E7EB))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(scrollProgress)
                            .fillMaxHeight()
                            .background(if (canAccept) Color(0xFF16A34A) else IfxLime)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable legal text
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.5.dp, IfxBlack, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = LegalTermsContent.TEXT,
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = IfxBlack
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons: Cancel and OK (Accept)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6),
                            contentColor = IfxBlack
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = IfxBlack,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Button(
                        onClick = {
                            onAccept()
                            onDismissRequest()
                        },
                        enabled = canAccept,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IfxLime,
                            contentColor = IfxBlack,
                            disabledContainerColor = Color(0xFFE5E7EB),
                            disabledContentColor = Color(0xFF9CA3AF)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (canAccept) "I Agree" else "Scroll to read",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
