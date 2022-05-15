buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://repo.binom.pw")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}

plugins {
    kotlin("jvm") version "1.6.21"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://repo.binom.pw")
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.21")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    api("pw.binom:kn-clang:0.1")
}