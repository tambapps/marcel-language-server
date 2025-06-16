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
      add(diagnostic(lexerError.line, lexerError.column, lexerError.message))
    }
    result.parserError?.let { parserError ->
      parserError.errors.forEach { error ->
        add(diagnostic(error.token.line, error.token.column, error.message))
      }
    }
    result.semanticError?.let { semanticError ->
      semanticError.errors.forEach { error ->
        add(diagnostic(error.token.line, error.token.column, error.message))
      }
    }
  }

  private fun diagnostic(line: Int, column: Int, message: String?): Diagnostic {
    return Diagnostic(
      range(line, column),
      message ?: "error",
      DiagnosticSeverity.Error,
      SOURCE
    )
  }
  private fun range(line: Int, column: Int) = Position(line, column).let { Range(it, it) }
}