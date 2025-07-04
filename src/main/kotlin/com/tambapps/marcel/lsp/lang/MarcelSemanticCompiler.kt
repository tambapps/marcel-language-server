package com.tambapps.marcel.lsp.lang

import com.tambapps.marcel.compiler.AbstractMarcelCompiler
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.exception.MarcelCompilerException
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.semantic.analysis.MarcelSemanticAnalysis
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import marcel.lang.URLMarcelClassLoader

class MarcelSemanticCompiler(
  compilerConfiguration: CompilerConfiguration,
): AbstractMarcelCompiler(compilerConfiguration) {

  val lexer = MarcelLexer()

  fun apply(text: String): SemanticResult {
    val tokens = try {
      lexer.lex(text)
    } catch (e: MarcelLexerException) {
      return SemanticResult(text = text, lexerError = e)
    }

    val parser = MarcelParser(tokens)

    val cst = try {
      parser.parse()
    } catch (e: MarcelParserException) {
      return SemanticResult(text = text, tokens = tokens, parserError = e)
    }
    val classLoader = URLMarcelClassLoader()
    val symbolResolver = MarcelSymbolResolver(classLoader)
    try {
      handleDumbbells(classLoader, cst)
    } catch (e: MarcelCompilerException) {
      return SemanticResult(text = text, tokens = tokens)
    }

    val ast = try {
      MarcelSemanticAnalysis.apply(configuration.semanticConfiguration, symbolResolver, cst, "temp.marcel")
    } catch (e: MarcelSemanticException) {
      return SemanticResult(text = text, tokens = tokens, ast = e.ast, semanticError = e)
    }
    return SemanticResult(text = text, tokens = tokens, ast = ast)
  }
}