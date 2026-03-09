package com.lm.app.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.lm.app.data.User
import com.lm.app.ui.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegistrationSuccess: () -> Unit,
    prefillEmail: String = "",
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Form Fields matching our User class and PMD style
    var name by remember { mutableStateOf("") }
    var kgid by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var placeOfWorking by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(prefillEmail) }
    var phone by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf<Date?>(null) }
    var dateOfAppointment by remember { mutableStateOf<Date?>(null) }
    
    // Auth specific fields
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }

    // UI State
    var genderExpanded by remember { mutableStateOf(false) }
    var departmentExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showValidationErrors by remember { mutableStateOf(false) }
    
    val isLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.error.collectAsState()
    val pendingGoogleUser by authViewModel.pendingGoogleUser.collectAsState()

    // Only use ViewModel fallback if prefillEmail wasn't provided via nav arg
    LaunchedEffect(pendingGoogleUser) {
        if (prefillEmail.isBlank() && !pendingGoogleUser.isNullOrBlank() && email.isBlank()) {
            email = pendingGoogleUser!!
        }
    }

    val fieldSpacing = 8.dp
    val sectionSpacing = 16.dp

    var departments by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFetchingDepartments by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        departments = authViewModel.authRepository.getDepartments()
        isFetchingDepartments = false
    }

    val allDistricts = listOf("Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban", "Bidar", "Chamarajanagar", "Chikkaballapura", "Chikkamagaluru", "Chitradurga", "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan", "Haveri", "Kalaburagi", "Kodagu", "Kolar", "Koppal", "Mandya", "Mysuru", "Raichur", "Ramanagara", "Shivamogga", "Tumakuru", "Udupi", "Uttara Kannada", "Vijayanagara", "Vijayapura", "Yadgir").sorted()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 Photo Picker UI
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            Toast.makeText(context, "Photo picker to be implemented", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Select Photo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            "Add Photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(sectionSpacing))

            // 🔹 Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name*") },
                modifier = Modifier.fillMaxWidth(),
                isError = showValidationErrors && name.isBlank()
            )
            if (showValidationErrors && name.isBlank()) {
                Text("Name required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 KGID
            OutlinedTextField(
                value = kgid,
                onValueChange = { kgid = it },
                label = { Text("KGID*") },
                modifier = Modifier.fillMaxWidth(),
                isError = showValidationErrors && kgid.isBlank()
            )
            if (showValidationErrors && kgid.isBlank()) {
                Text("KGID required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 Department Breakdown
            ExposedDropdownMenuBox(
                expanded = departmentExpanded,
                onExpandedChange = { departmentExpanded = !departmentExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = department.ifEmpty { "Select Department" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Department*") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = departmentExpanded) },
                    isError = showValidationErrors && department.isBlank()
                )
                ExposedDropdownMenu(expanded = departmentExpanded, onDismissRequest = { departmentExpanded = false }) {
                    if (isFetchingDepartments) {
                        DropdownMenuItem(text = { Text("Loading departments...") }, onClick = {})
                    } else if (departments.isEmpty()) {
                        DropdownMenuItem(text = { Text("No departments found") }, onClick = {})
                    } else {
                        departments.forEach { selection ->
                            DropdownMenuItem(text = { Text(selection) }, onClick = {
                                department = selection
                                departmentExpanded = false
                            })
                        }
                    }
                }
            }
            if (showValidationErrors && department.isBlank()) {
                Text("Department required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 District Dropdown
            ExposedDropdownMenuBox(
                expanded = districtExpanded,
                onExpandedChange = { districtExpanded = !districtExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = district.ifEmpty { "Select District" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("District*") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                    isError = showValidationErrors && district.isBlank()
                )
                ExposedDropdownMenu(
                    expanded = districtExpanded, 
                    onDismissRequest = { districtExpanded = false },
                    modifier = Modifier.heightIn(max = 250.dp) // Scrollable list
                ) {
                    allDistricts.forEach { selection ->
                        DropdownMenuItem(text = { Text(selection) }, onClick = {
                            district = selection
                            districtExpanded = false
                        })
                    }
                }
            }
            if (showValidationErrors && district.isBlank()) {
                Text("District required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 Place of Working
            OutlinedTextField(
                value = placeOfWorking,
                onValueChange = { placeOfWorking = it },
                label = { Text("Place of Working*") },
                modifier = Modifier.fillMaxWidth(),
                isError = showValidationErrors && placeOfWorking.isBlank()
            )
            if (showValidationErrors && placeOfWorking.isBlank()) {
                Text("Place of Working required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address*") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = showValidationErrors && email.isBlank()
            )
            if (showValidationErrors && email.isBlank()) {
                Text("Email required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 Phone Number
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Mobile Number*") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = showValidationErrors && phone.isBlank()
            )
            if (showValidationErrors && phone.isBlank()) {
                Text("Mobile Number required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 Gender List (PMD logic)
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = gender.ifEmpty { "Select Gender" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender*") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    isError = showValidationErrors && gender.isBlank()
                )
                ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                    listOf("Male", "Female", "Other").forEach { selection ->
                        DropdownMenuItem(text = { Text(selection) }, onClick = {
                            gender = selection
                            genderExpanded = false
                        })
                    }
                }
            }
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 Date of Birth
            val dobCalendar = Calendar.getInstance()
            dateOfBirth?.let { dobCalendar.time = it }
            val dobDatePicker = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    dateOfBirth = cal.time
                },
                dobCalendar.get(Calendar.YEAR),
                dobCalendar.get(Calendar.MONTH),
                dobCalendar.get(Calendar.DAY_OF_MONTH)
            )

            OutlinedTextField(
                value = dateOfBirth?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "",
                onValueChange = { },
                label = { Text("Date of Birth (Optional)") },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = "Select Date", modifier = Modifier.clickable { dobDatePicker.show() })
                },
                modifier = Modifier.fillMaxWidth().clickable { dobDatePicker.show() },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 Date of Appointment
            val apptCalendar = Calendar.getInstance()
            dateOfAppointment?.let { apptCalendar.time = it }
            val apptDatePicker = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    dateOfAppointment = cal.time
                },
                apptCalendar.get(Calendar.YEAR),
                apptCalendar.get(Calendar.MONTH),
                apptCalendar.get(Calendar.DAY_OF_MONTH)
            )

            OutlinedTextField(
                value = dateOfAppointment?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: "",
                onValueChange = { },
                label = { Text("Date of Appointment (Optional)") },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = "Select Date", modifier = Modifier.clickable { apptDatePicker.show() })
                },
                modifier = Modifier.fillMaxWidth().clickable { apptDatePicker.show() },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.height(sectionSpacing))

            authError?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 🔹 PIN Requirements
            Text("Create PIN (6+ Digits)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN*") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = showValidationErrors && (pin.isBlank() || pin.length < 6)
            )
            if (showValidationErrors && (pin.isBlank() || pin.length < 6)) {
                Text("Valid 6+ digit PIN required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(Modifier.height(fieldSpacing))

            OutlinedTextField(
                value = confirmPin,
                onValueChange = { confirmPin = it },
                label = { Text("Confirm PIN*") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = showValidationErrors && (confirmPin.isBlank() || confirmPin != pin)
            )
            if (showValidationErrors && confirmPin != pin) {
                Text("PINs do not match", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(Modifier.height(fieldSpacing))

            // 🔹 Terms
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = acceptedTerms, onCheckedChange = { acceptedTerms = it })
                Spacer(Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("I accept ")
                    Text(
                        text = "Terms & Conditions",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Terms opened", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            Spacer(Modifier.height(sectionSpacing))

            // 🔹 Submit Button
            Button(
                onClick = {
                    showValidationErrors = true

                    if (name.isBlank() || kgid.isBlank() || gender.isBlank() || department.isBlank() || 
                        district.isBlank() || placeOfWorking.isBlank() || email.isBlank() || phone.isBlank() ||
                        pin.length < 6 || pin != confirmPin || !acceptedTerms) {
                        Toast.makeText(context, "Please fix remaining errors", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val newUser = User(
                        kgid = kgid.trim(), // Reusing kgid as ID equivalent since it's unique
                        name = name.trim(),
                        gender = gender,
                        email = email.trim(),
                        phone = phone.trim(),
                        district = district,
                        placeOfWorking = placeOfWorking.trim(),
                        department = department,
                        dob = dateOfBirth?.time,
                        doa = dateOfAppointment?.time
                    )
                        
                    authViewModel.register(newUser, pin) {
                        Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        onRegistrationSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Register Now")
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}
