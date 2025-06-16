package com.tambapps.marcel.lsp.lang

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.lsp.model.LspTokenModifier
import com.tambapps.marcel.lsp.model.LspTokenType
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.processor.extensions.forEachNode
import com.tambapps.marcel.semantic.variable.Variable
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.variable.field.MethodField

class MarcelCodeHighlighter() {

  /**
   * Returns a FLAT list of tokens.
   * Each group of 5 ints corresponds to 1 token. I didn't choose this, it is from the official spec
   * of the LSP (https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/#textDocument_semanticTokens).
   *
   * @param result the semantic result
   * @return
   */
  fun computeHighlight(result: SemanticResult): List<Int> {
    val highlightTokenMapBuilder = HighlightTokenMapBuilder()
    if (result.ast != null) {
      for (classNode in result.ast.classes) {
        highlightTokenMapBuilder.accept(classNode)
      }
    }
    val highlightTokens = mutableListOf<Int>()
    if (result.tokens != null) {
      val tokens = result.tokens
      val tokenMap = highlightTokenMapBuilder.tokenMap
      applyHighlight(highlightTokens, tokens, tokenMap)
    }
    return highlightTokens
  }

  private fun applyHighlight(highlightTokens: MutableList<Int>, tokens: List<LexToken>,
                             tokenMap: Map<LexToken, Style>) {
    var lastLine = 0
    var lastCol = 0
    for (i in 0 until tokens.size - 1) { // - 1 to avoid handling END_OF_FILE token
      val token = tokens[i]
      val style = when (token.type) {
        TokenType.IDENTIFIER -> tokenMap[token]
        TokenType.TYPE_INT, TokenType.TYPE_LONG, TokenType.TYPE_SHORT, TokenType.TYPE_FLOAT, TokenType.TYPE_DOUBLE, TokenType.TYPE_BOOL, TokenType.TYPE_BYTE, TokenType.TYPE_VOID, TokenType.TYPE_CHAR, TokenType.FUN, TokenType.RETURN,
        TokenType.VALUE_TRUE, TokenType.VALUE_FALSE, TokenType.NEW, TokenType.IMPORT, TokenType.AS, TokenType.INLINE, TokenType.STATIC, TokenType.FOR, TokenType.IN, TokenType.IF, TokenType.ELSE, TokenType.NULL, TokenType.BREAK, TokenType.CONTINUE, TokenType.DEF, TokenType.DO,
        TokenType.CLASS, TokenType.EXTENSION, TokenType.PACKAGE, TokenType.EXTENDS, TokenType.IMPLEMENTS, TokenType.FINAL, TokenType.SWITCH, TokenType.WHEN, TokenType.NOT_WHEN, TokenType.THIS, TokenType.SUPER, TokenType.DUMBBELL, TokenType.TRY, TokenType.CATCH, TokenType.FINALLY, TokenType.WHILE,
        TokenType.INSTANCEOF, TokenType.NOT_INSTANCEOF, TokenType.THROW, TokenType.THROWS, TokenType.CONSTRUCTOR, TokenType.DYNOBJ, TokenType.ASYNC, TokenType.ENUM, TokenType.OVERRIDE,
          // visibilities
        TokenType.VISIBILITY_PUBLIC, TokenType.VISIBILITY_PROTECTED, TokenType.VISIBILITY_INTERNAL, TokenType.VISIBILITY_PRIVATE, -> Style.KEYWORD
        TokenType.INTEGER, TokenType.FLOAT -> Style.NUMBER
        TokenType.BLOCK_COMMENT, TokenType.DOC_COMMENT, TokenType.HASH, TokenType.SHEBANG_COMMENT, TokenType.EOL_COMMENT -> Style.COMMENT
        TokenType.OPEN_QUOTE, TokenType.CLOSING_QUOTE, TokenType.REGULAR_STRING_PART,
        TokenType.OPEN_CHAR_QUOTE, TokenType.CLOSING_CHAR_QUOTE,
        TokenType.OPEN_REGEX_QUOTE, TokenType.CLOSING_REGEX_QUOTE,
        TokenType.OPEN_SIMPLE_QUOTE, TokenType.CLOSING_SIMPLE_QUOTE -> Style.STRING
        TokenType.SHORT_TEMPLATE_ENTRY_START, TokenType.LONG_TEMPLATE_ENTRY_START, TokenType.LONG_TEMPLATE_ENTRY_END -> Style.TEMPLATE_ENTRY
        else -> tokenMap[token]
      } ?: continue
      val deltaLine = token.line - lastLine
      val deltaCol = if (deltaLine == 0) token.column - lastCol else token.column

      highlightTokens.addAll(listOf(deltaLine, deltaCol, token.end - token.start, style.tokenType, style.tokenModifiers))

      lastLine = token.line
      lastCol = token.column
    }
  }
}

