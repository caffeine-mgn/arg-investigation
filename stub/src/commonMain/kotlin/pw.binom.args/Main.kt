package pw.binom.args

import pw.binom.*
import pw.binom.concurrency.Worker
import pw.binom.concurrency.create
import pw.binom.date.Date
import pw.binom.date.iso8601
import pw.binom.io.file.*
import pw.binom.io.use
import pw.binom.process.Process
import pw.binom.process.execute
import pw.binom.process.exitProcess

fun main(args: Array<String>) {
    val currentExe = File(Environment.currentExecutionPath)
    val originalFile = currentExe.parent!!.relative("spy." + currentExe.name)
    val currentOutputFolder = originalFile.parent!!.relative("spy.logs")
    val outFileName = "${currentExe.name}_${Date().iso8601().replace(':', '_')}.txt"
    val nameOfOutputFile = currentOutputFolder.relative(outFileName)
    currentOutputFolder.mkdirs()
    val sb = StringBuilder()
    sb.appendLine("Exe path: $currentExe")
    sb.appendLine("Arguments [${args.size}]:")
    args.forEachIndexed { index, s ->
        sb.appendLine(s)
    }

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
    val p = Process.execute(
            path = originalFile.path,
            args = args.toList(),
            env = Environment.getEnvs(),
            workDir = Environment.workDirectory,
    )
    startPrinter(p, from = p.stdout, to = Console.stdChannel)
    startPrinter(p, from = p.stderr, to = Console.errChannel)
    startPrinter(p, from = Console.inChannel, to = p.stdin)
    p.join()
    sb.appendLine()
    sb.appendLine("Exit Status: ${p.exitStatus}")
    nameOfOutputFile.rewrite(sb.toString())
    exitProcess(p.exitStatus)
}

private fun startPrinter(p: Process, from: Input, to: Output) {
    val w = Worker.create()
    w.execute {
        try {
            ByteBuffer.alloc(1) { buf ->
                while (p.isActive) {
                    buf.clear()
                    val l = from.read(buf)
                    if (l <= 0) {
                        break
                    }
                    buf.flip()
                    to.write(buf)
                }
            }
        } finally {
            w.requestTermination()
        }
    }
}

private inline fun Input.readByte2(buffer: ByteBuffer, ret: (Byte) -> Unit): Boolean {
    buffer.reset(0, 1)
    if (read(buffer) == 1) {
        buffer.flip()
        val value = buffer.get()
        ret(value)
        return true
    }
    return false
}

fun File.copy(to: File) {
    openRead().use { input ->
        to.openWrite().use { output ->
            ByteBuffer.alloc(DEFAULT_BUFFER_SIZE) { buf ->
                while (true) {
                    buf.clear()
                    val len = input.read(buf)
                    if (len <= 0) {
                        break
                    }
                    buf.flip()
                    output.write(buf)
                }
            }
        }
    }
}