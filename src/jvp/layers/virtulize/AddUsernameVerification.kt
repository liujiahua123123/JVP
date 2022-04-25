package jvp.layers.virtulize

import jvp.*
import org.mozilla.javascript.ast.*

class AddUsernameVerification: Layer() {
    override fun invoke(root: AstRoot, components: EncodeComponents) {
        root.visit {
            if(it is ElementGet) {
                val elementGet = it
                val element = it.element
                if(element is VirtualizeFieldStringLiteral && !element.addUsernameVerify) {
                    element.addUsernameVerify = true
                    //10% true 90% false
                    if (Math.random() * 100 <= components.preference.virtualizeBindingPercentage) {
                        if (Math.random() > 0.9) {
                            val statement = components.usernameVerificationNodeFactory.createTrueStatement()
                            elementGet.element = conditionalStatement {
                                testExpression = statement
                                trueExpression = element
                                falseExpression =
                                    element.value.asVirtualizeFieldStringLiteral(element.realParent, true).apply {
                                        addUsernameVerify = true
                                    }
                            }
                        } else {
                            val statement = components.usernameVerificationNodeFactory.createFalseStatement()
                            elementGet.element = conditionalStatement {
                                testExpression = statement
                                trueExpression =
                                    element.value.asVirtualizeFieldStringLiteral(element.realParent, true).apply {
                                        addUsernameVerify = true
                                    }
                                falseExpression = element
                            }
                        }
                        log("Added username verification on virtualize field" + element.realParent + "." + element.value)
                    }
                }
            }
            true
        }
    }
}
