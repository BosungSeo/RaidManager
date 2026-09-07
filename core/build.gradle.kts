plugins {
    kotlin("jvm")
}

val gdxVersion: String by rootProject.extra

dependencies {
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}
