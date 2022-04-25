package jvp.layers

import jvp.*
import org.mozilla.javascript.ast.*

class RenameLocalDeclaration: Layer() {
    val PREFIX = "\$_JVP_"

    data class ReplacementInfo(val string:String, val type:String)

    val callbackObjects = listOf("Cheat","Globals")

    override fun invoke(root: AstRoot, components: EncodeComponents) {
        if(!(components.preference.enableLocalFunctionRename && components.preference.enableLocalVariableRename)){
            return
        }

        val localDeclaration = mutableMapOf<String, ReplacementInfo>()

        root.statements.forEach { node ->
            if(components.preference.enableLocalVariableRename) {
                if (node is VariableDeclaration) {
                    node.variables.forEach {
                        val target = it.target
                        if (target is Name) {
                            val currentName = target.identifier
                            val replacedName = PREFIX + replaceVarName(currentName)

                            target.identifier = replacedName
                            localDeclaration[currentName] = ReplacementInfo(replacedName, "Variable")
                            log("Replacing Top-level Variable Declaration of $currentName to $replacedName")
                        }
                    }
                }
            }
            if(components.preference.enableLocalFunctionRename) {
                if (node is FunctionNode) {
                    val functionName = node.functionName.identifier
                    val replacedFunctionName = PREFIX + replaceFunName(functionName)
                    localDeclaration[functionName] = ReplacementInfo(replacedFunctionName, "Function")
                    node.functionName.identifier = replacedFunctionName
                    log("Replacing Function Declaration of $functionName to $replacedFunctionName")
                }
            }
        }

        root.visit(object : NodeVisitor {
            fun handle(node: Name) {
                val refer = node.identifier
                val toReplaced = localDeclaration[refer]
                if (toReplaced != null) {
                    node.identifier = toReplaced.string
                    log("Replacing ${toReplaced.type} of $refer to ${toReplaced.string}")
                }
            }


            override fun visit(node: AstNode?): Boolean {
                if (node is Name) {
                    val parent = node.parent
                    if (parent is PropertyGet && parent.property == node) {
                        //ignore
                    } else {
                        handle(node)
                    }
                }
                return true
            }
        })


        if (components.preference.enableLocalFunctionRename) {
            //find onetap specific
            root.visit {
                if (it is FunctionCall) {
                    val target = it.target

                    if (it.arguments.size == 2 && target is PropertyGet || target is ElementGet) {
                        var targetObject = ""
                        var targetMethod = ""
                        if (target is PropertyGet) {
                            val targetObjectAst = target.target
                            if (targetObjectAst is Name) {
                                targetObject = targetObjectAst.identifier
                            }
                            targetMethod = target.property.identifier
                        }
                        if (target is ElementGet) {
                            val targetObjectAst = target.target
                            if (targetObjectAst is Name) {
                                targetObject = targetObjectAst.identifier
                            }
                            val targetMethodAst = target.element
                            if (targetMethodAst is StringLiteral) {
                                targetMethod = targetMethodAst.value
                            }
                        }
                        if (targetObject.isNotEmpty() && targetMethod.isNotEmpty() && callbackObjects.contains(
                                targetObject
                            ) && targetMethod == "RegisterCallback"
                        ) {
                            val callbackFunctionStack = it.arguments[1]
                            if (callbackFunctionStack !is StringLiteral) {
                                error("Found one Onetap callback function, but stack[1] is not StringLiteral")
                            }
                            val callbackFunctionName = callbackFunctionStack.value
                            val replacedData = localDeclaration[callbackFunctionName]
                            if (replacedData === null) {
                                error("Found one Onetap callback function, but no replacement found for $callbackFunctionName")
                            }
                            if (replacedData.type != "Function") {
                                error("Found one Onetap callback function, but origin declaration is not Function $callbackFunctionName")
                            }

                            val replacedFunctionName = replacedData.string

                            callbackFunctionStack.value = replacedFunctionName
                            log("Replacing Onetap Function Callback of $callbackFunctionName to $replacedFunctionName")
                        }
                    }
                }
                true
            }
        }
    }

}