package com.tambapps.marcel.lsp.service

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
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class MarcelTextDocumentService(
  private val marcelSemanticCompiler: MarcelSemanticCompiler,
): TextDocumentService {

  private val semanticResults = ConcurrentHashMap<String, CompletableFuture<SemanticResult>>()

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
      semanticResults[uri] = CompletableFuture.supplyAsync { marcelSemanticCompiler.apply(text) }
    }
  }

  override fun didChange(params: DidChangeTextDocumentParams) {
    val uri = params.textDocument?.uri
    val changes = params.contentChanges
    if (uri != null && !changes.isNullOrEmpty()) {
      val text = changes.last().text
      semanticResults[uri] = CompletableFuture.supplyAsync { marcelSemanticCompiler.apply(text) }
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