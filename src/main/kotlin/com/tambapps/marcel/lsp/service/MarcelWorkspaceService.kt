package com.tambapps.marcel.lsp.service

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService

class MarcelWorkspaceService: WorkspaceService {

  override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {

  }

  override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
  }
}