package com.lm.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.lm.app.data.User
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val TAG = "AuthRepository"
    private val functions = Firebase.functions("asia-south1")
    private val usersCollection = firestore.collection("users")

    suspend fun loginWithEmailPin(identifier: String, pin: String): Result<User> {
        return try {
            val trimmedInput = identifier.trim()
            val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedInput).matches()
            
            // Query Firestore for user with email OR phone
            val query = if (isEmail) {
                usersCollection.whereEqualTo("email", trimmedInput.lowercase())
            } else {
                usersCollection.whereEqualTo("phone", trimmedInput)
            }
            
            val snapshot = query.limit(1).get().await()
            if (snapshot.isEmpty) {
                return Result.failure(Exception(if (isEmail) "Email not found" else "Mobile Number not found"))
            }

            val doc = snapshot.documents.first()
            val storedPin = doc.getString("pin") ?: ""
            
            // Check PIN
            if (storedPin != pin) {
                return Result.failure(Exception("Incorrect PIN"))
            }

            // Check approval status
            val status = doc.getString("status") ?: "approved"
            if (status != "approved") {
                return Result.failure(Exception("Account pending approval. Please contact administrator."))
            }

            // Authenticate anonymously in Firebase to get a session
            auth.signInAnonymously().await()
            
            val user = User(
                kgid = doc.id,
                name = doc.getString("name") ?: "",
                gender = doc.getString("gender") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                district = doc.getString("district") ?: "",
                placeOfWorking = doc.getString("placeOfWorking") ?: "",
                department = doc.getString("department") ?: "",
                dob = doc.getString("dob")?.toLongOrNull(),
                doa = doc.getString("doa")?.toLongOrNull()
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }

    suspend fun registerUser(user: User, pin: String): Result<User> {
        return try {
            val normalizedEmail = user.email.trim().lowercase()
            
            // Check if email already exists
            val existing = usersCollection.whereEqualTo("email", normalizedEmail).limit(1).get().await()
            if (!existing.isEmpty) {
                return Result.failure(Exception("Email already registered"))
            }
            
            val userMap = hashMapOf(
                "name" to user.name,
                "gender" to user.gender,
                "email" to normalizedEmail,
                "phone" to user.phone,
                "district" to user.district,
                "placeOfWorking" to user.placeOfWorking,
                "department" to user.department,
                "dob" to user.dob?.toString(),
                "doa" to user.doa?.toString(),
                "pin" to pin, // Storing plain PIN for demo purposes (should hash)
                "status" to "pending",
                "app" to "leave-manager",
                "createdAt" to System.currentTimeMillis()
            )

            // Save to Firestore
            val docRef = usersCollection.document(user.kgid)
            docRef.set(userMap).await()

            // Authenticate
            auth.signInAnonymously().await()

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            Result.failure(e)
        }
    }

    suspend fun sendOtp(email: String): Result<String> {
        return try {
            val normalizedEmail = email.trim().lowercase()
            
            // Check if user exists first
            val existing = usersCollection.whereEqualTo("email", normalizedEmail).limit(1).get().await()
            if (existing.isEmpty) {
                return Result.failure(Exception("Email not registered"))
            }

            val data = hashMapOf("email" to normalizedEmail)
            
            val result = functions
                .getHttpsCallable("requestOtp")
                .call(data)
                .await()

            val responseData = result.

            getData() as? java.util.Map<*, *>
            if (responseData == null || responseData["success"] != true) {
                val msg = responseData?.get("message")?.toString() ?: "Failed to send OTP"
                return Result.failure(Exception(msg))
            }

            Result.success(responseData["message"]?.toString() ?: "OTP Sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send OTP", e)
            Result.failure(Exception("Server error or function not defined. ${e.message}"))
        }
    }

    suspend fun verifyOtpAndResetPin(email: String, otp: String, newPin: String): Result<String> {
        return try {
            val normalizedEmail = email.trim().lowercase()
            val data = hashMapOf(
                "email" to normalizedEmail,
                "code" to otp.trim()
            )

            // Call verify Cloud Function
            val result = functions
                .getHttpsCallable("verifyOtpEmail")
                .call(data)
                .await()

            val response = result.getData() as? java.util.Map<*, *>
            if (response == null || response["success"] != true) {
                val msg = response?.get("message")?.toString() ?: "Invalid OTP."
                return Result.failure(Exception(msg))
            }

            // If OTP verified, update the PIN
            val snapshot = usersCollection.whereEqualTo("email", normalizedEmail).limit(1).get().await()
            if (!snapshot.isEmpty) {
                snapshot.documents.first().reference.update("pin", newPin).await()
            }
            
            Result.success("PIN reset successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Verify OTP failed", e)
            Result.failure(Exception("Server error or function not defined. ${e.message}"))
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser == null) {
                return Result.failure(Exception("Google Sign-In failed"))
            }

            val email = firebaseUser.email ?: return Result.failure(Exception("No email found in Google account"))
            val normalizedEmail = email.trim().lowercase()

            val snapshot = usersCollection.whereEqualTo("email", normalizedEmail).limit(1).get().await()
            if (snapshot.isEmpty) {
                // Return a specific exception so the ViewModel knows to redirect to Registration
                val displayName = firebaseUser.displayName ?: ""
                return Result.failure(Exception("USER_NOT_FOUND:$displayName:$normalizedEmail"))
            } 
            
            val doc = snapshot.documents.first()
            
            // Check approval status for Google Sign-In too - DEFAULT TO APPROVED for existing users
            val status = doc.getString("status") ?: "approved"
            if (status != "approved") {
                return Result.failure(Exception("Account pending approval. Please contact administrator."))
            }
            val user = User(
                kgid = doc.id,
                name = doc.getString("name") ?: "",
                gender = doc.getString("gender") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                district = doc.getString("district") ?: "",
                placeOfWorking = doc.getString("placeOfWorking") ?: "",
                department = doc.getString("department") ?: "",
                dob = doc.getString("dob")?.toLongOrNull(),
                doa = doc.getString("doa")?.toLongOrNull()
            )

            Result.success(user)
        } catch (e: Exception) {
             Log.e(TAG, "Google Sign-In failed", e)
             Result.failure(e)
        }
    }

    suspend fun updateLastActive(kgid: String) {
        try {
            usersCollection.document(kgid).update("lastActive", System.currentTimeMillis()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update lastActive", e)
        }
    }

    suspend fun getDepartments(): List<String> {
        return try {
            val snapshot = firestore.collection("leave_manager_departments").get().await()
            if (snapshot.isEmpty) {
                listOf("Health", "Education", "Revenue", "Other")
            } else {
                snapshot.documents.mapNotNull { it.getString("name") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching departments", e)
            listOf("Health", "Education", "Revenue", "Other")
        }
    }
}
