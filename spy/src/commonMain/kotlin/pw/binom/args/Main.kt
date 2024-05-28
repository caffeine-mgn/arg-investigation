package pw.binom.args

import pw.binom.*
import pw.binom.io.file.*
import pw.binom.url.isWildcardMatch

val STUB_IN_SPY_MAGIC_BYTES = byteArrayOf(0x33, 0x43, 0x54)

fun stub(mask: String, it: Sequence<File>) {
    it.forEach {
        if (it.name.startsWith("spy.")) {
            return@forEach
        }
        if (!it.isFile) {
            return@forEach
        }
        if (!it.name.isWildcardMatch(mask)) {
            return@forEach
        }
        val isExecuted = when {
            Environment.os == OS.WINDOWS -> it.extension?.lowercase() == "exe"
            else -> it.getPosixMode().isExecute
        }
        if (!isExecuted) {
            return@forEach
        }
        val newOriginalName = it.parent.relative("spy.${it.name}")
        if (newOriginalName.isFile) {
            return@forEach
        }
        println("Stub ${it.name} -> $newOriginalName")
        Stub.stubFile(
            config = ExecutionConfig(
                executeFile = newOriginalName.path,
                dirForLog = it.parent.relative("spy.logs").path,
            ),
            original = it
        )
    }
}

fun unstub(mask: String) {
    Environment.workDirectoryFile.list().forEach {
        if (it.name.startsWith("spy.")) {
            return@forEach
        }
        if (it.isFile && it.name.isWildcardMatch(mask)) {
            Stub.unstubFile(it)
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
            "stub", "-stub" -> stub(mask = it.next(), it = Environment.workDirectoryFile.walkDownSequence())
            "unstub", "-unstub" -> unstub(it.next())
            "help", "-help", "--help" -> help(it)
            else -> invalidCmd(cmd, it)
        }
    }
}
