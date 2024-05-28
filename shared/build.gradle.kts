import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import pw.binom.kotlin.clang.eachNative


plugins {
    id("org.jetbrains.kotlin.multiplatform")
    kotlin("plugin.serialization")
}

fun KotlinNativeTarget.config() {
    binaries {
        if (target.konanTarget==KonanTarget.MACOS_ARM64 || target.konanTarget==KonanTarget.MACOS_X64){
            framework()
        } else {
            staticLib()
        }
    }
}

kotlin {
    jvm()
    linuxX64()

    mingwX64()

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    linuxArm64()

    macosX64()
    eachNative {
        config()
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
//                api("org.jetbrains.kotlin:kotlin-stdlib-common:${pw.binom.Versions.KOTLIN_VERSION}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${pw.binom.Versions.KOTLINX_SERIALIZATION_VERSION}")
                api("pw.binom.io:core:${pw.binom.Versions.BINOM_VERSION}")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
//                api("org.jetbrains.kotlin:kotlin-stdlib:${pw.binom.Versions.KOTLIN_VERSION}")
            }
        }

        val jvmTest by getting {
            dependencies {
                api(kotlin("test-junit"))
            }
        }
    }
}