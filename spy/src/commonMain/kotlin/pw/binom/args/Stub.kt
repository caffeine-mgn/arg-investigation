package pw.binom.args

import pw.binom.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.file.*
import pw.binom.io.use as use1

object Stub {
    private val stubSize by lazy {
        File(Environment.currentExecutionPath).openRead().use1 { exe ->
            exe.position = exe.size - STUB_IN_SPY_MAGIC_BYTES.size - Long.SIZE_BYTES
            val stubSize = ByteBuffer(Long.SIZE_BYTES).use1 { buf ->
                exe.read(buf)
                buf.flip()
                Long.fromBytes(buf)
            }
            val actualMagic = ByteBuffer(STUB_IN_SPY_MAGIC_BYTES.size).use1 { buf ->
                exe.read(buf)
                buf.flip()
                buf.toByteArray()
            }

            actualMagic.forEachIndexed { index, byte ->
                if (byte != STUB_IN_SPY_MAGIC_BYTES[index]) {
                    throw RuntimeException("Spy doesn't have stub. Check build")
                }
            }
            stubSize
        }
    }
    private val stubStart by lazy {
        val spySize = File(Environment.currentExecutionPath).size
        spySize - stubSize - STUB_IN_SPY_MAGIC_BYTES.size - Long.SIZE_BYTES
    }

    private fun checkStub() {

    }

    fun unpack(config: ExecutionConfig, dest: File) {
        checkStub()
        if (dest.parent.mkdirs() == null) {
            throw IOException("Can't create ${dest.parent}")
        }
        dest.openWrite().use1 { dest ->
            File(Environment.currentExecutionPath).openRead().use1 { exe ->
                exe.position = stubStart
                ByteBuffer(DEFAULT_BUFFER_SIZE).use1 { buf ->
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
        val stub = original.parent.relative("spy.${original.name}")
        original.renameTo(stub)
        unpack(config = config, dest = original)
        original.setPosixMode(stub.getPosixMode())
    }

    fun unstubFile(stub: File) {
        val originalFile = stub.parent.relative("spy." + stub.name)
        if (!originalFile.isFile) {
            throw IllegalStateException("Can't unstub \"$stub\". Original file \"$originalFile\" not found")
        }
        stub.delete()
        originalFile.renameTo(stub)
    }
}
