package utils

import io.ktor.utils.io.core.*
import java.nio.charset.Charset
import kotlin.experimental.xor
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.text.String
import kotlin.text.toByteArray

class Kim64Key(
    val alphabet:String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
    val xorKey:ByteArray
){
    val inverseAlphabet: IntArray by lazy {
        IntArray(128) {
            alphabet.indexOf(it.toChar())
        }
    }

    init {
        check(alphabet.length == 64)
    }


    val exportable:String by lazy {
        String((alphabet + xorKey.toString(Charset.forName("UTF-8"))).toByteArray(Charset.forName("UTF-8")).encodeRailFence(), Charset.forName("UTF-8"))
    }

    companion object{
        private const val ALPHABET:String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        fun create(): Kim64Key = Kim64Key(
            alphabet = String(ALPHABET.toByteArray(Charset.forName("utf-8")).apply {shuffle()},charset = Charset.forName("utf-8")).take(64),
            xorKey = run {
                buildString {
                    repeat(16) {
                        append(ALPHABET.random())
                    }
                }.toByteArray(Charset.forName("utf-8"))
            }
        )
    }

}

class Kim64(val key: Kim64Key){

    fun encode(source:String) = encode(source.toByteArray(Charset.forName("utf-8")))

    fun encode(source: ByteArray):String{

        fun ByteArray.clearFrom(from: Int) {
            (from until size).forEach { this[it] = 0 }
        }

        fun Int.toBase64(): Char = key.alphabet[this]

        fun ByteReadPacket.encodeBase64(): String = buildString {
            val data = ByteArray(3)
            while (remaining > 0) {
                val read = readAvailable(data)
                data.clearFrom(read)

                val padSize = (data.size - read) * 8 / 6
                val chunk = ((data[0].toInt() and 0xFF) shl 16) or
                        ((data[1].toInt() and 0xFF) shl 8) or
                        (data[2].toInt() and 0xFF)

                for (index in data.size downTo padSize) {
                    val char = (chunk shr (6 * index)) and 0x3f
                    append(char.toBase64())
                }

            }
        }

        val size = source.size
        val mixed:ByteArray = if(key.xorKey.isEmpty()) {
            source
        }else{
            ByteArray(source.size).apply {
                repeat(size) {
                    val encoded = source[it] xor key.xorKey[(it % key.xorKey.size)]
                    this[it] = encoded
                }
            }
        }

        return ByteReadPacket(mixed).encodeBase64()
    }

    fun encodeSelfExplained(source: String):String = encodeSelfExplained(source.toByteArray(Charset.forName("utf-8")))

    fun encodeSelfExplained(source: ByteArray):String{
        return key.exportable + encode(source)
    }
}

fun ByteArray.encodeRailFence():ByteArray{
    val mixed = ByteArray(size)
    val half = size/2 + size%2
    repeat(size){
        mixed[if(it % 2 == 0){
            if(it == 0){0}else{it/2}
        }else{
            half +it/2
        }] = this[it]
    }
    return mixed
}


fun Int.randomString():String{
    return buildString {
        repeat(absoluteValue) {
            append(Random.nextInt(9))
        }
    }
}


