plugins {
    kotlin("jvm") version "2.0.21" apply false
    kotlin("android") version "2.0.21" apply false
    id("com.android.application") version "8.7.3" apply false
}

allprojects {
    group = "com.raidmanager"
    version = "1.0.0"
}

extra["gdxVersion"] = "1.13.5"

