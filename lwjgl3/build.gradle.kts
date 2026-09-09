plugins {
    kotlin("jvm")
    application
}

val gdxVersion: String by rootProject.extra

application { mainClass.set("com.raidmanager.lwjgl3.Lwjgl3LauncherKt") }

tasks.named<JavaExec>("run") {
    jvmArgs("-XstartOnFirstThread")
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    runtimeOnly("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

kotlin { jvmToolchain(17) }

tasks.register<JavaExec>("spritePreview") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.raidmanager.lwjgl3.SpritePreviewKt")
    jvmArgs("-XstartOnFirstThread")
}
