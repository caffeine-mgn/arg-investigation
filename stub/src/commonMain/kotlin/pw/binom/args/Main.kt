package pw.binom.args

import pw.binom.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.date.DateTime
import pw.binom.date.iso8601
import pw.binom.io.*
import pw.binom.io.file.*
import pw.binom.process.ProcessStarter
import pw.binom.process.exitProcess
import pw.binom.thread.Thread

fun main(args: Array<String>) {
    val currentExe = File(Environment.currentExecutionPath)
    val spyConfig = currentExe.openRead().use { exe ->
        exe.position = exe.size - Int.SIZE_BYTES
        val infoSize = ByteBuffer(Int.SIZE_BYTES).use { buf ->
            exe.read(buf)
            buf.flip()
            Int.fromBytes(buf)
        }
        val offset = Int.SIZE_BYTES + infoSize
        exe.position = exe.size - offset
        val infoData = ByteBuffer(infoSize).use { infoBuffer ->
            ByteBuffer(DEFAULT_BUFFER_SIZE).use { buf ->
                exe.copyTo(infoBuffer, infoBuffer.capacity)
            }
            infoBuffer.flip()
            infoBuffer.toByteArray()
        }
        protoBuf.decodeFromByteArray(ExecutionConfig.serializer(), infoData)
    }
    val originalFile = File(spyConfig.executeFile)
    if (!originalFile.isFile) {
        Console.err.appendLine("Original File $originalFile not found")
        exitProcess(1)
        return
    }
    val currentOutputFolder = spyConfig.dirForLog?.let { File(it) } ?: originalFile.parent.relative("spy.logs")
    val outFileName = "${currentExe.name}_${DateTime.now.iso8601().replace(':', '_')}.txt"
    val nameOfOutputFile = currentOutputFolder.relative(originalFile.name).relative(outFileName)
    currentOutputFolder.mkdirs()
    val sb = StringBuilder()
    sb.appendLine("Exe path: $currentExe")
    sb.appendLine("Arguments [${args.size}]:")
    args.forEachIndexed { index, s ->
        sb.appendLine(s)
    }
    sb.appendLine("Environments:")
    Environment.getEnvs().forEach { (key, value) ->
        sb.appendLine("$key=$value")
    }
    sb.appendLine()
    sb.appendLine()
//    if (currentExe.nameWithoutExtension == "clang" || currentExe.nameWithoutExtension == "clang++") {
//        val i = args.iterator()
//        while (i.hasNext()) {
//            val e = i.next()
//            if (e == "-c") {
//                val source = File(i.next())
//                source.copy(currentOutputFolder.relative(source.name))
//            }
//        }
//    }
    val processBuilder = ProcessStarter.create(
        path = originalFile.path,
        args = args.toList(),
        envs = Environment.getEnvs(),
        workDir = Environment.workDirectory,
    )
    StreamReader(from = processBuilder.stdout, to = Console.stdChannel).use { stdout ->
        StreamReader(from = processBuilder.stderr, to = Console.errChannel).use { stderr ->
            StreamReader(from = Console.inChannel, to = processBuilder.stdin).use { stdin ->
                val process = processBuilder.start()
                stdout.start()
                sb.appendLine()
                sb.appendLine("Exit Status: ${process.exitStatus}")
                nameOfOutputFile.rewrite(sb.toString())
                exitProcess(process.exitStatus)
            }
        }
    }
}

class StreamReader(from: Input, to: Output) : Closeable {
    private val closed = AtomicBoolean(false)
    val thread = Thread {
        ByteBuffer(DEFAULT_BUFFER_SIZE).use { buf ->
            while (!closed.getValue()) {
                buf.clear()
                val l = from.read(buf)
                if (l.isNotAvailable) {
                    break
                }
                buf.flip()
                to.write(buf)
            }
        }
    }

    fun start() {
        thread.start()
    }

    override fun close() {
        closed.setValue(false)
    }
}

private inline fun Input.readByte2(buffer: ByteBuffer, ret: (Byte) -> Unit): Boolean {
    buffer.reset(0, 1)
    if (read(buffer).isAvailable) {
        buffer.flip()
        val value = buffer.getByte()
        ret(value)
        return true
    }
    return false
}

fun File.copy(to: File) {
    openRead().use { input ->
        to.openWrite().use { output ->
            ByteBuffer(DEFAULT_BUFFER_SIZE).use { buf ->
                while (true) {
                    buf.clear()
                    val len = input.read(buf)
                    if (len.isNotAvailable) {
                        break
                    }
                    buf.flip()
                    output.write(buf)
                }
            }
        }
    }
}
