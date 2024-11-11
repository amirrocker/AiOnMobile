import de.undercouch.gradle.tasks.download.Download

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}


android {
    namespace = "de.ams.techday.aionmobilemediapipe"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.ams.techday.aionmobilemediapipe"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Set ASSET_DIR as an extra property
val ASSET_DIR = projectDir.resolve("src/main/assets").toString()
project.extensions.extraProperties["ASSET_DIR"] = ASSET_DIR

tasks.register<Download>("downloadModelFile0") {
    src("https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/float32/1/efficientdet_lite0.tflite")
    dest(file("${project.ext["ASSET_DIR"]}/efficientdet-lite0.tflite"))
    overwrite(false)
}

tasks.register<Download>("downloadModelFile1") {
    src("https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite2/float32/1/efficientdet_lite2.tflite")
    dest(file("${project.ext["ASSET_DIR"]}/efficientdet-lite2.tflite"))
    overwrite(false)
}

tasks.register<Download>("downloadModelFile2") {
    src("https://storage.googleapis.com/download.tensorflow.org/models/tflite/task_library/image_classification/android/mobilenet_v1_1.0_224_quantized_1_metadata_1.tflite")
    dest(file("${project.ext["ASSET_DIR"]}/mobilenetv1.tflite"))
    overwrite(false)
}

tasks.named("preBuild") {
    dependsOn(
        "downloadModelFile0",
        "downloadModelFile1",
        "downloadModelFile2",
    )
}

dependencies {

    implementation(platform(libs.kotlin.bom))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // first this
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    /* ** camera dependencies ** */
    // CameraX core library
    implementation(libs.androidx.camerax.core)

    // CameraX Camera2 extensions
    implementation(libs.androidx.camera2.ext)

    // CameraX Lifecycle library
    implementation(libs.androidx.camera.lifecycle)

    // CameraX View class
    implementation(libs.androidx.camera.view)

    implementation(libs.google.accompanist.permission)

    /* ** media pipe dependencies ** */

    // media pipe objectdetection
    implementation(libs.tasks.vision)

    // genai llm chat tasks
    implementation (libs.tasks.genai)

    // logging
    implementation(libs.timber)

    // exo player
    implementation(libs.exoplayer)

}
