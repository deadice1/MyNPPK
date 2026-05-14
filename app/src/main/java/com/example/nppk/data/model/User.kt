package com.example.nppk.data.model

data class User(
    val id: Int,
    val login: String,
    val fullName: String,
    val role: UserRole,
    val groupNumber: String
)
