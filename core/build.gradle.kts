plugins {
    kotlin("jvm")
}

val gdxVersion: String by rootProject.extra

dependencies {
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.github.mgsx-dev.gdx-gltf:gltf:2.3.0")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}
