package pw.binom.args

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import pw.binom.ByteBuffer
import pw.binom.Input
import pw.binom.Output
import pw.binom.io.EOFException

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
        if (l <= 0) {
            throw EOFException()
        }
        buf.flip()
        rem -= output.write(buf)
    }
}