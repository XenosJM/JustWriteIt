plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP (Kotlin Symbol Processing) - Room이 코드 생성에 사용
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

android {
    namespace = "com.badger.justwriteit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.badger.justwriteit"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = false
    }
}

dependencies {


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.appcompat:appcompat:1.7.0")
    // Room Database - 핵심 3가지
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")  // Kotlin 확장 (코루틴 지원)
    ksp("androidx.room:room-compiler:$roomVersion")  // 코드 생성기
    // Lifecycle (ViewModel, LiveData)
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

    // RecyclerView - 리스트 표시용
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // 코루틴 - 비동기 작업
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // CardView 라이브러리 (레이아웃)
    implementation ("androidx.cardview:cardview:1.0.0")

    // Material Design 라이브러리
    implementation ("com.google.android.material:material:1.13.0")

    // 컬러 팔레트 라이브러리
    implementation("com.github.dhaval2404:colorpicker:2.3")
}