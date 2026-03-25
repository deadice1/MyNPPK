package com.example.coll.data

/**
 * Модель кабинета
 */
data class Room(
    val id: String,
    val number: String,
    val name: String,
    val floor: Int,
    val schedule: List<ScheduleItem> = emptyList()
)

/**
 * Элемент расписания
 */
data class ScheduleItem(
    val time: String,
    val subject: String,
    val teacher: String,
    val group: String
)

