package pw.binom.args

import pw.binom.Environment
import pw.binom.currentExecutionPath
import pw.binom.io.file.*
import pw.binom.isWildcardMatch

val STUB_IN_SPY_MAGIC_BYTES = byteArrayOf(0x33, 0x43, 0x54)

fun stub(it: Iterator<String>) {
    if (!it.hasNext()) {
        throw IllegalArgumentException("Expected file mask")
    }
    while (it.hasNext()) {
        val mask = it.next()
        Environment.workDirectoryFile.list().forEach {
            if (it.name.startsWith("spy.")) {
                return@forEach
            }
            if (it.isFile && it.name.isWildcardMatch(mask)) {
                val newOriginalName = it.parent!!.relative("spy.${it.name}")
                if (newOriginalName.isFile) {
                    return@forEach
                }
                println("Stub ${it.name} -> $newOriginalName")
                Stub.stubFile(
                    config = ExecutionConfig(
                        executeFile = newOriginalName.path,
                        dirForLog = it.parent!!.relative("spy.logs").path,
                    ),
                    original = it
                )
            }
        }
    }
}

fun unstub(it: Iterator<String>) {
    if (!it.hasNext()) {
        throw IllegalArgumentException("Expected file mask")
    }
    while (it.hasNext()) {
        val mask = it.next()
        Environment.workDirectoryFile.list().forEach {
            if (it.name.startsWith("spy.")) {
                return@forEach
            }
            if (it.isFile && it.name.isWildcardMatch(mask)) {
                Stub.unstubFile(it)
            }
        }
    }
}

fun help(it: Iterator<String>) {
    println("Usage ${File(Environment.currentExecutionPath).name} commands")
    println("  stub <wildcard> for create stub spy")
    println("  unstub <wildcard> for remove stub spy")
}

fun invalidCmd(cmd: String, it: Iterator<String>) {
    println("Unknown cmd \"$cmd\"")
    help(it)
}

fun main(args: Array<String>) {
    val it = args.iterator()
    while (it.hasNext()) {
        val cmd = it.next()
        when (cmd) {
            "stub", "-stub" -> stub(it)
            "unstub", "-unstub" -> unstub(it)
            "help", "-help", "--help" -> help(it)
            else -> invalidCmd(cmd, it)
        }
    }
}
