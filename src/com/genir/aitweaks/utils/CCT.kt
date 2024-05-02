package com.genir.aitweaks.utils

import java.io.IOException
import java.lang.instrument.IllegalClassFormatException
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.min

class CCT(transforms: List<Transform>) {
    private val transforms: List<Transform> = if (transforms.javaClass == STATIC_ARRAY_LIST_TYPE) transforms else ArrayList(transforms)

    @Throws(IllegalClassFormatException::class)
    fun apply(data: ByteArray): ByteArray {
        try {
            val out: MutableList<Byte> = mutableListOf()
            val cpCount = readUnsignedShort(data, 8)
            var entrySize: Int
            var currentOffset = 10
            var lastIdx = 0
            var i = 1

            while (i < cpCount) {
                when (data[currentOffset]) {
                    CONSTANT_Utf8 -> {
                        val len = readUnsignedShort(data, currentOffset + 1)
                        entrySize = 3 + len
                        val fromIdx = currentOffset + 3
                        val toIdx = fromIdx + len

                        // println(data.slice(fromIdx..toIdx).toByteArray().decodeToString())

                        for (transform in transforms) {
                            val match = indexOf(data, transform.fromBytes, fromIdx, toIdx)
                            if (match != -1) {
                                val newBytes = if (match == 0 && len == transform.fromBytes.size) {
                                    transform.toBytes
                                } else {
                                    String(data, fromIdx, len, StandardCharsets.UTF_8).replace(transform.from, transform.to).toByteArray(StandardCharsets.UTF_8)
                                }

                                val newLen = newBytes.size
                                out.addAll(data.slice(lastIdx..currentOffset))
                                out.add(((newLen shr 8) and 0xFF).toByte())
                                out.add((newLen and 0xFF).toByte())
                                out.addAll(newBytes.toTypedArray())
                                lastIdx = toIdx
                                break
                            }
                        }
                    }

                    CONSTANT_Integer, CONSTANT_Float, CONSTANT_NameAndType, CONSTANT_Fieldref, CONSTANT_Methodref, CONSTANT_InterfaceMethodref, CONSTANT_Dynamic, CONSTANT_InvokeDynamic -> entrySize = 5

                    CONSTANT_Long, CONSTANT_Double -> {
                        entrySize = 9
                        i++
                    }

                    CONSTANT_Class, CONSTANT_String, CONSTANT_MethodType, CONSTANT_Module, CONSTANT_Package -> entrySize = 3

                    CONSTANT_MethodHandle -> entrySize = 4

                    else -> throw IllegalClassFormatException("Unknown constant tag " + data[currentOffset])
                }

                currentOffset += entrySize
                i++
            }

            out.addAll(data.slice(lastIdx until data.size))
            return out.toByteArray()
        } catch (e: IOException) {
            throw AssertionError("unreachable", e)
        }
    }

    class Transform internal constructor(from: String, to: String) {
        val from: String = Objects.requireNonNull(from)
        val fromBytes: ByteArray = from.toByteArray(StandardCharsets.UTF_8)
        val to: String = Objects.requireNonNull(to)
        val toBytes: ByteArray = to.toByteArray(StandardCharsets.UTF_8)

        override fun toString(): String {
            return "Transform{from='$from', to='$to'}"
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val transform = o as Transform
            return from == transform.from && to == transform.to
        }

        override fun hashCode(): Int {
            return Objects.hash(from, to)
        }
    }

    companion object {
        private val STATIC_ARRAY_LIST_TYPE = mutableListOf(true, false).javaClass as Class<out MutableList<*>?>

        fun newTransform(from: String, to: String): Transform {
            return Transform(from, to)
        }

        @Throws(IOException::class)
        fun readClassBuffer(cl: ClassLoader, className: String): ByteArray {
            val classPath = className.replace('.', '/') + ".class"
            val stream = cl.getResourceAsStream(classPath)

            var size = 0
            var buffer = ByteArray(1024)
            while (stream.available() > 0) {
                size += stream.read(buffer, size, buffer.size - size)
                if (size == buffer.size) {
                    buffer += ByteArray(buffer.size)
                }
            }

            val classData = buffer.sliceArray(IntRange(0, size - 1))

            return classData
        }

        private fun readUnsignedShort(buf: ByteArray, offset: Int): Int {
            return ((buf[offset].toInt() and 0xFF) shl 8) or (buf[offset + 1].toInt() and 0xFF)
        }

        private fun indexOf(src: ByteArray, tgt: ByteArray, fromIndex: Int, toIndex: Int): Int {
            val srcCount = min(src.size.toDouble(), toIndex.toDouble()).toInt()
            val tgtCount = tgt.size
            val first = tgt[0]
            val max = (srcCount - tgtCount)
            var i = fromIndex
            while (i <= max) {
                // Look for first byte.
                if (src[i] != first) {
                    while (++i <= max && src[i] != first); /*continue*/
                }
                // Found first byte, now look at the rest of the sequence
                if (i <= max) {
                    var j = i + 1
                    val end = j + tgtCount - 1
                    var k = 1
                    while (j < end && src[j] == tgt[k]) {/*continue*/
                        j++
                        k++
                    }
                    if (j == end) {
                        // Found whole sequence.
                        return i
                    }
                }
                i++
            }
            return -1
        }

        private const val CONSTANT_Utf8: Byte = 1
        private const val CONSTANT_Integer: Byte = 3
        private const val CONSTANT_Float: Byte = 4
        private const val CONSTANT_Long: Byte = 5
        private const val CONSTANT_Double: Byte = 6
        private const val CONSTANT_Class: Byte = 7
        private const val CONSTANT_String: Byte = 8
        private const val CONSTANT_Fieldref: Byte = 9
        private const val CONSTANT_Methodref: Byte = 10
        private const val CONSTANT_InterfaceMethodref: Byte = 11
        private const val CONSTANT_NameAndType: Byte = 12
        private const val CONSTANT_MethodHandle: Byte = 15
        private const val CONSTANT_MethodType: Byte = 16
        private const val CONSTANT_Dynamic: Byte = 17
        private const val CONSTANT_InvokeDynamic: Byte = 18
        private const val CONSTANT_Module: Byte = 19
        private const val CONSTANT_Package: Byte = 20
    }
}