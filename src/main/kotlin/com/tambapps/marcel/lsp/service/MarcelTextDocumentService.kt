package com.tambapps.marcel.lsp.service

import com.tambapps.marcel.lsp.lang.MarcelCodeHighlighter
import com.tambapps.marcel.lsp.lang.MarcelSemanticCompiler
import com.tambapps.marcel.lsp.lang.SemanticResult
import com.tambapps.marcel.lsp.lang.visitor.findByPosition
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.InsertTextFormat
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.Range
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
  private val marcelLangService = MarcelLangService()
  // SupervisorJob allows coroutines created to be independent of each other when failing
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  var languageClient: LanguageClient? = null

  override fun semanticTokensFull(params: SemanticTokensParams): CompletableFuture<SemanticTokens> {
    val semanticResultFuture = semanticResults[params.textDocument?.uri] ?: return CompletableFuture.completedFuture(SemanticTokens(listOf()))
    return scope.future(CoroutineName("semanticTokensFull for ${params.textDocument?.uri}")) {
      SemanticTokens(highlighter.computeHighlight(semanticResultFuture.await()))
    }
  }

  override fun hover(params: HoverParams): CompletableFuture<Hover?> {
    val uri = params.textDocument?.uri
    val position = params.position
    if (uri == null || position == null) {
      return CompletableFuture.completedFuture(null)
    }
    val semanticResultFuture = semanticResults[uri] ?: return CompletableFuture.completedFuture(null)
    return scope.future(CoroutineName("hover for ${params.textDocument?.uri}")) {
      val semanticResult = semanticResultFuture.await()
      val classes = semanticResult.ast?.classes ?: return@future null
      val nodeOfInterest = classes.firstNotNullOfOrNull { classNode ->
        classNode.methods.firstNotNullOfOrNull {
            methodNode -> methodNode.blockStatement.findByPosition(position.line, position.character)
        }
      } ?: return@future null
      val markupContent = marcelLangService.generateHover(nodeOfInterest) ?: return@future null
      Hover(markupContent, Range(position, Position(position.line, nodeOfInterest.token.end)))
    }
  }

  override fun completion(position: CompletionParams): CompletableFuture<Either<List<CompletionItem>, CompletionList>> {
    val uri = position.textDocument?.uri
    val position = position.position
    if (uri == null || position == null) {
      return CompletableFuture.completedFuture(Either.forLeft(emptyList()))
    }
    // val semanticResultFuture = semanticResults[uri] ?: return CompletableFuture.completedFuture(Either.forLeft(emptyList()))
    return scope.future(CoroutineName("completion for $uri")) {
      // TODO add real completion items based on semanticResult
      // val semanticResult = semanticResultFuture.await()
      Either.forLeft(buildList {
        add(CompletionItem().apply {
          insertText = "sayHello() {\n    print(\"hello\")\n}"
          label = "sayHello()"
          filterText
          kind = CompletionItemKind.Snippet
          insertTextFormat = InsertTextFormat.Snippet
          detail = "sayHello()\n this will say hello to the people"
        })
      })
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
    val future = scope.future { marcelSemanticCompiler.apply(text) }
    semanticResults[uri] = future
    val languageClient = this.languageClient
    if (languageClient != null) {
      scope.launch(CoroutineName("publish diagnostics for $uri")) {
        val semanticResult = future.await()
        val diagnostics = marcelLangService.generateDiagnostic(semanticResult)
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