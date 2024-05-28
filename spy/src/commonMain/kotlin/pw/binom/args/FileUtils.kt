package pw.binom.args

import pw.binom.collections.LinkedList
import pw.binom.io.file.File

class FileWalkerIterator(root: File) : Iterator<File> {
    private val needVisit = LinkedList<File>()

    init {
        needVisit += root
    }

    override fun hasNext(): Boolean = needVisit.isNotEmpty()

    override fun next(): File {
        while (true) {
            val item = needVisit.removeFirstOrNull() ?: throw NoSuchElementException()
            if (item.isDirectory) {
                item.list().forEach {
                    needVisit.addLast(it)
                }
                return item
            }
            if (item.isFile) {
                return item
            }
        }
    }
}

class FileWalterSequence(private val root: File) : Sequence<File> {
    override fun iterator(): Iterator<File> = FileWalkerIterator(root)
}

fun File.walkDownSequence() = FileWalterSequence(this)