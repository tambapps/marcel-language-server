package com.tambapps.marcel.lsp.service

import com.tambapps.marcel.lsp.lang.SemanticResult
import com.tambapps.marcel.lsp.lang.visitor.GenerateHoverVisitor
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

/**
 * Service allowing to convert Marcel Lang object into lsp4j objects. It is responsible for
 * - Computes diagnostics, allowing to expose errors, warnings, info on the code
 * - Computing hover information
 * - ...
 *
 */
class MarcelLangService {

  companion object {
    private const val DIAGNOSTIC_SOURCE = "compiler"
  }

  fun generateHover(node: ExpressionNode) = node.accept(GenerateHoverVisitor())

  fun generateDiagnostic(result: SemanticResult) = mutableListOf<Diagnostic>().apply {
    result.lexerError?.let { lexerError ->
      add(diagnostic(lexerError.line, lexerError.column, lexerError.column, lexerError.message))
    }
    result.parserError?.let { parserError ->
      parserError.errors.forEach { error ->
        add(diagnostic(error.token.line, error.token.column, error.token.end, error.message))
      }
    }
    result.semanticError?.let { semanticError ->
      semanticError.errors.forEach { error ->
        add(diagnostic(error.token.line, error.token.column, error.token.end, error.message))
      }
    }
  }

  private fun diagnostic(line: Int, column: Int, end: Int, message: String?): Diagnostic {

    return Diagnostic(
      range(line, column, end),
      transformMessage(message),
      DiagnosticSeverity.Error,
      DIAGNOSTIC_SOURCE
    )
  }

  private fun transformMessage(s: String?): String {
    return if (s == null) {
      "error"
    } else if (s.contains(":")) {
      s.substring(s.lastIndexOf(":") + 1)
    } else {
      s
    }
  }
  private fun range(line: Int, column: Int, end: Int = column) = Range(Position(line, column), Position(line, end))
}