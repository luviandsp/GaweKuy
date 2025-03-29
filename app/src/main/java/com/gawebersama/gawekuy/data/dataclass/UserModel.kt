package com.gawebersama.gawekuy.data.dataclass

import com.google.firebase.Timestamp

data class UserModel(
    val userId: String? = "",
    val email: String?,
    val name: String?,
    val phone: String?,
    val role: String?,
    val profileImageUrl: String?,
    val biography: String?,
    val createdAt: Timestamp?,
    val userStatus: String?,
    val accountStatus: Boolean?
)
