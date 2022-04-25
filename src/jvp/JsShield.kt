package jvp
import STATIC_FILES
import kotlinx.serialization.Serializable
import jvp.layers.virtulize.AddUsernameVerification
import jvp.layers.RenameLocalDeclaration
import jvp.layers.RenameScopeVariable
import jvp.layers.SplitStringArguments
import jvp.layers.StandardizePropertyGet
import jvp.layers.virtulize.AllocateVirtualizeField
import jvp.layers.virtulize.ExecuteVirtualization
import org.mozilla.javascript.CompilerEnvirons
import org.mozilla.javascript.Context
import org.mozilla.javascript.Node
import org.mozilla.javascript.Parser
import org.mozilla.javascript.ast.*
import org.mozilla.javascript.tools.ToolErrorReporter


fun jsParser(builder: (CompilerEnvirons) -> Unit = {}):Parser{
    val evn = CompilerEnvirons()
    evn.optimizationLevel = -1
    evn.isGeneratingSource = true
    evn.isGenerateDebugInfo = true
    evn.languageVersion = Context.VERSION_ES6

    builder(evn)
    return Parser(evn, ToolErrorReporter(true))
}

data class EncodeComponents(
    val stringXorEncoder:JsXorStringEncoder,
    val usernameVerificationNodeFactory: UsernameVerificationNodeFactory,
    val preference: EncodePreference
)


@Serializable
data class EncodePreference(
    val virtualizePercentage: Int,
    val virtualizeBindingPercentage: Int,
    val enableScopeRename:Boolean,
    val enableLocalFunctionRename:Boolean,
    val enableLocalVariableRename:Boolean,
    val enableStringSplit:Boolean,
    val username: String
)

interface EncodeLogger{
    fun log(any: Any?)
}

object JsShield {
    val ONETAP_OBJECTS = listOf("Globals","UI","Entity","Render","Convar","Event","Trace","UserCMD","Sound","Local","Cheat","Input","World","AntiAim","Exploit","Ragebot","Material","DataFile")

    val HEADER = STATIC_FILES.findFile("JS_HEADER.js").readText()


    fun encode(script:String, encodePreference: EncodePreference, encodeLogger: EncodeLogger):String{

        val component = EncodeComponents(
            JsXorStringEncoder("OaciHwDfsSN6+Be/d1xYhtGoyVLTCWvb4UZQX2nMmAjr7lE3Iu8RgpzJF5q0kKP9", encodePreference.username.toPrivateKey()),
            UsernameVerificationNodeFactory(encodePreference.username),
            encodePreference
        )


        val layers = listOf(StandardizePropertyGet(),RenameScopeVariable(), RenameLocalDeclaration(),SplitStringArguments(),
            AllocateVirtualizeField(), AddUsernameVerification(), ExecuteVirtualization()
        )

        val p = jsParser()

        val tree = p.parse(script, "JsTarget", 0)

        layers.forEach {
            it.logger = encodeLogger
            it(tree,component)
           // println("--------------------")
        }

        return HEADER + "\n" + tree.toSource()
    }
}

/**
 * convert the current string to ONE ast node for quick access
 */

fun String.toAstNode(): AstNode {
    return jsParser{}.parse(this,"JsFromString",0).statements[0]
}



interface ILayer{


    var logger:EncodeLogger?

    fun log(any:Any?){
        if(logger==null) {
            println(this.javaClass.simpleName + " : " + any)
        }else{
            logger!!.log(any)
        }
    }


    operator fun invoke(root:AstRoot, components: EncodeComponents)
}


abstract class Layer() :ILayer{
    override var logger: EncodeLogger? = null
}


fun expression(builder: ExpressionStatement.() -> Unit = {}):ExpressionStatement{
    return ExpressionStatement().apply(builder)
}

fun elementGet(targetObject:String,builder: ElementGet.() -> Unit = {}):ElementGet{
    return ElementGet().apply {
        this.target = Name(0,targetObject)
        builder(this)
    }
}

fun elementGet(builder: ElementGet.() -> Unit = {}):ElementGet{
    return ElementGet().apply {
        builder(this)
    }
}

fun propertyGet(builder: PropertyGet.() -> Unit = {}):PropertyGet{
    return PropertyGet().apply {
        builder(this)
    }
}

fun String.asLiteral(builder: StringLiteral.() -> Unit = {}):StringLiteral{
    val string = this
    return StringLiteral().apply {
        length = 2 + string.length
        quoteCharacter = '\"'
        value = string
        builder(this)
    }
}

fun String.asVirtualizeFieldStringLiteral(realParent: String, isFake: Boolean = false, builder: VirtualizeFieldStringLiteral.() -> Unit = {}):VirtualizeFieldStringLiteral{
    val string = this
    return VirtualizeFieldStringLiteral(realParent,this,isFake).apply {
        length = 2 + string.length
        quoteCharacter = '\"'
        value = string
        builder(this)
    }
}

fun String.asName(pos:Int = 0):Name = Name(pos,this)

fun Int.asLiteral():NumberLiteral{
    val int = this
    return NumberLiteral().apply {
        this.value = int.toString()
        this.number = int.toDouble()
    }

}


class VirtualizeFieldStringLiteral(
    val realParent: String,
    val realMethod: String,
    var isFake: Boolean,
    var addUsernameVerify: Boolean = false
): StringLiteral(){

    fun updateJSValue(to:String){
        length = 2 + to.length
        quoteCharacter = '\"'
        value = to
    }

}



fun assignment(builder: Assignment.() -> Unit = {}):Assignment{
    return Assignment().apply(builder)
}

fun functionCall(builder: FunctionCall.() -> Unit = {}):FunctionCall{
    return FunctionCall().apply(builder)
}

fun ifStatement(builder: IfStatement.() -> Unit = {}):IfStatement{
    return IfStatement().apply(builder)
}

fun conditionalStatement(builder: ConditionalExpression.() -> Unit = {} ):ConditionalExpression{
    return ConditionalExpression().apply(builder)
}

class EqualStatementBuilder{
    var left:AstNode? = null
    var right:AstNode? = null
}



//charCodeAt(0) == Number
//length == Number
//charAt(0) == 'a'


fun equalStatement(builder: EqualStatementBuilder.() -> Unit):InfixExpression{
    val builderInstance = EqualStatementBuilder()
    builder(builderInstance)
    return InfixExpression(builderInstance.left!!,builderInstance.right!!).apply {
        operator = 46
    }
}


fun infixExpression(builder: InfixExpression.() -> Unit):InfixExpression{
    return InfixExpression().apply(builder)
}


fun AstNode.anyMatch(predict: (Node) -> Boolean):Boolean{
    var shouldContinue = true
    this.visit {
        if(predict(it)){
            shouldContinue = false
        }
        shouldContinue
    }

    return !shouldContinue
}



