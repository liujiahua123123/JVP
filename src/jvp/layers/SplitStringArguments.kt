package jvp.layers

import jvp.EncodeComponents
import jvp.Layer
import jvp.splitToInfixExpression
import org.mozilla.javascript.ast.*

class SplitStringArguments: Layer() {
    override fun invoke(root: AstRoot, components: EncodeComponents) {

        fun handleAstList(arguments:List<AstNode>):List<AstNode>{
            val newList = mutableListOf<AstNode>()

            arguments.forEach {
                if(it is StringLiteral){
                    newList.add(it.value.splitToInfixExpression())
                }else if(it is ArrayLiteral){
                    it.elements = handleAstList(it.elements)
                    newList.add(it)
                }else {
                    newList.add(it)
                }
            }

            return newList
        }

        if(components.preference.enableStringSplit) {
            root.visit { node ->
                if (node is FunctionCall) {
                    node.arguments = handleAstList(node.arguments)
                }
                true
            }
        }

    }
}