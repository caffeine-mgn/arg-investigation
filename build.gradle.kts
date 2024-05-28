plugins {
    kotlin("plugin.serialization") version pw.binom.Versions.KOTLIN_VERSION apply false
}
allprojects {
    version = pw.binom.Versions.LIB_VERSION
    group = "pw.binom"

    repositories {
        mavenLocal()
        maven(url = "https://repo.binom.pw")
        mavenCentral()
    }
}