package ru.filden.logic


////TEST
class ScheduleController {
    private val groups: MutableMap<String, Schedule> = mutableMapOf()

    init {
        initializeData()
    }
    private fun initializeData() {
        val students1 = listOf(
            Student("Ивав", 1, 0, "1"),
            Student("Андрей", 2, 0, "1"),
            Student("Михаил", 3, 0, "1"),
            Student("Антон", 4, 0, "1")
        )
        createGroup("1", students1)

        val students2 = listOf(
            Student("Ольга", 5, 0, "2"),
            Student("Анна", 6, 0, "2"),
            Student("Светлана", 7, 0, "2"),
            Student("Илья", 8, 0, "2")
        )
        createGroup("2", students2)

        val students3 = listOf(
            Student("Максим", 9, 0, "3"),
            Student("Арина", 10, 0, "3"),
            Student("Владислав", 11, 0, "3"),
            Student("Никита", 12, 0, "3")
        )
        createGroup("3", students3)

        val students4 = listOf(
            Student("Алёна", 13, 0, "4"),
            Student("Лариса", 14, 0, "4"),
            Student("Дарья", 15, 0, "4"),
            Student("Екатерина", 16, 0, "4")
        )
        createGroup("4", students4)
    }
    fun createGroup(groupId: String, students: List<Student>): Boolean {
        return if (!groups.containsKey(groupId)) {
            groups[groupId] = Schedule(students)
            true
        } else false
    }

    fun deleteGroup(groupId: String): Boolean {
        return groups.remove(groupId) != null
    }

    fun getAllGroups(): List<String> = groups.keys.toList()

    fun getSchedule(groupId: String): Schedule? = groups[groupId]

    fun getStudents(groupId: String): List<Student> {
        return groups[groupId]?.students ?: emptyList()
    }

    fun addStudentToGroup(student: Student, groupId: String): Boolean {
        return if (groups.containsKey(groupId)) {
            val updatedStudent = student.copy(groupId = groupId)
            groups[groupId]?.addStudent(updatedStudent)
            true
        } else false
    }

    fun removeStudentFromGroup(studentId: Int, groupId: String): Boolean {
        return groups[groupId]?.removeStudentById(studentId) ?: false
    }
    fun removeStudentGlobally(studentId: Int): Boolean {
        for ((groupId, schedule) in groups) {
            if (schedule.removeStudentById(studentId)) {
                return true
            }
        }
        return false
    }
    fun findStudentInGroup(studentId: Int, groupId: String): Student? {
        return groups[groupId]?.findStudentById(studentId)
    }

    fun findStudentGlobally(studentId: Int): Student? {
        for (schedule in groups.values) {
            schedule.findStudentById(studentId)?.let { return it }
        }
        return null
    }
    fun updateStudentInGroup(student: Student, groupId: String): Boolean {
        return if (groups.containsKey(groupId) && student.groupId == groupId) {
            groups[groupId]?.updateStudent(student) ?: false
        } else false
    }

    fun updateStudentGlobally(student: Student): Boolean {
        val groupId = student.groupId ?: return false
        return updateStudentInGroup(student, groupId)
    }

    fun moveStudentToGroup(studentId: Int, fromGroupId: String, toGroupId: String): Boolean {
        val student = findStudentInGroup(studentId, fromGroupId) ?: return false
        return if (removeStudentFromGroup(studentId, fromGroupId)) {
            val movedStudent = student.copy(groupId = toGroupId)
            addStudentToGroup(movedStudent, toGroupId)
        } else false
    }

    fun completeDutyInGroup(groupId: String, pair: Schedule.Pair): Boolean {
        return try {
            groups[groupId]?.completeDuty(pair)
            true
        } catch (e: Exception) {
            false
        }
    }
    fun getCurrentPair(groupId: String): Schedule.Pair? {
        return groups[groupId]?.currentPair
    }
    fun groupExists(groupId: String): Boolean = groups.containsKey(groupId)

}


