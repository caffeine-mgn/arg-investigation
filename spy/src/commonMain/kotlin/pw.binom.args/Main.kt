package pw.binom.args

import pw.binom.Environment
import pw.binom.currentExecutionPath
import pw.binom.io.file.File
import pw.binom.io.file.name
import pw.binom.io.file.workDirectoryFile
import pw.binom.isWildcardMatch

//import pw.binom.*
//import pw.binom.concurrency.Worker
//import pw.binom.concurrency.create
//import pw.binom.date.Date
//import pw.binom.date.iso8601
//import pw.binom.io.file.*
//import pw.binom.io.use
//import pw.binom.process.Process
//import pw.binom.process.execute
//import pw.binom.process.exitProcess

val STUB_IN_SPY_MAGIC_BYTES = byteArrayOf(0x33, 0x43, 0x54)

fun stub(it: Iterator<String>) {
    if (!it.hasNext()) {
        throw IllegalArgumentException("Expected file mask")
    }
    val mask = it.next()
    Environment.workDirectoryFile.list().forEach {
        if (it.isFile && it.name.isWildcardMatch(mask)) {
            Stub.stubFile(it)
        }
    }
}

fun unstub(it: Iterator<String>) {
    if (!it.hasNext()) {
        throw IllegalArgumentException("Expected file mask")
    }
    val mask = it.next()
    Environment.workDirectoryFile.list().forEach {
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
            "stub", "-stub" -> stub(it)
            "unstub", "-unstub" -> unstub(it)
            "help", "-help", "--help" -> help(it)
            else -> invalidCmd(cmd, it)
        }
    }
}