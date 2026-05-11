# Структура папок и модулей

## Проект Хост (:app)
- `com.example.nppk.NPPKApp`: Инициализация системы.
- `com.example.nppk.NppkRoot`: Глобальный Scaffold, BottomBar и логика переключения "Вход/Приложение".
- `com.example.nppk.data.api`: Описание сетевых контрактов авторизации.
- `com.example.nppk.data.di`: Настройки Koin (NetworkModule, DataModule).
- `com.example.nppk.ui.screens`: Адаптеры, которые соединяют внешние модули с интерфейсом хоста.

## Внешние модули (external_modules/)
### 1. Schedule (Расписание)
- `shared/`: Чистая логика. Репозитории, UseCase-ы.
- `feature/`: Весь UI расписания на Compose.
- **Особенность**: Имеет два режима работы — для студентов и для преподавателей.

### 2. Map (Карта)
- Содержит интерактивные SVG-планы этажей.
- Интегрируется через `AndroidView` (взаимодействие Compose и старого XML Layout).

### 3. DutySchedule (Дежурства)
- Автономный модуль для управления графиком дежурных.

## Карта важных файлов
| Путь | Зачем нужен |
| --- | --- |
| `app/build.gradle.kts` | Главный файл сборки, здесь подключаются все `external_modules`. |
| `app/src/main/res/values/strings.xml` | Здесь меняется название приложения ("Навигатор НППК"). |
| `gradle/libs.versions.toml` | Единое место управления версиями библиотек (Retrofit, Koin и т.д.). |
