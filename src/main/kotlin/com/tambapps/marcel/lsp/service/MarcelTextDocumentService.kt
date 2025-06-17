package com.tambapps.marcel.lsp.service

import com.tambapps.marcel.lsp.lang.MarcelCodeHighlighter
import com.tambapps.marcel.lsp.lang.MarcelDiagnosticGenerator
import com.tambapps.marcel.lsp.lang.MarcelSemanticCompiler
import com.tambapps.marcel.lsp.lang.SemanticResult
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.SemanticTokens
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class MarcelTextDocumentService(
  private val marcelSemanticCompiler: MarcelSemanticCompiler,
  private val highlighter: MarcelCodeHighlighter
): TextDocumentService {

  private val semanticResults = ConcurrentHashMap<String, CompletableFuture<SemanticResult>>()
  private val diagnosticGenerator = MarcelDiagnosticGenerator()
  var languageClient: LanguageClient? = null

  override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
    val semanticResultFuture = semanticResults[params.textDocument?.uri] ?: return CompletableFuture.completedFuture(SemanticTokens(listOf()))
    return semanticResultFuture.thenApply { semanticResult -> SemanticTokens(highlighter.computeHighlight(semanticResult)) }
  }

  override fun completion(position: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
    val uri = position.textDocument?.uri
    val position = position.position
    if (uri == null || position == null) {
      return CompletableFuture.completedFuture(Either.forLeft(emptyList()))
    }
    val semanticResultFuture = semanticResults[uri] ?: return CompletableFuture.completedFuture(Either.forLeft(emptyList()))

    return semanticResultFuture.thenApply { semanticResult ->
      // TODO add real completion items based on semanticResult

      val completionItems = mutableListOf<CompletionItem>()
      completionItems.add(CompletionItem().apply {
        insertText = "sayHello() {\n    print(\"hello\")\n}"
        label = "sayHello()"
        filterText
        kind = CompletionItemKind.Snippet
        insertTextFormat = InsertTextFormat.Snippet
        detail = "sayHello()\n this will say hello to the people"
      })
      return@thenApply Either.forLeft(completionItems);
    }
  }

  override fun didOpen(params: DidOpenTextDocumentParams) {
    val uri = params.textDocument?.uri
    val text = params.textDocument?.text
    if (uri != null && text != null) {
      updateSemanticResults(uri = uri, text = text)
    }
  }

  override fun didChange(params: DidChangeTextDocumentParams) {
    val uri = params.textDocument?.uri
    val changes = params.contentChanges
    if (uri != null && !changes.isNullOrEmpty()) {
      val text = changes.last().text
      updateSemanticResults(uri = uri, text = text)
    }
  }

  private fun updateSemanticResults(uri: String, text: String) {
    val future = CompletableFuture.supplyAsync { marcelSemanticCompiler.apply(text) }
    semanticResults[uri] = future
    val languageClient = this.languageClient
    if (languageClient != null) {
      future.thenAcceptAsync { semanticResult ->
        val diagnostics = diagnosticGenerator.generate(semanticResult)
        languageClient.publishDiagnostics(PublishDiagnosticsParams(uri, diagnostics))
      }
    }
  }

  override fun didClose(params: DidCloseTextDocumentParams) {
    val uri = params.textDocument?.uri
    if (uri != null) {
      semanticResults.remove(uri)
    }
  }

  override fun didSave(params: DidSaveTextDocumentParams) {
  }
}