package pw.binom.args

import pw.binom.*
import pw.binom.io.IOException
import pw.binom.io.file.*
import pw.binom.io.use

object Stub {
    private var isStubValid = false
    private var stubSize = 0L
    private val stubStart by lazy {
        stubSize - STUB_IN_SPY_MAGIC_BYTES.size - Long.SIZE_BYTES
    }

    private fun checkStub() {
        File(Environment.currentExecutionPath).openRead().use { exe ->
            exe.position = exe.size - STUB_IN_SPY_MAGIC_BYTES.size - Long.SIZE_BYTES
            stubSize = ByteBuffer.alloc(Long.SIZE_BYTES) { buf ->
                exe.read(buf)
                buf.flip()
                Long.fromBytes(buf)
            }
            val actualMagic = ByteBuffer.alloc(STUB_IN_SPY_MAGIC_BYTES.size) { buf ->
                exe.read(buf)
                buf.flip()
                val v = buf.toByteArray()
                v
            }

            actualMagic.forEachIndexed { index, byte ->
                if (byte != STUB_IN_SPY_MAGIC_BYTES[index]) {
                    throw RuntimeException("Spy doesn't have stub. Check build")
                }
            }
        }
    }

    fun unpack(dest: File) {
        if (dest.parent?.mkdirs() == null) {
            throw IOException("Can't create ${dest.parent}")
        }
        dest.openWrite().use { dest ->
            File(Environment.currentExecutionPath).openRead().use { exe ->
                var rem = stubSize
                exe.position = stubStart
                ByteBuffer.alloc(DEFAULT_BUFFER_SIZE) { buf ->
                    while (rem > 0) {
                        buf.clear()
                        buf.limit = minOf(buf.capacity, rem.toInt())
                        val l = exe.read(buf)
                        if (l <= 0) {
                            throw IOException("Can't unpack stub to $dest: Spy file is EOF")
                        }
                        buf.flip()
                        rem -= dest.write(buf)
                    }
                }
            }
        }
    }

    fun stubFile(original: File) {
        val stub = original.parent!!.relative("spy.${original.name}")
        original.renameTo(stub)
        unpack(original)
    }

    fun unstubFile(stub: File) {
        val originalFile = stub.parent!!.relative(stub.name.removePrefix("spy."))
        if (!originalFile.isFile) {
            throw IllegalStateException("Can't unstub \"$stub\". Original file \"$originalFile\" not found")
        }
        stub.delete()
        originalFile.renameTo(stub)
    }
}