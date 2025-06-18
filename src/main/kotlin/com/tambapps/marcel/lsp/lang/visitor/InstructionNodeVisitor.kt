package com.tambapps.marcel.lsp.lang.visitor

import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor

interface InstructionNodeVisitor<T>: ExpressionNodeVisitor<T>, StatementNodeVisitor<T> {

  fun accept(astNode: AstNode) = when (astNode) {
    is ExpressionNode -> astNode.accept(this)
    is StatementNode -> astNode.accept(this)
    else -> throw IllegalArgumentException("Unknown AST node type ${astNode.javaClass.name}")
  }
}