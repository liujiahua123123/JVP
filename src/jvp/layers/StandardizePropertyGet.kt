package jvp.layers

import jvp.EncodeComponents
import jvp.JsShield
import jvp.Layer
import org.mozilla.javascript.ast.*

class StandardizePropertyGet:Layer() {

    override fun invoke(root: AstRoot, components: EncodeComponents) {
        root.visit {
            val parent = it.parent
            if(it is PropertyGet){
                val origin = it.target
                if(origin is Name && JsShield.ONETAP_OBJECTS.contains(origin.identifier)) {
                    val methodNode = it.property

                    val methodName = methodNode.identifier

                    val elementGet = ElementGet(it.position)
                    elementGet.target = Name(0, origin.identifier)
                    elementGet.element = StringLiteral(1,methodName.length + 2).apply {
                        quoteCharacter = '\"'
                        value = methodName
                    }

                    elementGet.parent = parent

                    if (parent is FunctionCall) {
                        parent.target = elementGet
                    }
                    if (parent is ExpressionStatement) {
                        parent.expression = elementGet
                    }

                    log("Changing onetap method call $methodName from propertyGet to elementGet")
                }
            }
            true
        }
    }

}