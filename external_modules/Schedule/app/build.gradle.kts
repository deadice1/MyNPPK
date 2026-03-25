import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// 1. Плагины
plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// 2. Логика переключения
val isLibraryMode = providers.gradleProperty("myLibraryMode").isPresent

if (isLibraryMode) {
    apply(plugin = "com.android.library")
} else {
    apply(plugin = "com.android.application")
}

// 3. Настройка Android
configure<BaseExtension> {
    namespace = "com.example.schedule"
    compileSdkVersion(36)

    defaultConfig {
        if (!isLibraryMode) {
            applicationId = "com.example.schedule"
        }
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures.compose = true

    sourceSets {
        getByName("main") {
            if (isLibraryMode) {
                manifest.srcFile("src/main/AndroidManifestLibrary.xml")
            } else {
                manifest.srcFile("src/main/AndroidManifest.xml")
            }
        }
    }
}

// 4. Настройка Kotlin (НОВЫЙ ВАРИАНТ для Kotlin 2.0)
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

// 5. Зависимости
dependencies {
    "implementation"(libs.androidx.core.ktx)
    "implementation"(libs.androidx.lifecycle.runtime.ktx)
    "implementation"(libs.androidx.activity.compose)
    "implementation"(platform(libs.androidx.compose.bom))
    "implementation"(libs.androidx.ui)
    "implementation"(libs.androidx.ui.graphics)
    "implementation"(libs.androidx.ui.tooling.preview)
    "implementation"(libs.material3)

    "testImplementation"(libs.junit)
    "androidTestImplementation"(libs.androidx.junit)
    "androidTestImplementation"(libs.androidx.espresso.core)
    "androidTestImplementation"(platform(libs.androidx.compose.bom))
    "androidTestImplementation"(libs.androidx.ui.test.junit4)
    "debugImplementation"(libs.androidx.ui.tooling)
    "debugImplementation"(libs.androidx.ui.test.manifest)
    "implementation"(libs.koin.android)

    "implementation"(project(":shared-date"))
    "implementation"(project(":shared-group"))
    "implementation"(project(":shared-schedule"))
    "implementation"(project(":shared-ui"))
    "implementation"(project(":libs-navigation"))
    "implementation"(project(":feature-schedule"))
}