package com.lm.app.data

data class User(
    val kgid: String = "",
    val name: String = "",
    val gender: String = "male", // "male" or "female"
    val department: String = "", // "Health", "Education", "Revenue", etc.
    val dob: Long? = null,
    val doa: Long? = null,
    val email: String = "",
    val phone: String = "",
    val district: String = "",
    val placeOfWorking: String = ""
)
