package com.example.coll.map

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ApiLessonSlot(
    val position: Int,
    val group: String,
    val name: String,
    val room: String,
)

data class MapRoomDef(
    val id: String,
    val title: String,
    val shortInfo: String,
    val responsible: String?,
    val apiRoomName: String?,
    val fetchScheduleFromApi: Boolean,
)

data class FloorHit(
    val roomId: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

private const val API_BASE = "http://147.124.204.18:8000/api/room/"

private val gson = Gson()

object MapRoomRegistry {

    /**
     * Для проверки ДСИ и расписания: подставить дату вместо календаря устройства.
     * После проверки присвойте null — будет использоваться реальная дата.
     * (Не const: в Kotlin у const допускаются только ненулевые примитивы и String.)
     */
    private val MAP_CALENDAR_OVERRIDE: String? = null

    private val roomsById: Map<String, MapRoomDef> = listOf(

        MapRoomDef(
            id = "201",
            title = "201 кабинет",
            shortInfo = "Лаборатории и учебные зоны: ПО и сопровождение КС; программирование и базы данных; информационные технологии.",
            responsible = "Игликова Гульдина Жаслановна",
            apiRoomName = "201",
            fetchScheduleFromApi = true,
        ),
        MapRoomDef(
            "202",
            "202 кабинет",
            "Учебная часть. Выдача справок с 13:45 до 16:00",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "203",
            "203  кабинет",
            "Информационно-методический центр (ИМЦ).",
            "Лобенко Юлия Владимировна",
            null,
            false,
        ),
        MapRoomDef("204", "204 кабинет", "Учебная аудитория.", null, "204", true),
        MapRoomDef(
            "205",
            "205 кабинет ",
            "Организация самостоятельной и воспитательной работы студентов.",
            "Нижникова Ирина Яковлевна",
            "205",
            true,
        ),
        MapRoomDef(
            "206",
            "206 кабинет",
            "Биология и физиология растений; анатомия и гигиена; теория и история физкультуры; адаптивная физкультура; лаборатория функциональной диагностики.",
            "Лапицкая Татьяна Владимировна",
            "206",
            true,
        ),
        MapRoomDef(
            "207",
            "207 кабинет",
            "Кабинет и лаборатория «Безопасность жизнедеятельности» (ОБЖ). График приёма и сдачи долгов: каждый день после 4 пары.",
            "Копцев Владимир Михайлович",
            "207",
            true,
        ),
        MapRoomDef(
            "208",
            "208 кабинет",
            "Кабинет «Русский язык и культура речи».",
            "Крыгина Роза Михайловна",
            "208",
            true,
        ),
        MapRoomDef(
            "209",
            "209 кабинет",
            "Основы геологии, геоморфологии и почвоведения; почвоведение, земледелие и агрохимия; геодезия.",
            "Ермакова Наталья Владимировна",
            "209",
            true,
        ),
        MapRoomDef(
            "SPORT_HALL",
            "Спортивный зал колледжа",
            "Занятия по физической культуре и спорту.",
            null,
            null,
            false,
        ),
        // --- 3 этаж ---
        MapRoomDef(
            "301",
            "301 кабинет",
            "Кабинет «Теоретических основ организации учебной и учебно-производственной деятельности обучающихся», «Теоретических основ организации педагогического сопровождения обучающихся», «Отраслевых общепрофессиональных дисциплин».",
            "Балдина Ирина Петровна",
            "301",
            true,
        ),
        MapRoomDef(
            "302",
            "302 кабинет",
            "Кабинет «Истории и философии», «Основ философии».",
            "Ермолович Борис Александрович",
            "302",
            true,
        ),
        MapRoomDef(
            "303",
            "303 кабинет",
            "Кабинет «Кадастрового учёта». Лаборатория «Информационных технологий в профессиональной деятельности».",
            "Байкин Дмитрий",
            "303",
            true,
        ),
        MapRoomDef(
            "304",
            "304 кабинет",
            "Кабинет иностранного языка.",
            "Фролова Любовь Сергеевна",
            "304",
            true,
        ),
        MapRoomDef(
            "305",
            "305 кабинет",
            "Кабинет «Экономики и менеджмента», «Экономики организации, менеджмента и маркетинга», «Менеджмента и предпринимательской деятельности».",
            "Зильбернагель Яна Геннадьевна",
            "305",
            true,
        ),
        MapRoomDef(
            "306",
            "306 кабинет",
            "Кабинет «Метрологии и стандартизации», «Стандартизации и сертификации», «Зданий и сооружений», «Строительного дела и материалов». Лаборатория «Материаловедения», «Строительного дела и материалов».",
            "Хадеева Наталья Александровна",
            "306",
            true,
        ),
        MapRoomDef(
            "307",
            "307 кабинет",
            "Кабинет «Общепрофессиональных дисциплин», «Теории государства и права», «Конституционного права», «Гражданского права», «Административного права». Мастерская «Юриспруденции (кабинет профессиональных дисциплин)».",
            "Овчинников Антон Анатольевич",
            "307",
            true,
        ),
        MapRoomDef("308", "308 кабинет", "Учебная аудитория.", null, "308", true),

        MapRoomDef(
            "DIR",
            "Кабинет ",
            "Директора колледжа",
            "Лузан Светлана Сергеевна",
            null,
            false,
        ),
        MapRoomDef(
            "DEP_KH",
            "Кабинет ",
            "Заместителя директора по учебной работе",
            "Ходоенко Наталья Владимировна",
            null,
            false,
        ),
        MapRoomDef(
            "309",
            "309 кабинет",
            "Техник: Гречухина Ангелина Сергеевна. Мастер производственного обучения: Петрова Валентина Петровна. Перерыв на обед 12:30–13:00.",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "310",
            "310 кабинет",
            "Заведующий очной формы обучения. Перерыв на обед 12:30–13:00.",
            "Нагорная Елизавета Сергеевна",
            null,
            false,
        ),

        MapRoomDef(
            "111",
            "111 кабинет",
            "Иностранный язык, психология и педагогика; методическое сопровождение адаптивной физкультуры; учебная мастерская по профилю.",
            null,
            "111",
            true,
        ),
        MapRoomDef(
            "CONF_HALL",
            "Конференц-зал ",
            "Актовый зал для событий и собраний",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "110",
            "110  кабинет",
            "Библиотечно-информационный центр (БИЦ).",
            "Рыболовлева Лариса Сергеевна",
            "110",
            false,
        ),
        MapRoomDef(
            "101",
            "101 кабинет",
            "Заочное отделение; психологическая служба",
            "Шрамова Янина Михайловна",
            null,
            false,
        ),
        MapRoomDef("102", "102 — приёмная", "Приёмная колледжа.", null, "102", false),
        MapRoomDef(
            "103",
            "103  кабинет",
            "Центр воспитания и социальной работы.",
            null,
            "103",
            false,
        ),
        MapRoomDef(
            "104",
            "104  кабинет",
            "Отдел кадров.",
            "Канина Ирина Владимировна",
            null,
            false,
        ),
        MapRoomDef(
            "105",
            "105  кабинет",
            "Главный бухгалтер.",
            "Ершова Людмила Ивановна",
            null,
            false,
        ),
        MapRoomDef("106", "106 — кабинет", "Бухгалтерия (касса — по планировке рядом).", null, "106", false),
        MapRoomDef("107", "107 кабинет", "Учебная аудитория.", "Токарев Антон Павлович", "107", true),
        MapRoomDef(
            "107a",
            "107а  хозяйственный отдел",
            "Начальник хозяйственного отдела. Обеденный перерыв 12:00–13:00 .",
            null,
            "107a",
            false,
        ),
        // --- Подвал ---
        MapRoomDef("3", "3 кабинет", "Вычислительный центр.", null, null, false),
        MapRoomDef(
            "4",
            "4 кабинет",
            "Кабинет «Информатики», «Информационных технологий». Лаборатория «Информационных технологий», «Информационных систем и ресурсов».",
            "Башлыков Илья Дмитриевич",
            "4",
            true,
        ),
        MapRoomDef(
            "5",
            "5 кабинет",
            "Кабинет «Нормативного правового обеспечения информационной безопасности». Лаборатории «Сетей и систем передачи информации, программных и программно-аппаратных средств защиты информации», «Технических средств защиты информации», «Электроники и схемотехники».",
            "Шатохина Ольга Александровна",
            "5",
            true,
        ),
        MapRoomDef(
            "6",
            "6 кабинет",
            "Кабинет «Социально-экономических дисциплин», «Социально-гуманитарных дисциплин», «Гуманитарных и социально-экономических дисциплин».",
            "Черных Зоя Владимировна",
            "6",
            true,
        ),
        MapRoomDef(
            "7",
            "7 кабинет",
            "Кабинет «Правового обеспечения профессиональной деятельности», «Документационного обеспечения управления». Лаборатория «Информационные технологии».",
            "Нуркеев Азамат Каппасович",
            "7",
            true,
        ),
        MapRoomDef(
            "WARDROBE",
            "Гардероб",
            "Гардероб.",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "KITCHEN",
            "Кухня",
            "Кухня (подвал).",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "UTILITY",
            "Подсобное помещение",
            "Служебное подсобное помещение.",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "CANTEEN",
            "Столовая",
            "Столовая работает с 10:00 до 17:00.",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "112",
            "112  кабинет",
            "Дистанционного обучения.",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "WC_M1",
            "Санузел, 1 этаж",
            "Мужской.",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "WC_W1",
            "Санузел, 1 этаж",
            "Женский.",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "WC_M2",
            "Санузел, 2 этаж",
            "Мужской.",
            null,
            null,
            false,
        ),
        MapRoomDef(
            "WC_W2",
            "Санузел, 2 этаж",
            "Женский.",
            null,
            null,
            false,
        ),
    ).associateBy { it.id }

    fun roomDef(id: String): MapRoomDef? = roomsById[id]

    /** Подбор кабинета по нормализованным координатам SVG (0..1), как в [FloorHit]. */
    fun findRoomAt(asset: String, nx: Float, ny: Float): MapRoomDef? {
        val x = nx.coerceIn(0f, 1f)
        val y = ny.coerceIn(0f, 1f)
        val eps = 0.0025f

        for (hit in hitsForAsset(asset).asReversed()) {
            if (x >= hit.left - eps && x <= hit.right + eps &&
                y >= hit.top - eps && y <= hit.bottom + eps
            ) {
                return roomDef(hit.roomId)
            }
        }
        return null
    }


    fun scheduleBlockForToday(slots: List<ApiLessonSlot>): String {
        if (slots.isEmpty()) {
            return "\n\nНа сегодня по расписанию занятий нет."
        }
        val byPos = slots.groupBy { it.position }.mapValues { (_, v) -> v.last() }
        val maxPos = kotlin.math.max(6, slots.maxOfOrNull { it.position } ?: 0)
        val lines = (1..maxPos).joinToString("\n") { p ->
            val s = byPos[p]
            if (s != null) "Пара $p: ${s.name} (${s.group})" else "Пара $p: —"
        }
        return "\n\nРасписание на сегодня:\n$lines"
    }


    fun hitsForAsset(asset: String): List<FloorHit> = when (asset) {
        "floor2.svg" -> floor2Hits()
        "floor3.svg" -> floor3Hits()
        "floor1.svg" -> floor1Hits()
        "floor_basement.svg" -> basementHits()
        else -> emptyList()
    }

    private fun floor2Hits(): List<FloorHit> {
        val pw = 3338f
        val ph = 6036f
        fun r(x: Int, y: Int, w: Int, h: Int, id: String) =
            FloorHit(id, x / pw, y / ph, (x + w) / pw, (y + h) / ph)
        return listOf(
            r(332, 87, 604, 897, "201"),
            r(326, 996, 610, 202, "202"),
            r(333, 1200, 602, 213, "203"),
            r(332, 1413, 602, 828, "204"),
            r(328, 2241, 608, 426, "205"),
            r(323, 2667, 613, 760, "206"),
            r(332, 3436, 604, 650, "207"),
            r(330, 4098, 604, 717, "208"),
            r(332, 4823, 602, 1031, "209"),
            r(1313, 748, 1984, 1540, "SPORT_HALL"),
            r(942, 5451, 363, 404, "WC_M2"),
            r(1311, 4744, 249, 452, "WC_W2"),
        )
    }

    private fun floor3Hits(): List<FloorHit> {
        val pw = 3336f
        val ph = 6041f
        fun r(x: Int, y: Int, w: Int, h: Int, id: String) =
            FloorHit(id, x / pw, y / ph, (x + w) / pw, (y + h) / ph)
        return listOf(
            r(288, 88, 552, 908, "301"),
            r(283, 1008, 560, 653, "302"),
            r(283, 1664, 557, 613, "303"),
            r(283, 2277, 557, 414, "304"),
            r(283, 2691, 557, 765, "305"),
            r(286, 3456, 557, 667, "306"),
            r(286, 4123, 554, 750, "307"),
            r(288, 4873, 555, 1025, "308"),
            r(843, 5500, 357, 389, "309"),
            r(1200, 4797, 234, 449, "310"),
        )
    }

    private fun floor1Hits(): List<FloorHit> {
        val pad = 480f // floor1.svg: translate(pad,0), ширина viewBox = 3340 + pad
        val pw = 3340f + pad
        val ph = 6030f
        fun r(x: Int, y: Int, w: Int, h: Int, id: String) =
            FloorHit(id, (x + pad) / pw, y / ph, (x + w + pad) / pw, (y + h) / ph)
        return listOf(
            r(1660, 54, 1344, 706, "111"),
            r(1464, 770, 1544, 581, "CONF_HALL"),
            r(1084, 1651, 1926, 686, "110"),
            r(1076, 57, 582, 714, "112"),
            r(0, 2296, 721, 423, "DIR"),
            r(0, 3015, 718, 297, "102"),
            r(0, 3311, 721, 232, "DEP_KH"),
            r(0, 3551, 720, 524, "103"),
            r(0, 4082, 722, 205, "104"),
            r(0, 4291, 720, 201, "105"),
            r(0, 4503, 722, 225, "106"),
            r(0, 4737, 720, 209, "107a"),
            r(0, 4954, 720, 983, "107"),
            r(722, 5529, 350, 414, "WC_M1"),
            r(1074, 4888, 209, 386, "WC_W1"),
            r(0, 721, 722, 283, "101"),
        )
    }

    private fun basementHits(): List<FloorHit> {
        val pw = 3340f
        val ph = 6030f
        fun r(x: Int, y: Int, w: Int, h: Int, id: String) =
            FloorHit(id, x / pw, y / ph, (x + w) / pw, (y + h) / ph)
        return listOf(
            r(282, 2283, 769, 495, "3"),
            r(276, 2783, 776, 812, "4"),
            r(282, 3595, 776, 591, "5"),
            r(282, 4192, 776, 700, "6"),
            r(279, 4901, 777, 849, "7"),
            r(1585, 699, 1734, 591, "WARDROBE"),
            r(1588, 1299, 1734, 390, "KITCHEN"),
            r(1585, 1689, 1734, 588, "CANTEEN"),
            r(1575, 4858, 700, 455, "UTILITY"),
        )
    }

    private val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU"))

    fun todayApiString(): String {
        val o = MAP_CALENDAR_OVERRIDE
        if (!o.isNullOrBlank()) return o
        return dateFmt.format(Date())
    }

    fun isWednesday(): Boolean {
        val c = Calendar.getInstance()
        val o = MAP_CALENDAR_OVERRIDE
        if (!o.isNullOrBlank()) {
            val p = o.split(".")
            if (p.size == 3) {
                c.clear()
                c.set(p[2].toInt(), p[1].toInt() - 1, p[0].toInt())
            }
        }
        return c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY
    }


    fun studentInitiativeLine(roomId: String): String? {
        if (!isWednesday()) return null
        return when (roomId) {
            "206" -> "ДСИ (среда, 4 пара): Лаборатория успеха — студенческая инициатива (Русанова А.О.)"
            "207" -> "ДСИ (среда, 4 пара): Хранители правопорядка (Нижников А.В.)"
            "SPORT_HALL" -> "ДСИ (среда, 4 пара): Коннект — игры (Загоруйко Н.Е.)"
            "110" -> "ДСИ (среда, 4 пара): Интеллектуальные игры (Ганихина М.А.)"
            "CONF_HALL" -> "ДСИ (среда, 4 пара): ВнеФормата (Крыгина Р.М.)"
            "209" -> "ДСИ: в 209 каб. — клуб «Вектор НППК» БПЛА только 15:00–16:00 (Андреев А.Б.). Остальные площадки ДСИ — 4 пара."
            else -> null
        }
    }


    fun conferenceExtraForDate(dateStr: String): String? {
        return when (dateStr) {
            "18.05.2026" -> "Международный день музеев — для всех групп."
            "19.05.2026" -> "День детских общественных организаций России — для 1–2 курса."
            else -> null
        }
    }


    fun conferenceHallEventsBlock(effectiveDateStr: String): String {
        val ref = try {
            dateFmt.parse(effectiveDateStr) ?: return ""
        } catch (_: Exception) {
            return ""
        }
        val lines = mutableListOf<String>()
        conferenceExtraForDate(effectiveDateStr)?.let { text ->
            lines += "• $effectiveDateStr\n  $text"
        }
        val knownDates = listOf("18.05.2026", "19.05.2026")
        for (d in knownDates) {
            if (d == effectiveDateStr) continue
            val dt = try {
                dateFmt.parse(d) ?: continue
            } catch (_: Exception) {
                continue
            }
            if (!dt.after(ref)) continue
            conferenceExtraForDate(d)?.let { text ->
                lines += "• $d\n  $text"
            }
        }
        if (lines.isEmpty()) return ""
        return lines.joinToString("\n\n")
    }

    /**

     */
    fun conferenceSoonReminderLine(): String? {
        val todayStr = todayApiString()
        val ref = try {
            dateFmt.parse(todayStr) ?: return null
        } catch (_: Exception) {
            return null
        }
        val knownDates = listOf("18.05.2026", "19.05.2026")
        for (d in knownDates) {
            val dt = try {
                dateFmt.parse(d) ?: continue
            } catch (_: Exception) {
                continue
            }
            val diffMs = dt.time - ref.time
            if (diffMs <= 0L) continue
            val days = diffMs / 86400000L
            if (days !in 1L..7L) continue
            if (conferenceExtraForDate(d) != null) {
                return "Скоро событие в колледже!"
            }
        }
        return null
    }

    suspend fun loadSchedule(apiRoomName: String, dateStr: String): Result<List<ApiLessonSlot>> =
        withContext(Dispatchers.IO) {
            try {
                val enc = URLEncoder.encode(apiRoomName, Charsets.UTF_8.name())
                val u = URL("${API_BASE}$enc?date=$dateStr")
                val conn = (u.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12_000
                    readTimeout = 12_000
                    requestMethod = "GET"
                }
                conn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    val json = reader.readText()
                    val type = object : TypeToken<List<ApiLessonSlot>>() {}.type
                    val list: List<ApiLessonSlot> = gson.fromJson(json, type) ?: emptyList()
                    Result.success(list.sortedBy { it.position })
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
