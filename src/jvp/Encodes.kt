package jvp

import io.ktor.utils.io.core.*
import md5
import org.mozilla.javascript.ast.*
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.experimental.xor
import kotlin.text.toByteArray

fun String.jsEncode(const:Int = (1 .. 9).random(),writeRandomHead:Boolean = false):String{
    val input = this
    val split = "/x"
    return buildString {
        if(writeRandomHead) {
            append((1 .. 9).random())
        }else{
            append(const)
        }
        append(split)
        input.forEach {
            val total = it.toInt() + const
            val char1 = (Math.random() * total).toInt()
            val char2 = total - char1

            append(char1.toString(16))
            append(split)
            append(char2.toString(16))
            append(split)
        }
    }.removeSuffix(split)
}
fun List<String>.jsEncode():String{
    return this.map {it.jsEncode(this.size,true) }.joinToString("Username")
}
val magnifySplits = listOf(",",".","#")
val magnifyFills = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray()
fun String.magnifyEncode(sizeAtLeast:Int = 40):String{
    val fill = (sizeAtLeast - this.length).coerceAtMost(4)

    val leftFill:Int = (Math.random()*fill).toInt()
    val rightFill:Int = fill - leftFill

    val raw = this
    return buildString {
        append(leftFill)
        append(magnifySplits.random())
        append(raw.length)
        append(magnifySplits.random())
        repeat(leftFill){
            append(magnifyFills.random())
        }
        append(raw)
        repeat(rightFill){
            append(magnifyFills.random())
        }
    }
}

class JsXorStringEncoder(val alphabet: String, val xorKey:String ){

    fun encode(source:String) = encode(source.toByteArray(Charset.forName("utf-8")))

    fun encode(source: ByteArray):String{

        fun ByteArray.clearFrom(from: Int) {
            (from until size).forEach { this[it] = 0 }
        }

        fun Int.toBase64(): Char = alphabet[this]

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


                repeat(padSize){
                    append("=")
                }
            }
        }

        val size = source.size
        val mixed:ByteArray = if(xorKey.isEmpty()) {
            source
        }else{
            val array = xorKey.toCharArray().map {
                val thisA = it.toInt()
                when {
                    thisA > 127 -> {
                        127
                    }
                    thisA < -128 -> {
                        128
                    }
                    else -> {
                        thisA
                    }
                }
            }

            ByteArray(source.size).apply {
                repeat(size) {
                    val encoded = source[it] xor (array[it%array.size]).toByte()
                    this[it] = encoded
                }
            }
        }

        return ByteReadPacket(mixed).encodeBase64()
    }
}

