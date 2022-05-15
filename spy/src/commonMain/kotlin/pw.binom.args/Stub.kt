package pw.binom.args

import pw.binom.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.file.*
import pw.binom.io.use

object Stub {
    private var isStubValid = false
    private var stubSize = 0L
    private val stubStart by lazy {
        val spySize = File(Environment.currentExecutionPath).size
        spySize - stubSize - STUB_IN_SPY_MAGIC_BYTES.size - Long.SIZE_BYTES
    }

    private fun checkStub() {
        File(Environment.currentExecutionPath).openRead().use { exe ->
            exe.position = exe.size - STUB_IN_SPY_MAGIC_BYTES.size - Long.SIZE_BYTES
            stubSize = ByteBuffer.alloc(Long.SIZE_BYTES).use { buf ->
                exe.read(buf)
                buf.flip()
                Long.fromBytes(buf)
            }
            val actualMagic = ByteBuffer.alloc(STUB_IN_SPY_MAGIC_BYTES.size).use { buf ->
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

    fun unpack(config: ExecutionConfig, dest: File) {
        checkStub()
        if (dest.parent?.mkdirs() == null) {
            throw IOException("Can't create ${dest.parent}")
        }
        dest.openWrite().use { dest ->
            File(Environment.currentExecutionPath).openRead().use { exe ->
                exe.position = stubStart
                ByteBuffer.alloc(DEFAULT_BUFFER_SIZE).use { buf ->
                    exe.copyTo(dest, stubSize, buf)

                    val configData = protoBuf.encodeToByteArray(ExecutionConfig.serializer(), config)
                    buf.clear()
                    buf.write(configData)
                    buf.writeInt(configData.size)
                    buf.flip()
                    dest.write(buf)
                }
            }
        }
    }

    fun stubFile(config: ExecutionConfig, original: File) {
        val stub = original.parent!!.relative("spy.${original.name}")
        original.renameTo(stub)
        unpack(config = config, dest = original)
    }

    fun unstubFile(stub: File) {
        val originalFile = stub.parent!!.relative("spy." + stub.name)
        if (!originalFile.isFile) {
            throw IllegalStateException("Can't unstub \"$stub\". Original file \"$originalFile\" not found")
        }
        stub.delete()
        originalFile.renameTo(stub)
    }
}
