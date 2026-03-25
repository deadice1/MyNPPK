pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NPPK"
include(":app")

// ==========================================
// === ПОДКЛЮЧЕНИЕ МОДУЛЯ(Schedule) ===
// ==========================================

// 1. Главное приложение (App)
include(":schedule-app")
project(":schedule-app").projectDir = file("external_modules/Schedule/app")

// 2. Feature (путь исправлен на основе скриншота!)
include(":feature-schedule")
project(":feature-schedule").projectDir = file("external_modules/Schedule/feature/schedule")

// 3. Libs (путь подтвердился)
include(":libs-navigation")
project(":libs-navigation").projectDir = file("external_modules/Schedule/libs/navigation")

// 4. Shared (пути подтвердились)
include(":shared-date")
project(":shared-date").projectDir = file("external_modules/Schedule/shared/date")

include(":shared-group")
project(":shared-group").projectDir = file("external_modules/Schedule/shared/group")

include(":shared-schedule")
project(":shared-schedule").projectDir = file("external_modules/Schedule/shared/schedule")

include(":shared-ui")
project(":shared-ui").projectDir = file("external_modules/Schedule/shared/ui")

// ==========================================
// === ПОДКЛЮЧЕНИЕ МОДУЛЯ DutySchedule ===
// ==========================================

include(":duty-app")
project(":duty-app").projectDir = file("external_modules/DutySchedule/app")

// ==========================================
// === ПОДКЛЮЧЕНИЕ МОДУЛЯ Map (coll) ===
// ==========================================

include(":map-app")
project(":map-app").projectDir = file("external_modules/Map/app")