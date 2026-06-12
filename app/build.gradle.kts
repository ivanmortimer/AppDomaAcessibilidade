plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.appdomaacessibilidade"
    compileSdk = 37 // <-- Valor alterado de 36 para 37. Sintaxe direta e limpa (i.e., sem usar a sintaxe "release(37) { minorApiLevel = 1 }")!

    defaultConfig {
        applicationId = "com.example.appdomaacessibilidade"
        minSdk = 30 // <-- Não mexa neste, deixe 30!
        targetSdk = 37 // <-- Atualizado para acompanhar o compileSdk
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}