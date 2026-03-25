package com.example.coll.data

/**
 * Репозиторий для работы с данными кабинетов
 * В будущем можно заменить на работу с базой данных или API
 */
object RoomRepository {
    
    private val rooms = mutableMapOf<String, Room>()
    
    init {
        // Пример данных для тестирования
        // В реальном приложении данные будут загружаться из базы данных или API
        addRoom(Room(
            id = "room_201",
            number = "201",
            name = "Кабинет информатики",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Программирование", "Иванов И.И.", "ИТ-21"),
                ScheduleItem("10:45-12:15", "Базы данных", "Петров П.П.", "ИТ-22"),
                ScheduleItem("13:00-14:30", "Веб-разработка", "Сидоров С.С.", "ИТ-23")
            )
        ))
        
        addRoom(Room(
            id = "room_202",
            number = "202",
            name = "Лаборатория",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Физика", "Кузнецов К.К.", "Ф-21"),
                ScheduleItem("10:45-12:15", "Химия", "Смирнова С.С.", "Х-22")
            )
        ))
        
        addRoom(Room(
            id = "room_203",
            number = "203",
            name = "Лекционный зал",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Математика", "Васильев В.В.", "М-21"),
                ScheduleItem("10:45-12:15", "Математика", "Васильев В.В.", "М-22")
            )
        ))
        
        addRoom(Room(
            id = "room_204",
            number = "204",
            name = "Кабинет 204",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Предмет", "Преподаватель", "Группа"),
                ScheduleItem("10:45-12:15", "Предмет", "Преподаватель", "Группа")
            )
        ))
        
        addRoom(Room(
            id = "room_205",
            number = "205",
            name = "Кабинет 205",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Предмет", "Преподаватель", "Группа"),
                ScheduleItem("10:45-12:15", "Предмет", "Преподаватель", "Группа")
            )
        ))
        
        addRoom(Room(
            id = "room_206",
            number = "206",
            name = "Кабинет 206",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Предмет", "Преподаватель", "Группа"),
                ScheduleItem("10:45-12:15", "Предмет", "Преподаватель", "Группа")
            )
        ))
        
        addRoom(Room(
            id = "room_207",
            number = "207",
            name = "Кабинет 207",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Предмет", "Преподаватель", "Группа"),
                ScheduleItem("10:45-12:15", "Предмет", "Преподаватель", "Группа")
            )
        ))
        
        addRoom(Room(
            id = "room_208",
            number = "208",
            name = "Кабинет 208",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Предмет", "Преподаватель", "Группа"),
                ScheduleItem("10:45-12:15", "Предмет", "Преподаватель", "Группа")
            )
        ))
        
        addRoom(Room(
            id = "room_209",
            number = "209",
            name = "Кабинет 209",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Предмет", "Преподаватель", "Группа"),
                ScheduleItem("10:45-12:15", "Предмет", "Преподаватель", "Группа")
            )
        ))
        
        addRoom(Room(
            id = "room_sports",
            number = "Sports",
            name = "Спортивный зал",
            floor = 2,
            schedule = listOf(
                ScheduleItem("09:00-10:30", "Физкультура", "Спортивный инструктор", "Группа"),
                ScheduleItem("10:45-12:15", "Физкультура", "Спортивный инструктор", "Группа")
            )
        ))
    }
    
    fun addRoom(room: Room) {
        rooms[room.id] = room
    }
    
    fun getRoomById(id: String): Room? {
        return rooms[id]
    }
    
    fun getAllRooms(): List<Room> {
        return rooms.values.toList()
    }
    
    fun getRoomsByFloor(floor: Int): List<Room> {
        return rooms.values.filter { it.floor == floor }
    }
}

