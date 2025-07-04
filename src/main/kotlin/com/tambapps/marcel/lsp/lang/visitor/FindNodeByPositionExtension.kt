package com.tambapps.marcel.lsp.lang.visitor

import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.DupNode
import com.tambapps.marcel.semantic.ast.expression.ExprErrorNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.TernaryNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.ByteConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.NewArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.ShortConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.expression.operator.AndNode
import com.tambapps.marcel.semantic.ast.expression.operator.ArrayIndexAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.operator.BinaryOperatorNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.ElvisNode
import com.tambapps.marcel.semantic.ast.expression.operator.GeNode
import com.tambapps.marcel.semantic.ast.expression.operator.GtNode
import com.tambapps.marcel.semantic.ast.expression.operator.IncrNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeftShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.LtNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.ModNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.NotNode
import com.tambapps.marcel.semantic.ast.expression.operator.OrNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.ast.expression.operator.RightShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.BreakNode
import com.tambapps.marcel.semantic.ast.statement.ContinueNode
import com.tambapps.marcel.semantic.ast.statement.DoWhileNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode

fun ExpressionNode.findByPosition(line: Int, column: Int) = accept(FindNodeByTokenExtension(line, column))
fun StatementNode.findByPosition(line: Int, column: Int) = accept(FindNodeByTokenExtension(line, column))

class FindNodeByTokenExtension(
  private val line: Int,
  private val column: Int,
): InstructionNodeVisitor<ExpressionNode?> {
  override fun visit(node: ArrayAccessNode) = test(node) ?: node.indexNode.accept(this)

  override fun visit(node: ClassReferenceNode) = test(node)

  override fun visit(node: DupNode) = node.expression.accept(this)

  override fun visit(node: ExprErrorNode) = null

  // need to find the deepest elements first, then the shallow
  override fun visit(node: FunctionCallNode) = testExprs(node.arguments) ?: test(node)

  override fun visit(node: InstanceOfNode) = node.expressionNode.accept(this) ?: test(node)

  override fun visit(node: JavaCastNode) = node.expressionNode.accept(this)?: test(node)

  override fun visit(node: NewInstanceNode) = testExprs(node.arguments) ?: test(node)

  override fun visit(node: ReferenceNode) = test(node)

  override fun visit(node: StringNode) = test(node)

  override fun visit(node: SuperConstructorCallNode) = testExprs(node.arguments)?: test(node)

  override fun visit(node: SuperReferenceNode) = test(node)

  override fun visit(node: TernaryNode) = node.testExpressionNode.accept(this)
  ?: node.trueExpressionNode.accept(this)
  ?: node.falseExpressionNode.accept(this)
  ?: test(node)

  override fun visit(node: ThisConstructorCallNode) = testExprs(node.arguments)?: test(node)

  override fun visit(node: ThisReferenceNode) = test(node)

  override fun visit(node: ArrayNode) = testExprs(node.elements)?: test(node)

  override fun visit(node: BoolConstantNode) = test(node)

  override fun visit(node: ByteConstantNode) = test(node)

  override fun visit(node: CharConstantNode) = test(node)

  override fun visit(node: DoubleConstantNode) = test(node)

  override fun visit(node: FloatConstantNode) = test(node)

  override fun visit(node: IntConstantNode) = test(node)

  override fun visit(node: LongConstantNode) = test(node)

  override fun visit(node: MapNode) = testExprs(node.entries.flatMap { listOf(it.first, it.second) })

  override fun visit(node: NewArrayNode) = node.sizeExpr.accept(this) ?: test(node)

  override fun visit(node: NullValueNode) = test(node)

  override fun visit(node: ShortConstantNode) = test(node)

  override fun visit(node: StringConstantNode) = test(node)

  override fun visit(node: VoidExpressionNode) = test(node)

  override fun visit(node: AndNode) = test(node)

  override fun visit(node: ArrayIndexAssignmentNode) = node.owner.accept(this)
    ?: node.expression.accept(this)
    ?: node.indexExpr.accept(this)
    ?: test(node)

  override fun visit(node: DivNode) = test(node)

  override fun visit(node: ElvisNode) = test(node)

  override fun visit(node: GeNode) = test(node)

  override fun visit(node: GtNode) = test(node)

  override fun visit(node: IncrNode) = test(node)

  override fun visit(node: IsEqualNode) = test(node)

  override fun visit(node: IsNotEqualNode) = test(node)

  override fun visit(node: LeNode) = test(node)

  override fun visit(node: LeftShiftNode) = test(node)

  override fun visit(node: LtNode) = test(node)

  override fun visit(node: MinusNode) = test(node)

  override fun visit(node: ModNode) = test(node)

  override fun visit(node: MulNode) = test(node)

  override fun visit(node: NotNode) = test(node)

  override fun visit(node: OrNode) = test(node)

  override fun visit(node: PlusNode) = test(node)

  override fun visit(node: RightShiftNode) = test(node)

  override fun visit(node: VariableAssignmentNode) = node.expression.accept(this)?: test(node)

  override fun visit(node: BlockStatementNode) = testStmts(node.statements)

  override fun visit(node: BreakNode) = null

  override fun visit(node: ContinueNode) = null

  override fun visit(node: DoWhileNode) = node.condition.accept(this) ?: node.statement.accept(this)

  override fun visit(node: ExpressionStatementNode) = node.expressionNode.accept(this)

  override fun visit(node: ForInIteratorStatementNode) = node.iteratorExpression.accept(this)
    ?: node.nextMethodCall.accept(this)
    ?: node.bodyStatement.accept(this)

  override fun visit(node: ForStatementNode) = node.initStatement.accept(this) ?: node.condition.accept(this)
  ?: node.iteratorStatement.accept(this)
  ?: node.bodyStatement.accept(this)

  override fun visit(node: IfStatementNode) = node.conditionNode.accept(this)
    ?: node.trueStatementNode.accept(this)
    ?: node.falseStatementNode?.accept(this)

  override fun visit(node: ReturnStatementNode) = node.expressionNode.accept(this)

  override fun visit(node: ThrowNode) = node.expressionNode.accept(this)

  override fun visit(node: TryNode) = node.tryStatementNode.accept(this) ?: node.catchNodes.firstNotNullOfOrNull { it.statement.accept(this) }

  override fun visit(node: WhileNode) = node.condition.accept(this) ?: node.statement.accept(this)

  private fun <T: ExpressionNode> testExprs(nodes: List<T>) = nodes.firstNotNullOfOrNull { it.accept(this) }
  private fun <T: StatementNode> testStmts(nodes: List<T>) = nodes.firstNotNullOfOrNull { it.accept(this) }

  private fun test(node: BinaryOperatorNode) = node.leftOperand.accept(this) ?: node.rightOperand.accept(this)

  private fun test(node: ExpressionNode): ExpressionNode? {
    if (node.token.line == line
      && node.token.column <= column
      && node.token.column + node.token.end > column) {
      return node
    }
    return null
  }

}