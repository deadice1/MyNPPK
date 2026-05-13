// ApiModels.kt
package ru.filden.api

import com.google.gson.annotations.SerializedName


data class ApiBaseResponse<T>(
    val status: String,
    val data: T? = null,
    val message: String? = null
)


data class ApiUser(
    val id: Int,
    val login: String,
    val password: String,
    val role: Int
)

data class ApiStudent(
    val id: Int,
    val name: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("group_id") val groupId: Int,
    @SerializedName("count_duty") val countDuty: Int,
    @SerializedName("is_duty") val isDuty: Boolean = false
)

data class ApiGroup(
    val id: Int,
    val name: String
)

data class ApiDutyHistory(
    val id: Int,
    @SerializedName("first_student_id") val firstStudentId: Int,
    @SerializedName("second_student_id") val secondStudentId: Int?,
    @SerializedName("group_id") val groupId: Int,
    val date: String,
    @SerializedName("first_student_name") val firstStudentName: String? = null,
    @SerializedName("second_student_name") val secondStudentName: String? = null
)

data class ApiTeacher(
    val id: Int,
    val name: String,
    @SerializedName("user_id") val userId: Int
)

data class ApiTeacherGroup(
    val id: Int,
    @SerializedName("teacher_id") val teacherId: Int,
    @SerializedName("group_id") val groupId: Int
)

data class ApiCurrentDuty(
    @SerializedName("first_student_id") val firstStudentId: Int,
    @SerializedName("second_student_id") val secondStudentId: Int?,
    @SerializedName("first_student_name") val firstStudentName: String,
    @SerializedName("second_student_name") val secondStudentName: String?
)

data class CreateStudentRequest(
    val name: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("group_id") val groupId: Int
)

data class UpdateStudentRequest(
    val name: String,
    @SerializedName("count_duty") val countDuty: Int
)

data class CompleteDutyRequest(
    @SerializedName("first_student_id") val firstStudentId: Int,
    @SerializedName("second_student_id") val secondStudentId: Int?,
    @SerializedName("group_id") val groupId: Int
)

data class Student(
    val id: Int,
    val name: String,
    val countDuty: Int,
    val groupId: Int
)

data class DutyPair(
    val first: Student,
    val second: Student?
)

data class DutyHistoryRecord(
    val id: Int,
    val firstStudent: Student,
    val secondStudent: Student?,
    val date: String
)

// (1-student, 2-headman, 3-teacher, 4-admin)
enum class UserRole(val apiValue: Int, val displayName: String, val description: String) {
    STUDENT(1, "Студент", "Только просмотр"),
    HEADMAN(2, "Староста", "Выбор дежурных + управление студентами"),
    TEACHER(3, "Преподаватель", "Выбор дежурных + управление студентами + выбор группы"),
    ADMIN(4, "Администратор", "Полный доступ");

    companion object {
        fun fromApiValue(value: Int): UserRole = when (value) {
            1 -> STUDENT
            2 -> HEADMAN
            3 -> TEACHER
            4 -> ADMIN
            else -> STUDENT
        }
    }
}
fun UserRole.canSelectDuty(): Boolean =
    this == UserRole.HEADMAN || this == UserRole.TEACHER || this == UserRole.ADMIN

fun UserRole.canConfirmDuty(): Boolean =
    this == UserRole.HEADMAN || this == UserRole.TEACHER || this == UserRole.ADMIN

fun UserRole.canManageStudents(): Boolean =
    this == UserRole.HEADMAN || this == UserRole.TEACHER || this == UserRole.ADMIN

fun UserRole.canEditStudentData(): Boolean =
    this == UserRole.TEACHER || this == UserRole.ADMIN

fun UserRole.canChangeGroup(): Boolean =
    this == UserRole.TEACHER || this == UserRole.ADMIN