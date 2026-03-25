package ru.filden.logic

class Schedule(initialStudents: List<Student>) {
    private val _students = initialStudents.sortedBy { it.countDuty }.toMutableList()
    private var _currentPair: Pair = if (_students.size >= 2) Pair(_students[0], _students[1])
    else throw IllegalStateException("Need at least 2 students")

    val students: List<Student>
        get() = _students.toList()

    val currentPair: Pair
        get() = _currentPair

    fun completeDuty(pair: Pair) {
        _students.replaceAll { student ->
            when (student) {
                pair.first, pair.second -> student.increment()
                else -> student
            }
        }
        _students.sortBy { it.countDuty }
        updateCurrentPair()
    }

    private fun updateCurrentPair() {
        _currentPair = if (_students.size >= 2) Pair(_students[0], _students[1])
        else throw IllegalStateException("Need at least 2 students")
    }

    fun findStudentById(id: Int): Student? = _students.find { it.uuid == id }

    fun findStudentByName(name: String): Student? = _students.find { it.name.equals(name, ignoreCase = true) }

    fun addStudent(student: Student) {
        _students.add(student)
        _students.sortBy { it.countDuty }
        updateCurrentPair()
    }

    fun removeStudentById(id: Int): Boolean {
        val removed = _students.removeAll { it.uuid == id }
        if (removed && _students.isNotEmpty()) {
            _students.sortBy { it.countDuty }
            updateCurrentPair()
        }
        return removed
    }

    fun updateStudent(updatedStudent: Student): Boolean {
        val index = _students.indexOfFirst { it.uuid == updatedStudent.uuid }
        return if (index != -1) {
            _students[index] = updatedStudent
            _students.sortBy { it.countDuty }
            updateCurrentPair()
            true
        } else false
    }

    data class Pair(var first: Student, var second: Student)
}