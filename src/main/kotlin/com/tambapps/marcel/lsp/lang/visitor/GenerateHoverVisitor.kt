package com.tambapps.marcel.lsp.lang.visitor

import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ConditionalExpressionNode
import com.tambapps.marcel.semantic.ast.expression.DupNode
import com.tambapps.marcel.semantic.ast.expression.ExprErrorNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
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
import com.tambapps.marcel.semantic.ast.expression.YieldExpression
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
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.method.MethodParameter
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelField
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind

class GenerateHoverVisitor: ExpressionNodeVisitor<MarkupContent?> {
  override fun visit(node: ArrayAccessNode): MarkupContent? {
    return null
  }

  override fun visit(node: ClassReferenceNode): MarkupContent? {
    return text(node.classType.toString())
  }

  override fun visit(node: DupNode): MarkupContent? {
    return null
  }

  override fun visit(node: ExprErrorNode): MarkupContent? {
    return null
  }

  override fun visit(node: FunctionCallNode): MarkupContent? {
    val method = node.javaMethod
    return markup(StringBuilder().apply {
      if (method.isMarcelStatic) append("static ")
      if (method.isAsync) append("async ")
      if (method.isAbstract && !method.ownerClass.isInterface) append("abstract ")
      append("fun ")
      append(method.returnType.simpleName)
      append(" **")
      append(method.name)
      append("**")
      appendParameters(method.parameters, this)

      append("\n")
      append(if (method.isExtension) "Extension method from " else "Method from ")
      append(method.ownerClass)
      if (method.isExtension) {
        method.ownerClass
      }
    }.toString())
  }

  private fun appendParameters(parameters: List<MethodParameter>, buffer: StringBuilder) {
    parameters.joinTo(buffer, separator = ", ", transform = this@GenerateHoverVisitor::transformMethodParameter, prefix = "(", postfix = ")")
  }
  private fun transformMethodParameter(p: MethodParameter) = StringBuilder().apply {
    append(p.type.simpleName)
    append(" ")
    append(p.name)
    if (p.defaultValue != null) {
      append(" = ")
      append(p.defaultValue)
    }
  }.toString()

  override fun visit(node: InstanceOfNode): MarkupContent? {
    return null
  }

  override fun visit(node: JavaCastNode): MarkupContent? {
    return null
  }

  override fun visit(node: NewInstanceNode): MarkupContent? {
    return null
  }

  override fun visit(node: ReferenceNode): MarkupContent? {
    val variable = node.variable
    return markup(StringBuilder().apply {
      if (variable is MarcelField) {
        append(variable.visibility.name.lowercase())
        append(" ")
        if (variable.isMarcelStatic) {
          append("static ")
        }
      }
      append(variable.type.simpleName)
      append(" **")
      append(variable.name)
      append("**")
      if (variable is MarcelField) {
        append("\n")
        append(if (variable.isExtension) "Extension field from " else "Field from ")
        append(variable.owner)
      }
    }.toString())
  }

  override fun visit(node: StringNode): MarkupContent? {
    return null
  }

  override fun visit(node: SuperConstructorCallNode) = constructor(node.javaMethod)

  override fun visit(node: ThisConstructorCallNode) = constructor(node.javaMethod)

  private fun constructor(method: MarcelMethod) = markup(StringBuilder().apply {
    append("constructor")
    appendParameters(method.parameters, this)
    append("\n")
    append("From ")
    append(method.ownerClass)
  }.toString())


  override fun visit(node: SuperReferenceNode): MarkupContent? {
    return text(node.type.toString())
  }

  override fun visit(node: TernaryNode): MarkupContent? {
    return null
  }

  override fun visit(node: ThisReferenceNode): MarkupContent? {
    return text(node.type.toString())
  }

  override fun visit(node: ArrayNode): MarkupContent? {
    return null
  }

  override fun visit(node: BoolConstantNode): MarkupContent? {
    return null
  }

  override fun visit(node: ByteConstantNode): MarkupContent? {
    return null
  }

  override fun visit(node: CharConstantNode): MarkupContent? {
    return null
  }

  override fun visit(node: DoubleConstantNode): MarkupContent? {
    return null
  }

  override fun visit(node: FloatConstantNode): MarkupContent? {
    return null
  }

  override fun visit(node: IntConstantNode): MarkupContent? {
    return null
  }

  override fun visit(node: LongConstantNode): MarkupContent? {
    return null
  }

  override fun visit(node: MapNode): MarkupContent? {
    return null
  }

  override fun visit(node: NewArrayNode): MarkupContent? {
    return null
  }

  override fun visit(node: NullValueNode): MarkupContent? {
    return null
  }

  override fun visit(node: ShortConstantNode): MarkupContent? {
    return null
  }

  override fun visit(node: StringConstantNode): MarkupContent? {
    return null
  }

  override fun visit(node: VoidExpressionNode): MarkupContent? {
    return null
  }

  override fun visit(node: AndNode): MarkupContent? {
    return null
  }

  override fun visit(node: ArrayIndexAssignmentNode): MarkupContent? {
    return null
  }

  override fun visit(node: DivNode): MarkupContent? {
    return null
  }

  override fun visit(node: ElvisNode): MarkupContent? {
    return null
  }

  override fun visit(node: GeNode): MarkupContent? {
    return null
  }

  override fun visit(node: GtNode): MarkupContent? {
    return null
  }

  override fun visit(node: IncrNode): MarkupContent? {
    return null
  }

  override fun visit(node: IsEqualNode): MarkupContent? {
    return null
  }

  override fun visit(node: IsNotEqualNode): MarkupContent? {
    return null
  }

  override fun visit(node: LeNode): MarkupContent? {
    return null
  }

  override fun visit(node: LeftShiftNode): MarkupContent? {
    return null
  }

  override fun visit(node: LtNode): MarkupContent? {
    return null
  }

  override fun visit(node: MinusNode): MarkupContent? {
    return null
  }

  override fun visit(node: ModNode): MarkupContent? {
    return null
  }

  override fun visit(node: MulNode): MarkupContent? {
    return null
  }

  override fun visit(node: NotNode): MarkupContent? {
    return null
  }

  override fun visit(node: OrNode): MarkupContent? {
    return null
  }

  override fun visit(node: PlusNode): MarkupContent? {
    return null
  }

  override fun visit(node: RightShiftNode): MarkupContent? {
    return null
  }

  override fun visit(node: VariableAssignmentNode): MarkupContent? {
    return null
  }

  override fun visit(node: ConditionalExpressionNode): MarkupContent? {
    return null
  }

  override fun visit(node: YieldExpression): MarkupContent? {
    return null
  }

  private fun markup(content: String) = MarkupContent(MarkupKind.MARKDOWN, content)
  private fun text(content: String) = MarkupContent(MarkupKind.PLAINTEXT, content)
}