package com.lm.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lm.app.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentUser: User?,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var kgid by remember { mutableStateOf(currentUser?.kgid ?: "") }
    var gender by remember { mutableStateOf(currentUser?.gender ?: "male") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var district by remember { mutableStateOf(currentUser?.district ?: "") }
    var placeOfWorking by remember { mutableStateOf(currentUser?.placeOfWorking ?: "") }
    var department by remember { mutableStateOf(currentUser?.department ?: "") }
    var dobString by remember { mutableStateOf("") }
    var doaString by remember { mutableStateOf("") }

    val departments = listOf("Health", "Education", "Revenue", "Police", "Other")
    var expandedDepartment by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = kgid,
                    onValueChange = { kgid = it },
                    label = { Text("KGID") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("District") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = placeOfWorking,
                    onValueChange = { placeOfWorking = it },
                    label = { Text("Place of Working") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Gender Selection
                Text("Gender", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = gender == "male",
                        onClick = { gender = "male" },
                        label = { Text("Male") }
                    )
                    FilterChip(
                        selected = gender == "female",
                        onClick = { gender = "female" },
                        label = { Text("Female") }
                    )
                }

                // Department Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedDepartment,
                    onExpandedChange = { expandedDepartment = !expandedDepartment }
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDepartment) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDepartment,
                        onDismissRequest = { expandedDepartment = false }
                    ) {
                        departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept) },
                                onClick = {
                                    department = dept
                                    expandedDepartment = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dobString,
                    onValueChange = { dobString = it },
                    label = { Text("Date of Birth (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = doaString,
                    onValueChange = { doaString = it },
                    label = { Text("Date of Appointment (DD/MM/YYYY)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedUser = currentUser?.copy(
                        name = name,
                        kgid = kgid,
                        gender = gender,
                        phone = phone,
                        email = email,
                        district = district,
                        placeOfWorking = placeOfWorking,
                        department = department
                    ) ?: User(
                        kgid = kgid, 
                        name = name, 
                        gender = gender, 
                        phone = phone,
                        email = email,
                        district = district,
                        placeOfWorking = placeOfWorking,
                        department = department
                    )
                    
                    onSave(updatedUser)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
