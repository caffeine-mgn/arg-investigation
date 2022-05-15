import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    kotlin("plugin.serialization") version "1.5.31"
}

fun KotlinNativeTarget.config() {
    binaries {
        executable {
            entryPoint = "pw.binom.args.main"
        }
    }
}

kotlin {
    jvm()
    linuxX64 {
        config()
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp {
            config()
        }
    }

    mingwX64 {
        config()
    }
    if (pw.binom.Target.MINGW_X86_SUPPORT) {
        mingwX86 {
            config()
        }
    }

    androidNativeArm32 {
        config()
    }
    androidNativeArm64 {
        config()
    }
    androidNativeX86 {
        config()
    }
    androidNativeX64 {
        config()
    }
    linuxArm64 {
        config()
    }
    if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
        linuxArm32Hfp {
            config()
        }
    }

    macosX64 {
        config()
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib-common:${pw.binom.Versions.KOTLIN_VERSION}")
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

        val linuxX64Main by getting {
            dependencies {
                dependsOn(commonMain)
            }
        }
        val linuxX64Test by getting {
        }

        if (pw.binom.Target.LINUX_ARM32HFP_SUPPORT) {
            val linuxArm32HfpMain by getting {
                dependencies {
                    dependsOn(linuxX64Main)
                }
            }
        }

        val macosX64Main by getting {
            dependencies {
                dependsOn(linuxX64Main)
            }
        }

        val mingwX64Main by getting {
            dependencies {
                dependsOn(linuxX64Main)
            }
        }
        val androidNativeArm32Main by getting {
            dependencies {
                dependsOn(linuxX64Main)
            }
        }
        val androidNativeArm64Main by getting {
            dependencies {
                dependsOn(linuxX64Main)
            }
        }
        val androidNativeX86Main by getting {
            dependencies {
                dependsOn(linuxX64Main)
            }
        }
        val androidNativeX64Main by getting {
            dependencies {
                dependsOn(linuxX64Main)
            }
        }

        val mingwX64Test by getting {
            dependencies {
                dependsOn(linuxX64Test)
            }
        }

        if (pw.binom.Target.MINGW_X86_SUPPORT) {
            val mingwX86Main by getting {
                dependencies {
                    dependsOn(linuxX64Main)
                }
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
