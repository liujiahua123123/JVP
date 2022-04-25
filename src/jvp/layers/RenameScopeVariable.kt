package jvp.layers

import jvp.EncodeComponents
import jvp.Layer
import jvp.createUuidForVariableName
import jvp.replaceVarName
import org.mozilla.javascript.ast.*

class RenameScopeVariable: Layer() {
    override fun invoke(root: AstRoot, components: EncodeComponents) {

        fun handleBlock(block:Block){
            val names = mutableMapOf<String,String>()
            block.visit {
                if(it is VariableDeclaration){
                    it.variables.forEach {vi ->
                        val target = vi.target
                        if(target is Name){
                            val newName = "LOCAL_" + replaceVarName(target.identifier)
                            names[target.identifier] = newName
                            target.identifier = newName
                         }
                    }
                }
                true
            }

            root.visit{node ->
                if(node is Name){
                    val parent =  node.parent
                    if(parent is PropertyGet && parent.property == node){
                        //ignore
                    }else{
                        val newName = names[node.identifier]
                        if(newName!=null) {
                            node.identifier = newName
                        }
                    }
                }
                true
            }
        }

        if(components.preference.enableScopeRename) {
            root.visit {
                if (it is Block) {
                    handleBlock(it)
                    false
                } else {
                    true
                }
            }
        }
    }
}