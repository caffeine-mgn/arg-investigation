import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import pw.binom.kotlin.clang.eachNative

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    linuxX64()
    mingwX64()
    linuxArm64()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    macosX64()
    macosArm64()

    eachNative {
        binaries {
            executable {
                entryPoint = "pw.binom.args.main"
            }
        }
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
//                api("org.jetbrains.kotlin:kotlin-stdlib-common:${pw.binom.Versions.KOTLIN_VERSION}")
                api("pw.binom.io:file:${pw.binom.Versions.BINOM_VERSION}")
                api(project(":shared"))
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
                api("org.jetbrains.kotlin:kotlin-stdlib:${pw.binom.Versions.KOTLIN_VERSION}")
            }
        }

        val jvmTest by getting {
            dependencies {
                api(kotlin("test-junit"))
            }
        }
    }
}

apply<pw.binom.AppenderPlugin>()
