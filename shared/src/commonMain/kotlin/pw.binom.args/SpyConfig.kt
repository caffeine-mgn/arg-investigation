@file:OptIn(ExperimentalSerializationApi::class)

package pw.binom.args

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import pw.binom.io.ByteBuffer
import pw.binom.io.EOFException
import pw.binom.io.Input
import pw.binom.io.Output

@Serializable
data class ExecutionConfig(
    val executeFile: String,
    val dirForLog: String?,
)

@Serializable
data class SpyConfig(
    val dirForLog: String?,
)

val protoBuf = ProtoBuf { }

fun Input.copyTo(output: Output, size: Long, buf: ByteBuffer) {
    var rem = size
    while (rem > 0) {
        buf.clear()
        buf.limit = minOf(buf.capacity, rem.toInt())
        val l = read(buf)
        if (l.isNotAvailable) {
            throw EOFException()
        }
        buf.flip()
        val w = output.write(buf)
        if (w.isAvailable){
            rem -= w.length
        }
    }
}