data class Style(val tokenType: Int, val tokenModifiers: Int) {
  companion object {
    val ANNOTATION_STYLE = Style(LspTokenType.macro, emptyList())
    val STRING = Style(LspTokenType.string, emptyList())
    val NUMBER = Style(LspTokenType.number, emptyList())
    val KEYWORD = Style(LspTokenType.keyword, emptyList())
    val COMMENT = Style(LspTokenType.comment, emptyList())
    val TEMPLATE_ENTRY = Style(LspTokenType.keyword, emptyList())

  }
  constructor(
    tokenType: LspTokenType,
    tokenModifiers: List<LspTokenModifier>)
      : this(tokenType.ordinal, tokenModifiers.fold(0) {acc, modifier -> acc or (1 shl modifier.ordinal) })
}

private class HighlightTokenMapBuilder {
  val tokenMap = mutableMapOf<LexToken, Style>()

  fun accept(classNode: ClassNode) {

    // TODO implement classNode.identifierToken to then highlight it here

    for (fieldNode in classNode.fields) {
      fieldNode.identifierToken?.let { token -> tokenMap[token] = Style(LspTokenType.property, modifiers(isStatic = fieldNode.isMarcelStatic, isReadOnly = fieldNode.isFinal, isDeclaration = true)) }
    }
    for (annotation in classNode.annotations) {
      acceptAnnotation(annotation)
    }
    for (annotation in classNode.fields.flatMap { it.annotations }) {
      acceptAnnotation(annotation)
    }
    for (annotation in classNode.methods.flatMap { it.annotations }) {
      acceptAnnotation(annotation)
    }
    for (methodNode in classNode.methods) {
      methodNode.identifierToken?.let { token -> tokenMap[token] = Style(LspTokenType.method, modifiers(isStatic = methodNode.isMarcelStatic, isDeclaration = true)) }
      // TODO handle method parameters and types (maybe use the CST?)
      methodNode.blockStatement.forEachNode { node ->
        if (shouldBeProcessed(node)) {
          when (node) {
            is VariableAssignmentNode -> node.identifierToken?.let { token -> tokenMap[token] = variableStyle(node.variable) }
            is ReferenceNode -> tokenMap[node.tokenStart] = variableStyle(node.variable)
            is FunctionCallNode -> tokenMap[node.tokenStart] = Style(LspTokenType.method, modifiers(isStatic = methodNode.isMarcelStatic))
          }
        }
      }
    }
  }

  private fun modifiers(
    isStatic: Boolean = false,
    isAsync: Boolean = false,
    isAbstract: Boolean = false,
    isReadOnly: Boolean = false,
    isDeclaration: Boolean = false,
  ): List<LspTokenModifier> = mutableListOf<LspTokenModifier>().apply {
    if (isStatic) {
      add(LspTokenModifier.static)
    }
    if (isAsync) {
      add(LspTokenModifier.async)
    }
    if (isAbstract) {
      add(LspTokenModifier.abstract)
    }
    if (isReadOnly) {
      add(LspTokenModifier.readonly)
    }
    if (isDeclaration) {
      add(if (isAbstract) LspTokenModifier.definition else LspTokenModifier.declaration)
    }
  }

  fun acceptAnnotation(annotationNode: AnnotationNode) {
    tokenMap[annotationNode.tokenStart] = Style.ANNOTATION_STYLE
    annotationNode.identifierToken?.let { token -> tokenMap[token] = Style.ANNOTATION_STYLE }
  }

  private fun variableStyle(variable: Variable) = when (variable) {
    is MethodField -> {
      val method = if (variable.isGettable) variable.getterMethod else variable.setterMethod
      Style(LspTokenType.function, modifiers(isStatic = variable.isMarcelStatic, isReadOnly = variable.isFinal, isAsync = method.isAsync, isAbstract = method.isAbstract))
    }
    is MarcelField -> Style(LspTokenType.property, modifiers(isStatic = variable.isMarcelStatic, isReadOnly = variable.isFinal))
    else -> Style(LspTokenType.variable, emptyList())
  }

  private fun shouldBeProcessed(node: AstNode): Boolean {
    return node.tokenEnd != LexToken.DUMMY // this is a mark used to recognize nodes of marcel-generated code (not in the source)
        && !tokenMap.containsKey(node.tokenStart) // don't really remember what this condition is for
  }
}
