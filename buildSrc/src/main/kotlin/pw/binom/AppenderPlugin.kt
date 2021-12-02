package pw.binom

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.jetbrains.kotlin.konan.target.presetName
import pw.binom.kotlin.clang.eachKotlinNativeLink

class AppenderPlugin : Plugin<Project> {
    override fun apply(target: Project) {
//        val kotlinExt = target.extensions.findByType(KotlinMultiplatformExtension::class.java)
        val stubProject = target.rootProject.project(":stub")
        val buildTask = target.tasks.findByName("build")!!
        target.tasks.eachKotlinNativeLink(release = true, debug = true, test = false) {
            val task = target.tasks.register("link${it.binary.buildType.name.toLowerCase().capitalize()}ExecutableWithStub${it.binary.target.konanTarget.presetName.capitalize()}", pw.binom.AppenderTask::class.java).get()
            task.spyTask.set(it)
            task.outputBinary.set(target.buildDir.resolve("withStub/${it.binary.target.konanTarget.presetName}/${it.binary.buildType.name.toLowerCase()}/${it.binary.outputFile.name}"))
            task.inputs.file(it.binary.outputFile)
            task.group = "build"
            task.dependsOn(it)
            buildTask.dependsOn(task)
            stubProject.tasks.whenTaskAdded { new ->
                if (new.isSame(it) && new.name == it.name) {
                    task.dependsOn(new)
                    task.stubTask.set(new)
                }
            }
        }
    }
}