plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")
    defaultConfig {
        applicationId = "io.github.thanosfisherman.blueflow.sample"
        minSdkVersion(15)
        targetSdkVersion(29)
        versionCode = Artifact.versionCode
        versionName = Artifact.artifactVersion
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


dependencies {
    implementation(project(":blueflowlib"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.KOTLIN}")
    implementation("androidx.appcompat:appcompat:${Versions.AppCompat}")
    implementation("androidx.core:core-ktx:${Versions.KTX}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Coroutines}")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}
