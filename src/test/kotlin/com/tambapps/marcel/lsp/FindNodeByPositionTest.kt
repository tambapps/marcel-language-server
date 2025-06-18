package com.tambapps.marcel.lsp

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.lsp.lang.MarcelSemanticCompiler
import com.tambapps.marcel.lsp.lang.visitor.findByPosition
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class FindNodeByPositionTest : FunSpec({

    val semanticCompiler = MarcelSemanticCompiler(CompilerConfiguration(dumbbellEnabled = false))

    fun generateAst(text: String) = semanticCompiler.apply(text).ast!!.classes.first().methods.find { it.name == "run" }!!

    test("find variable reference") {
        val ast = generateAst("int a = 2\na = 6; null")

        for (column in 0..1) {
            val a = ast.blockStatement.findByPosition(1, column)
            a.shouldBeInstanceOf<VariableAssignmentNode>()
            a.variable.name shouldBe "a"
        }
    }

    test("find function reference") {
        val ast = generateAst("fun void foo() {}\n\nfoo(); null")

        for (column in 0..3) {
            val a = ast.blockStatement.findByPosition(2, column)
            a.shouldBeInstanceOf<FunctionCallNode>()
            a.javaMethod.name shouldBe "foo"
        }
    }
})
