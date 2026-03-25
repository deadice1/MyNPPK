package ru.filden.logic

data class Student(
    var name: String,
    val uuid: Int,
    var countDuty: Int,
    var groupId: String? = null
) {
    fun increment(): Student {
        countDuty++
        return this
    }

    fun pairOf(second: Student): Schedule.Pair = Schedule.Pair(this, second)
}
