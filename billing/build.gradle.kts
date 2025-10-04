plugins {
    id("greenpass.lib-conventions")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.michaeltroger.gruenerpass.billing"
    buildFeatures.compose = true
    androidResources.enable = true
}

detekt {
    ignoreFailures = true
}

dependencies {
    implementation(project(":coroutines"))

    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.android.billing.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
}
