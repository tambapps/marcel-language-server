package com.tambapps.marcel.lsp

import com.tambapps.marcel.lsp.service.MarcelTextDocumentService
import com.tambapps.marcel.lsp.service.MarcelWorkspaceService
import org.eclipse.lsp4j.CompletionOptions
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.TextDocumentSyncKind
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
  private lateinit var languageClient: LanguageClient

  override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult?>? {
    // Initialize the InitializeResult for this LS.
    val initializeResult = InitializeResult(ServerCapabilities())

    // Set the capabilities of the LS to inform the client.
    initializeResult.capabilities.setTextDocumentSync(TextDocumentSyncKind.Full)
    val completionOptions = CompletionOptions()
    initializeResult.capabilities.completionProvider = completionOptions
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
    this.languageClient = client
  }
}