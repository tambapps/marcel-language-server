package com.tambapps.marcel.lsp

import com.tambapps.marcel.lsp.model.LspTokenModifier
import com.tambapps.marcel.lsp.model.LspTokenType
import com.tambapps.marcel.lsp.service.MarcelTextDocumentService
import com.tambapps.marcel.lsp.service.MarcelWorkspaceService
import org.eclipse.lsp4j.CompletionOptions
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.SemanticTokensLegend
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.ServerInfo
import org.eclipse.lsp4j.TextDocumentSyncKind
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageClientAware
import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

class MarcelLanguageServer(
  private val textDocumentService: MarcelTextDocumentService,
  private val workspaceService: MarcelWorkspaceService,
): LanguageServer, LanguageClientAware {

  private var exitCode = 1

  override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult?>? {
    // Set the capabilities of the LS to inform the client.
    val capabilities = ServerCapabilities().apply {
      // text highlight capabilities
      semanticTokensProvider = SemanticTokensWithRegistrationOptions().apply {
        // specify supported features
        legend = SemanticTokensLegend(
          LspTokenType.entries.map { it.name },
          LspTokenModifier.entries.map { it.name }
        )
        full = Either.forLeft(true) // supports request on whole document
        hoverProvider = Either.forLeft(true)
      }

      setTextDocumentSync(TextDocumentSyncKind.Full)
      val completionOptions = CompletionOptions()
      completionProvider = completionOptions
    }
    // Initialize the InitializeResult for this LS.
    val initializeResult = InitializeResult(capabilities).apply {
      serverInfo = ServerInfo("marcel-ls", "1.0")
    }
    return CompletableFuture.supplyAsync<InitializeResult?> { initializeResult }
  }

  override fun shutdown(): CompletableFuture<in Any>? {
    exitCode = 0
    return null
  }

  override fun exit() {
    exitProcess(exitCode)
  }

  override fun getTextDocumentService() = textDocumentService

  override fun getWorkspaceService() = workspaceService

  override fun connect(client: LanguageClient) {
    textDocumentService.languageClient = client
    // TODO change log level
    client.logMessage(MessageParams(MessageType.Error, "Connected to Marcel LSP Server"))
  }
}