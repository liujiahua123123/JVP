package jvp.layers.virtulize

import jvp.*
import org.mozilla.javascript.ast.*
import kotlin.random.Random

class ExecuteVirtualization: Layer() {
    val PARENT = "Cheat"
    private val INITIALIZER_INDEX = "\$_JVP_initVirtualEnv"


    override fun invoke(root: AstRoot, components: EncodeComponents) {

        val virtualizedCalls = mutableMapOf<String, VirtualizeFieldStringLiteral>()

        root.visit {node ->
            if(node is VirtualizeFieldStringLiteral && !node.isFake){
                val newValue = createUuidForVirtualization()
                virtualizedCalls[newValue] = node
                node.updateJSValue(newValue)
            }
            true
        }

        root.visit {node ->
            if(node is VirtualizeFieldStringLiteral && node.isFake){
                val fakeValue = virtualizedCalls.keys.random()
                node.updateJSValue(fakeValue)
            }
            true
        }


        val function = "function $INITIALIZER_INDEX(){}".toAstNode() as FunctionNode
        val functionBody = function.body as Block
        val caller = "$INITIALIZER_INDEX()".toAstNode() as ExpressionStatement

        log("Writing Virtualize Data")


        val fixedRandom = Random(1234567890)
        val statements = mutableListOf<ExpressionStatement>()

        virtualizedCalls.forEach { (virtualizedName, data) ->

            /*
            val statement = "$PARENT[\"$virtualizedName\"] = Something[\"Target\"]".toAstNode() as ExpressionStatement
             */

            val encodedReference = components.stringXorEncoder.encode(data.realMethod.magnifyEncode().jsEncode())
            val encodedMyPrivateKeyReference = "\$_JVP_privateKey".jsEncode()

            val statement = expression {
                this.expression = assignment {
                    operator = 91
                    if(fixedRandom.nextDouble() * 100 <= components.preference.virtualizePercentage) {
                        left = elementGet(PARENT) {
                            element = functionCall {
                                target = elementGet(PARENT) {
                                    element = "\$_JVP_decodeString".asLiteral()
                                }
                                arguments = listOf(
                                    virtualizedName.jsEncode().asLiteral(),
                                    createUuid5().asLiteral(),
                                    elementGet(PARENT) {
                                        element = "GetUsername".asLiteral()
                                    },
                                    functionCall {
                                        target = elementGet(PARENT) {
                                            element = "GetUsername".asLiteral()
                                        }
                                        arguments = listOf()
                                    }
                                )
                            }
                        }
                        right = elementGet(data.realParent) {
                            element = functionCall {
                                target = elementGet(PARENT) {
                                    element = "\$_JVP_decodeMagnifyString".asLiteral()
                                }
                                arguments = listOf(
                                    functionCall {
                                        target = elementGet(PARENT) {
                                            element = "\$_JVP_decodeString".asLiteral()
                                        }
                                        arguments = listOf(
                                            functionCall {
                                                target = elementGet(PARENT) {
                                                    element = "\$_JVP_decodeKim64".asLiteral()
                                                }
                                                arguments = listOf(
                                                    encodedReference.asLiteral(),
                                                    elementGet(PARENT) {
                                                        element = functionCall {
                                                            target = elementGet(PARENT) {
                                                                element = "\$_JVP_decodeString".asLiteral()
                                                            }
                                                            arguments = listOf(
                                                                encodedMyPrivateKeyReference.asLiteral()
                                                            )
                                                        }
                                                    },
                                                    createUuid4().asLiteral(),
                                                    functionCall {
                                                        target = elementGet(PARENT) {
                                                            element = "GetUsername".asLiteral()
                                                        }
                                                        arguments = listOf()
                                                    }
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }else{
                        left = elementGet(PARENT) {
                            element = virtualizedName.splitToInfixExpression()
                        }
                        right = elementGet(data.realParent) {
                            element = data.realMethod.splitToInfixExpression()
                        }
                    }
                }
            }

           statements.add(statement)
        }

        statements.shuffle()
        statements.forEach {
            functionBody.addStatement(it)
        }


        root.addChildToFront(caller)
        root.addChildToFront(function)
    }
}