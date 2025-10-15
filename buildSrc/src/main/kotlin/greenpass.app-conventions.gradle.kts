import com.github.jk1.license.filter.LicenseBundleNormalizer
import java.io.ByteArrayInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")

    id("com.github.jk1.dependency-license-report")
    id("io.gitlab.arturbosch.detekt")
}

android {
    compileSdk = Versions.COMPILE_SDK

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    lint {
        warningsAsErrors = true
        checkDependencies = true
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
        }
        release {
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            signingConfigs {
                create("release") {
                    try {
                        val keystoreProperties = Properties()
                        //keystoreProperties.load(ByteArrayInputStream(System.getenv("RELEASE_KEYSTORE_PROPERTIES").toByteArray()))
                        keystoreProperties.load(ByteArrayInputStream(rootProject.file("credentials/keystore.properties").readBytes()))
                        keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
                        keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
                        storeFile = rootProject.file("credentials/keystore.jks")
                        storePassword = keystoreProperties.getProperty("STORE_PASSWORD")
                    } catch(ignored: Exception) {
                        println("No signing configuration found, ignoring: $ignored")
                    }
                }
            }
            signingConfig = signingConfigs.getByName("release")
        }

        flavorDimensions += "variant"
        productFlavors {
            create("foss") {
                dimension = "variant"
            }
            create("play") {
                dimension = "variant"
            }
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

kotlin {
    jvmToolchain(Versions.JAVA)
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

licenseReport {
    outputDir = "$rootDir/docs/licenses"
    configurations = arrayOf("playReleaseRuntimeClasspath")
    filters = arrayOf(LicenseBundleNormalizer())
}

dependencies {
    coreLibraryDesugaring(libs.libDesugarJdkLibs)
    debugImplementation(libs.libLeakCanary)

    implementation(platform(libs.libKotlinBom))
    implementation(libs.libRoomKtx)
    implementation(libs.libHiltAndroid)

    ksp(libs.libRoomCompiler)
    ksp(libs.libHiltCompiler)
}
