plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.florent.location"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.florent.location"
        minSdk = 33
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${project.properties["SUPABASE_URL"]}\""
        )

        buildConfigField(
            "String",
            "SUPABASE_PUB_KEY",
            "\"${project.properties["SUPABASE_PUB_KEY"]}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11


    }
    buildFeatures {
        compose = true
        buildConfig = true
    }



}

kotlin {
    compilerOptions {
        allWarningsAsErrors.set(false)
        freeCompilerArgs.add(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose)
    //Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Supabase
    implementation(libs.postgrest.kt)
    implementation(libs.realtime.kt)
    implementation(libs.storage.kt)
    // Ktor (requis par Supabase)
    //noinspection UseTomlInstead
    implementation("io.ktor:ktor-client-android:3.4.0")
    // WorkManager pour la sync en arrière-plan
    implementation(libs.androidx.work.runtime.ktx)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation("io.mockk:mockk:1.13.12")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
