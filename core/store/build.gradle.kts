
plugins {
    id("com.omar.android.library")
    id("com.omar.android.hilt")
}

android {
    namespace = "com.omar.musica.store"
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.datastore)
    implementation(project(mapOf("path" to ":core:model")))
    implementation(project(mapOf("path" to ":core:database")))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}