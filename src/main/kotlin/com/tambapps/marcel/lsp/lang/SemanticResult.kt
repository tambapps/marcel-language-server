package com.tambapps.marcel.lsp.lang

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.semantic.ast.ModuleNode

data class SemanticResult(
  val text: String, // TODO may be useless
  val tokens: List<LexToken>? = null,
  val cst: SourceFileCstNode? = null,
  val ast: ModuleNode? = null,
) {

  val dumbbells get() = cst?.dumbbells
  val textHashCode = text.hashCode()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SemanticResult) return false

    if (textHashCode != other.textHashCode) return false

    return true
  }

  override fun hashCode(): Int {
    return textHashCode
  }
}