package com.tambapps.marcel.lsp.lang

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

/**
 * Computes diagnostics, allowing to expose errors, warnings, info on the code
 *
 * @constructor Create empty Marcel diagnostic generator
 */
class MarcelDiagnosticGenerator {

  companion object {
    private const val SOURCE = "compiler"
  }
  fun generate(result: SemanticResult)= mutableListOf<Diagnostic>().apply {
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
      SOURCE
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