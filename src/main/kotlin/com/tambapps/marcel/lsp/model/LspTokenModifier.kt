package com.tambapps.marcel.lsp.model

/**
 * Additional qualifiers that can be associated with a semantic token to refine its meaning.
 */
enum class LspTokenModifier {
  declaration,
  definition,
  readonly,
  static,
  deprecated,
  abstract,
  async,
  modification,
  documentation,
  defaultLibrary
}