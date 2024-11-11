import de.undercouch.gradle.tasks.download.Download

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.ams.techday.aionmobilelitert"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.ams.techday.aionmobilelitert"
        minSdk = 31
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
        kotlinCompilerExtensionVersion = "1.5.1"
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

tasks.register<Download>("downloadModelFileReinforcementLearning") {
    src("https://storage.googleapis.com/ai-edge/interpreter-samples/reinforcement_learning/android/planestrike_tf.tflite")
    dest(file("${project.ext["ASSET_DIR"]}/planestrike.tflite"))
    overwrite(false)
}

tasks.register<Download>("downloadModelFileSuperResolution") {
    src("https://storage.googleapis.com/ai-edge/interpreter-samples/super_resolution/android/ESRGAN.tflite")
    dest(file("${project.ext["ASSET_DIR"]}/ESRGAN.tflite"))
    overwrite(false)
}

tasks.register<Download>("downloadModelFileTextClassificationBert") {
    src("https://storage.googleapis.com/ai-edge/interpreter-samples/text_classification/android/bert_classifier.tflite")
    dest(file("${project.ext["ASSET_DIR"]}/mobile_bert.tflite"))
    overwrite(false)
}

tasks.register<Download>("downloadModelFileTextClassificationAvgWordVec") {
    src("https://storage.googleapis.com/ai-edge/interpreter-samples/text_classification/android/average_word_classifier.tflite")
    dest(file("${project.ext["ASSET_DIR"]}/word_vec.tflite"))
    overwrite(false)
}

tasks.register<Download>("downloadModelFileTextClassificationV2") {
    src("https://storage.googleapis.com/download.tensorflow.org/models/tflite/text_classification/text_classification_v2.tflite")
    dest(file("${project.ext["ASSET_DIR"]}/text_classification_v2.tflite"))
    overwrite(false)
}


tasks.named("preBuild") {
    dependsOn(
        "downloadModelFileReinforcementLearning",
        "downloadModelFileSuperResolution",
        "downloadModelFileTextClassificationBert",
        "downloadModelFileTextClassificationAvgWordVec",
        "downloadModelFileTextClassificationV2"
    )
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
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.androidx.navigation)

    // first this
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // change later when navigation is impl.

    // litert dependencies
    // needed for tensorflow lite models - now called litert
    implementation(libs.litert)
    implementation(libs.litert.support)
    implementation(libs.litert.metadata)

    // needed for super-resolution
    implementation(libs.litert.api)

    // helper dependency to easily load images
    implementation(libs.coil.compose)

    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}