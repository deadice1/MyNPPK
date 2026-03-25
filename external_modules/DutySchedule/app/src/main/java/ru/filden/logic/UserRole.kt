package ru.filden.logic

enum class UserRole(val displayName: String, val description: String) {
    STUDENT("Студент", "Только просмотр"),
    HEADMAN("Староста", "дежурства + управление студентами"),
    TEACHER("Преподаватель", "Полный доступ")
}