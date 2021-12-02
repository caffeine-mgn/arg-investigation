package pw.binom

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.FileOutputStream
import pw.binom.kotlin.clang.*
import java.io.FileInputStream

val MAGIC_BYTES = byteArrayOf(0x33, 0x43, 0x54)

abstract class AppenderTask : DefaultTask() {
    @get:Internal
    abstract val spyTask: Property<KotlinNativeLink>

    @get:Internal
    abstract val stubTask: Property<Task>

    @get:OutputFile
    abstract val outputBinary: RegularFileProperty

    @TaskAction
    fun execute() {
        println("Appending")
        val stubProject = project.rootProject.project(":stub")
        if (spyTask.get().project !== project) {
            throw RuntimeException("\"spyTask\" task should be from project ${project.path}. Now it is ${spyTask.get().project.path}:${spyTask.get().name}")
        }
        val stubTask=stubTask.get() as KotlinNativeLink
        if (stubTask.project !== stubProject) {
            throw RuntimeException("\"stubTask\" task should be from project ${stubProject.path}. Now it is ${stubTask.project.path}:${stubTask.name}")
        }

        FileOutputStream(outputBinary.get().asFile).use { to ->
            FileInputStream(spyTask.get().binary.outputFile).use { from ->
                from.copyTo(to)
            }
            FileInputStream(stubTask.binary.outputFile).use { from ->
                from.copyTo(to)
                val fromSize = stubTask.binary.outputFile.length()
                to.write(fromSize.dump())
                to.write(MAGIC_BYTES)
            }
        }
        println("File completed ${outputBinary.get().asFile}")
    }
}

fun Long.dump(): ByteArray {
    val output = ByteArray(Long.SIZE_BYTES)
    output[0] = ((this ushr (56 - 8 * 0)) and 0xFF).toByte()
    output[1] = ((this ushr (56 - 8 * 1)) and 0xFF).toByte()
    output[2] = ((this ushr (56 - 8 * 2)) and 0xFF).toByte()
    output[3] = ((this ushr (56 - 8 * 3)) and 0xFF).toByte()
    output[4] = ((this ushr (56 - 8 * 4)) and 0xFF).toByte()
    output[5] = ((this ushr (56 - 8 * 5)) and 0xFF).toByte()
    output[6] = ((this ushr (56 - 8 * 6)) and 0xFF).toByte()
    output[7] = ((this ushr (56 - 8 * 7)) and 0xFF).toByte()
    return output
}

fun Task.isSame(like:KotlinNativeLink):Boolean{
    if (this !is KotlinNativeLink){
        return false
    }

    return binary.target.konanTarget == like.binary.target.konanTarget && binary.buildType == like.binary.buildType
}

fun Project.findSameTarget(like: KotlinNativeLink): KotlinNativeLink? {
    var to: KotlinNativeLink? = null
    tasks.eachKotlinNativeLink(release = true, debug = true) {
        if (it.binary.target.konanTarget == like.binary.target.konanTarget && it.binary.buildType == like.binary.buildType) {
            to = it
        }
    }
    return to
}