fun String.toPrivateKey():String{

    fun String.md5(): String {
        try {
            val m = MessageDigest.getInstance("MD5")
            m.update(this.toByteArray(charset("UTF8")))
            val s = m.digest()
            var result = ""
            for (i in s.indices) {
                result += Integer.toHexString(0x000000FF and s[i].toInt() or -0x100).substring(6)
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    val string = this

    return buildString {
        val md5 = string.md5()
        append(md5.substring(0 .. 7).toUpperCase())
        append(md5.substring(8 ..15).toLowerCase())
        append(md5.substring(16 .. 23).toUpperCase())
        append(md5.substring(24 .. 31).toLowerCase())
    }
}



private val REGEX_X = Regex("x")
private val RANDOM_CHAR_CANDIDATES = arrayOf("a", "b", "c", "d", "e", "f", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9")


fun createUuid4(): String = //UUID.randomUUID().toString()
    "xxxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx".replace(REGEX_X) { RANDOM_CHAR_CANDIDATES.random() }

fun createUuid5(): String = //UUID.randomUUID().toString()
    "xxxxxxxx-xxxx-5555-xxxx-xxxxxxxxxxxx".replace(REGEX_X) { RANDOM_CHAR_CANDIDATES.random() }

fun createUuidForFunctionName(): String = //UUID.randomUUID().toString()
    "xxxxxxxx_xxxx_4xxx_xxxx_xxxxxxxxxxxx".replace(REGEX_X) { RANDOM_CHAR_CANDIDATES.random() }

fun createUuidForVariableName(): String = //UUID.randomUUID().toString()
    "xxxxxxxx_xxxx_4xxx_xxxx_xxxxxxxxxxxx".replace(REGEX_X) { RANDOM_CHAR_CANDIDATES.random() }


fun replaceFunName(origin:String):String{
    return (origin + "asncjn1jnejksacmnai").md5()!!
}

fun replaceVarName(origin:String):String{
    return (origin + "asncnejksacmnai").md5()!!
}



private val RANDOM_CHAR_CANDIDATES2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+=".toCharArray().map { it.toString() }

fun createUuidForVirtualization(): String = //UUID.randomUUID().toString()
    "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx".replace(REGEX_X) { RANDOM_CHAR_CANDIDATES2.random() }


//charCodeAt(0) == Number
//length == Number
//charAt(0) == 'a'

class UsernameVerificationNodeFactory(
    val originUsername:String
){

    data class TrueOrFalseAstNodes(val nodeForTrue: AstNode, val nodeForFalse: AstNode)

    companion object{
        val maps = listOf<(String) -> Pair<AstNode,TrueOrFalseAstNodes>>(
            {originName ->
                var wrongLength = -1
                while (wrongLength== -1 || wrongLength == originName.length){
                    wrongLength = (Math.random()*10 + 3).toInt()
                }

                Pair(elementGet {
                    target = functionCall {
                        target = elementGet("Cheat") {
                            element = "GetUsername".asVirtualizeFieldStringLiteral("Cheat") {
                                addUsernameVerify = true
                            }
                        }
                        arguments = listOf(
                            createUuid5().asLiteral {  }
                        )
                    }
                    element = "length".asLiteral()
                },TrueOrFalseAstNodes(originName.length.asLiteral(),wrongLength.asLiteral()))
            },
            {originName ->
                val detectCodeAt = (((originName.length/2) * Math.random()).toInt()*2)
                val rightChar = originName[detectCodeAt].toString()
                var wrongChar = rightChar

                while (wrongChar == rightChar){
                    wrongChar = "cegtxz039p".random().toString()
                }

                Pair(functionCall {
                    arguments = listOf(detectCodeAt.asLiteral())
                    target = elementGet{
                        element = "charAt".asLiteral {  }
                        target = functionCall {
                            target = elementGet("Cheat") {
                                element = "GetUsername".asVirtualizeFieldStringLiteral("Cheat") {
                                    addUsernameVerify = true
                                }
                            }
                            arguments = listOf(
                                createUuid5().asLiteral {  }
                            )
                        }
                    }
                },TrueOrFalseAstNodes(
                    rightChar.asLiteral {  },wrongChar.asLiteral{}))
            },
            {originName ->
                val detectCodeAt = (((originName.length/2) * Math.random()).toInt()*2)
                val rightCharCode = originName[detectCodeAt].toInt()
                var wrongCharCode = rightCharCode

                while (rightCharCode == wrongCharCode){
                    wrongCharCode = "cegtxz039p".random().toInt()
                }

                Pair(functionCall {
                    arguments = listOf(detectCodeAt.asLiteral())
                    target = elementGet{
                        element = "charCodeAt".asLiteral {  }
                        target = functionCall {
                            target = elementGet("Cheat") {
                                element = "GetUsername".asVirtualizeFieldStringLiteral("Cheat") {
                                    addUsernameVerify = true
                                }
                            }
                            arguments = listOf(
                                createUuid5().asLiteral {  }
                            )
                        }
                    }
                },TrueOrFalseAstNodes(
                    rightCharCode.asLiteral(),wrongCharCode.asLiteral()))
            }
       )

    }


    fun createTrueStatement():InfixExpression = createStatement(true)

    fun createFalseStatement():InfixExpression = createStatement(false)

    fun createStatement(trueOrFalse:Boolean):InfixExpression{
        val statementFactory = maps.random()
        val statement = statementFactory(originUsername)

        val firstElement = statement.first
        val secondElement = if(trueOrFalse){statement.second.nodeForTrue}else{statement.second.nodeForFalse}


        return if(Math.random() > 0.5) {
            equalStatement {
                left = firstElement
                right = secondElement
            }
        }else{
            equalStatement {
                left = secondElement
                right = firstElement
            }
        }
    }

}

fun String.splitToInfixExpression():InfixExpression{

    class MyStringLiteral: StringLiteral() {
        override fun toSource(depth: Int): String? {
            return StringBuilder(makeIndent(depth))
                .append("\'")
                .append(value)
                .append("\'")
                .toString()
        }
    }

    fun String.asMyStringLiteral(block: MyStringLiteral.() -> Unit):MyStringLiteral{
        val string = this
        return MyStringLiteral().apply {
            length = 2 + string.length
            quoteCharacter = '\"'
            value = string
            block(this)
        }
    }

    val stringReversedReader = this.reversed().toCharArray()
    var counter = 0


    fun readNext():String{
        return (if(counter < stringReversedReader.size) {
            var a = stringReversedReader[counter++].toInt().toString(16)
            if(a.length == 1){
                a = "0$a"
            }
            "\\x$a"
        }else{
            ' '
        }).toString()
    }

    fun internalBuilder(currentLeftChild: InfixExpression):InfixExpression{
        if(counter >= stringReversedReader.size){
            return currentLeftChild
        }
        return internalBuilder(
            infixExpression {
                left = readNext().asMyStringLiteral {
                    quoteCharacter = '\''
                }
                right = currentLeftChild
                operator = 21
            }
        )
    }

    return internalBuilder(
        infixExpression {
            right = readNext().asMyStringLiteral {
                quoteCharacter = '\''
            }
            left = readNext().asMyStringLiteral {
                quoteCharacter = '\''
            }
            operator = 21
        }
    )
}
