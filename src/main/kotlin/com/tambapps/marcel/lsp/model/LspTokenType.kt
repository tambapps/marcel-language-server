package com.tambapps.marcel.lsp.model

enum class LspTokenType {
  namespace,
  type,
  `class`,
  enum,
  `interface`,
  struct,
  typeParameter,
  parameter,
  variable,
  property,
  enumMember,
  event,
  function,
  method,
  macro,
  keyword,
  modifier,
  comment,
  string,
  number,
  regexp,
  operator,
  /**
   * @since 3.17.0
   */
  decorator
}