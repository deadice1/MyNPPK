plugins {
    // Делаем модуль библиотекой, а не отдельным приложением
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ru.filden"
    compileSdk = 36

    defaultConfig {
        // У библиотек нет собственного applicationId и versionCode/versionName –
        // всё это задаётся в базовом приложении.
        minSdk = 26
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.mysql.connector.j)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Используем алиасы из корневого catalogs (как в основном app)
    implementation("io.ktor:ktor-client-core:3.4.3")
    implementation("io.ktor:ktor-client-android:3.4.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.3")
    implementation("io.ktor:ktor-serialization-gson:3.4.3")
    implementation("io.ktor:ktor-client-logging:3.4.3")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.navigation.compose)
    implementation("com.google.code.gson:gson:2.14.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)

    // Общий UI-дизайн из модуля расписания
    implementation(project(":shared-ui"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}