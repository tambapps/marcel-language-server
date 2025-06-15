package com.tambapps.marcel.lsp

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.lsp.lang.MarcelSemanticCompiler
import com.tambapps.marcel.lsp.service.MarcelTextDocumentService
import com.tambapps.marcel.lsp.service.MarcelWorkspaceService
import org.eclipse.lsp4j.launch.LSPLauncher
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Future
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

class Main : CliktCommand() {
  val dumbbellEnabled: Boolean by option().boolean().default(true).help("Enable dumbbell")

  override fun run() {
    StdioLauncher.launch(dumbbellEnabled)
  }
}
fun main(args: Array<String>) = Main().main(args)

object StdioLauncher {

  fun launch(dumbbellEnabled: Boolean) {
    LogManager.getLogManager().reset()
    val globalLogger: Logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)
    globalLogger.setLevel(Level.OFF)

    val marcelSemanticCompiler = MarcelSemanticCompiler(CompilerConfiguration(dumbbellEnabled = dumbbellEnabled))

    // start the language server
    startServer(System.`in`, System.out, marcelSemanticCompiler)
  }

  private fun startServer(`in`: InputStream, out: OutputStream, marcelSemanticCompiler: MarcelSemanticCompiler) {
    // Initialize the HelloLanguageServer
    val textDocumentService = MarcelTextDocumentService(marcelSemanticCompiler)
    val workspaceService = MarcelWorkspaceService()
    val languageServer = MarcelLanguageServer(textDocumentService, workspaceService)
    // Create JSON RPC launcher for HelloLanguageServer instance.
    val launcher = LSPLauncher.createServerLauncher(languageServer, `in`, out)
    // Get the client that request to launch the LS.
    val client = launcher.getRemoteProxy()
    // Set the client to language server
    languageServer.connect(client)
    // Start the listener for JsonRPC
    val startListening: Future<*> = launcher.startListening()
    // Get the computed result from LS.
    startListening.get()
  }
}