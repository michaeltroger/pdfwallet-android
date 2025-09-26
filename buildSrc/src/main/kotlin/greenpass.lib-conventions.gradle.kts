plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    
    id("io.gitlab.arturbosch.detekt")
}

android {
    compileSdk = Versions.COMPILE_SDK
    androidResources.enable = false
    buildFeatures {
        buildConfig = false
        resValues = false
    }
    defaultConfig {
        minSdk = Versions.MIN_SDK
    }
    lint {
        warningsAsErrors = true
    }
}

kotlin {
    explicitApi()
    jvmToolchain(Versions.JAVA)
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

dependencies {
    implementation(libs.libHiltAndroid)
    ksp(libs.libHiltCompiler)
}
