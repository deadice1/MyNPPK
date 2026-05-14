package com.example.nppk.data.model

enum class UserRole(val id: Int, val displayName: String) {
    STUDENT(1, "Студент"),
    HEADMAN(2, "Староста"),
    TEACHER(3, "Преподаватель"),
    ADMIN(4, "Администратор");

    companion object {
        fun fromId(id: Int): UserRole = values().find { it.id == id } ?: STUDENT
        
        fun fromDisplayName(name: String): UserRole = when (name) {
            "Преподаватель" -> TEACHER
            "Староста" -> HEADMAN
            "Администратор" -> ADMIN
            else -> STUDENT
        }
    }
}
