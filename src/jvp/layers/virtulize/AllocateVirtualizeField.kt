package jvp.layers.virtulize

import jvp.*
import org.mozilla.javascript.ast.AstRoot
import org.mozilla.javascript.ast.ElementGet
import org.mozilla.javascript.ast.Name
import org.mozilla.javascript.ast.StringLiteral

class AllocateVirtualizeField:Layer(){
    override fun invoke(root: AstRoot, components: EncodeComponents) {
        root.visit {node ->
            if(node is ElementGet){
                val target = node.target
                if(target is Name && JsShield.ONETAP_OBJECTS.contains(target.identifier)){
                    val methodAst = node.element
                    if(methodAst is StringLiteral) {
                        val originFrom = target.identifier
                        val currentName = methodAst.value
                        target.identifier = "Cheat"
                        node.element = currentName.asVirtualizeFieldStringLiteral(originFrom) {position = 1}
                        log("Allocated Virtualize Field: $originFrom.$currentName")
                    }
                }
            }
            true
        }
    }
}