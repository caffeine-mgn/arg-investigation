buildscript {

    repositories {
        mavenLocal()
        maven(url = "https://repo.binom.pw")
        mavenCentral()
        maven(url = "https://maven.google.com")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
        classpath("com.android.tools.build:gradle:7.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.9.24"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

repositories {
    mavenLocal()
    maven(url = "https://repo.binom.pw")
    mavenCentral()
}

val kotlinVersion = kotlin.coreLibrariesVersion
val ionspinBignumVersion = project.property("ionspin_bignum.version") as String
val kotlinxCoroutinesVersion = project.property("kotlinx_coroutines.version") as String
val kotlinxSerializationVersion = project.property("kotlinx_serialization.version") as String
val binomUuidVersion = project.property("binom_uuid.version") as String
val binomAtomicVersion = project.property("binom_atomic.version") as String
val binomBitArrayVersion = project.property("binom_bitarray.version") as String
val binomUrlVersion = project.property("binom_url.version") as String
val binomVersion = project.property("binom.version") as String

buildConfig {
    packageName(project.group.toString())
    buildConfigField("String", "KOTLIN_VERSION", "\"$kotlinVersion\"")
    buildConfigField("String", "IONSPIN_BIGNUM_VERSION", "\"$ionspinBignumVersion\"")
    buildConfigField("String", "KOTLINX_COROUTINES_VERSION", "\"$kotlinxCoroutinesVersion\"")
    buildConfigField("String", "KOTLINX_SERIALIZATION_VERSION", "\"$kotlinxSerializationVersion\"")
    buildConfigField("String", "BINOM_UUID_VERSION", "\"$binomUuidVersion\"")
    buildConfigField("String", "BITARRAY_VERSION", "\"$binomBitArrayVersion\"")
    buildConfigField("String", "BINOM_URL_VERSION", "\"$binomUrlVersion\"")
    buildConfigField("String", "ATOMIC_VERSION", "\"$binomAtomicVersion\"")
    buildConfigField("String", "BINOM_VERSION", "\"$binomVersion\"")
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    api("pw.binom:kn-clang:0.1.15")
}