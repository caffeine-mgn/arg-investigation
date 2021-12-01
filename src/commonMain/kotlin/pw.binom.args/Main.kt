package pw.binom.args

import pw.binom.Environment
import pw.binom.currentTimeMillis
import pw.binom.io.file.File
import pw.binom.io.file.relative
import pw.binom.io.file.rewrite

val argsDir = File("T:\\args")
fun main(args: Array<String>) {
    println("My program called!")
    println("Arguments [${args.size}]:")
    var i = 1
    for (str in args) {
        println("${i++}. \"$str\"")
    }

    val sb = StringBuilder()
    sb.appendLine("Arguments [${args.size}]:")
    args.forEachIndexed { index, s ->
        sb.appendLine(s)
    }

    argsDir
        .relative("run-${Environment.currentTimeMillis}.txt")
        .rewrite(sb.toString())
}